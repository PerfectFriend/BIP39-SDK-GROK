package com.example.bip39onboarding.vault

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Real biometric unlock path (audit P0-3): seals the PIN under a user-authentication
 * Keystore key. On bio success, PIN is unsealed and fed into [VaultUnlock].
 *
 * Does not replace PIN; biometrics is a convenience factor bound to this device.
 * Cleared on vault wipe / enrollment invalidation → fall back to PIN.
 */
object BiometricPinCache {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "bip39_vault_bio_pin_v1"
    private const val PREFS = "bip39_bio_pin_cache"
    private const val KEY_CT = "ct"
    private const val KEY_IV = "iv"
    private const val GCM_TAG = 128

    fun isSealed(context: Context): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.contains(KEY_CT) && p.contains(KEY_IV)
    }

    fun canUse(context: Context): Boolean {
        if (!isSealed(context)) return false
        return BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Seal PIN after successful vault unlock / onboarding. Requires biometric prompt once.
     */
    fun seal(
        activity: FragmentActivity,
        pin: CharArray,
        title: String = "Enable biometric unlock",
        subtitle: String = "Confirm biometrics to store a device-bound unlock factor",
        negative: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val ctx = activity.applicationContext
        try {
            ensureKey()
        } catch (e: Exception) {
            onError(e.message ?: "keystore")
            return
        }
        val cipher = try {
            Cipher.getInstance("AES/GCM/NoPadding").also {
                it.init(Cipher.ENCRYPT_MODE, secretKey())
            }
        } catch (e: Exception) {
            onError(e.message ?: "cipher")
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val c = result.cryptoObject?.cipher ?: run {
                        onError("no_cipher"); return
                    }
                    try {
                        val plain = String(pin).toByteArray(Charsets.UTF_8)
                        val ct = c.doFinal(plain)
                        plain.fill(0)
                        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putString(KEY_IV, Base64.encodeToString(c.iv, Base64.NO_WRAP))
                            .putString(KEY_CT, Base64.encodeToString(ct, Base64.NO_WRAP))
                            .apply()
                        onSuccess()
                    } catch (e: Exception) {
                        onError(e.message ?: "seal_failed")
                    }
                }

                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    if (code != BiometricPrompt.ERROR_USER_CANCELED &&
                        code != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) onError(msg.toString())
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negative)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
    }

    /**
     * Unseal PIN with biometrics, then invoke [onPin] (caller must clear the array).
     */
    fun unseal(
        activity: FragmentActivity,
        title: String = "Unlock",
        subtitle: String = "Use biometrics",
        negative: String = "Use PIN",
        onPin: (CharArray) -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val ctx = activity.applicationContext
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ivB64 = prefs.getString(KEY_IV, null)
        val ctB64 = prefs.getString(KEY_CT, null)
        if (ivB64 == null || ctB64 == null) {
            onError("not_sealed")
            return
        }
        val cipher = try {
            val iv = Base64.decode(ivB64, Base64.NO_WRAP)
            Cipher.getInstance("AES/GCM/NoPadding").also {
                it.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG, iv))
            }
        } catch (e: Exception) {
            // Enrollment changed or key invalidated
            clear(ctx)
            onError(e.message ?: "key_invalidated")
            return
        }
        val ct = Base64.decode(ctB64, Base64.NO_WRAP)
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val c = result.cryptoObject?.cipher ?: run {
                        onError("no_cipher"); return
                    }
                    try {
                        val plain = c.doFinal(ct)
                        val pin = String(plain, Charsets.UTF_8).toCharArray()
                        plain.fill(0)
                        onPin(pin)
                    } catch (e: Exception) {
                        clear(ctx)
                        onError(e.message ?: "unseal_failed")
                    }
                }

                override fun onAuthenticationError(code: Int, msg: CharSequence) {
                    if (code == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        code == BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        onFailed()
                    } else {
                        onError(msg.toString())
                    }
                }

                override fun onAuthenticationFailed() = onFailed()
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negative)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        try {
            val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
            if (ks.containsAlias(ALIAS)) ks.deleteEntry(ALIAS)
        } catch (_: Exception) { /* best effort */ }
    }

    private fun ensureKey() {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        if (ks.containsAlias(ALIAS)) return
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }
        kg.init(builder.build())
        kg.generateKey()
    }

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        return ks.getKey(ALIAS, null) as SecretKey
    }
}
