package com.example.bip39onboarding.security

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
 * Biometric gate with [BiometricPrompt.CryptoObject] (Keystore AES-GCM).
 *
 * Does not replace PIN-derived seed encryption. Proves biometric match via cipher.
 */
object BiometricHelper {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "bip39_onboarding_biometric_gate_v1"
    private const val PREFS = "bip39_bio_gate"
    private const val GCM_TAG_BITS = 128
    private val GATE_PLAIN = "bip39-gate".toByteArray(Charsets.UTF_8)

    fun canAuthenticate(context: Context): Boolean {
        return BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun availabilityMessage(context: Context): String {
        return when (
            BiometricManager.from(context).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> "available"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "none_enrolled"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "no_hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "hw_unavailable"
            else -> "unavailable"
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButton: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        showPrompt(
            activity, title, subtitle, negativeButton, null,
            onResult = { onSuccess() },
            onError, onFailed
        )
    }

    /**
     * First successful run seals a gate blob under biometric auth;
     * later runs decrypt that blob with CryptoObject.
     */
    fun authenticateWithCrypto(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButton: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val ctx = activity.applicationContext
        try {
            ensureKey()
        } catch (e: Exception) {
            onError(e.message ?: "keystore_key_failed")
            return
        }

        val hasBlob = loadCiphertext(ctx) != null
        val cipher = try {
            if (hasBlob) decryptCipher(ctx) else encryptCipher()
        } catch (e: Exception) {
            onError(e.message ?: "cipher_init_failed")
            return
        }

        showPrompt(
            activity, title, subtitle, negativeButton,
            BiometricPrompt.CryptoObject(cipher),
            onResult = { result ->
                val c = result.cryptoObject?.cipher ?: run {
                    onError("crypto_object_missing")
                    return@showPrompt
                }
                try {
                    if (hasBlob) {
                        c.doFinal(loadCiphertext(ctx)!!)
                    } else {
                        val ct = c.doFinal(GATE_PLAIN)
                        val iv = c.iv
                        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putString("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
                            .putString("ct", Base64.encodeToString(ct, Base64.NO_WRAP))
                            .apply()
                    }
                    onSuccess()
                } catch (e: Exception) {
                    onError(e.message ?: "cipher_failed")
                }
            },
            onError, onFailed
        )
    }

    private fun showPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButton: String,
        cryptoObject: BiometricPrompt.CryptoObject?,
        onResult: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() = onFailed()
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButton)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        if (cryptoObject != null) prompt.authenticate(info, cryptoObject)
        else prompt.authenticate(info)
    }

    private fun ensureKey() {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        if (ks.containsAlias(KEY_ALIAS)) return
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        fun spec(strongBox: Boolean) = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                } else {
                    @Suppress("DEPRECATION")
                    setUserAuthenticationValidityDurationSeconds(-1)
                }
                if (strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setIsStrongBoxBacked(true)
                }
            }.build()
        try {
            kg.init(spec(true))
            kg.generateKey()
        } catch (_: Exception) {
            kg.init(spec(false))
            kg.generateKey()
        }
    }

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        return ks.getKey(KEY_ALIAS, null) as SecretKey
    }

    private fun encryptCipher(): Cipher =
        Cipher.getInstance("AES/GCM/NoPadding").also {
            it.init(Cipher.ENCRYPT_MODE, secretKey())
        }

    private fun decryptCipher(context: Context): Cipher {
        val iv = loadIv(context) ?: throw IllegalStateException("iv_missing")
        return Cipher.getInstance("AES/GCM/NoPadding").also {
            it.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
    }

    private fun loadIv(context: Context): ByteArray? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("iv", null)?.let { Base64.decode(it, Base64.NO_WRAP) }

    private fun loadCiphertext(context: Context): ByteArray? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("ct", null)?.let { Base64.decode(it, Base64.NO_WRAP) }
}
