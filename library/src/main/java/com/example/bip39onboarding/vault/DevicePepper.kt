package com.example.bip39onboarding.vault

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

/**
 * Non-exportable AndroidKeyStore HMAC key used as pepper in KEK derivation.
 * Binds vault unwrap to this device (audit P0-2 / KeyLost).
 *
 * Create only via [ensureKey] during vault create. Unlock path must call [hmac]
 * which never generates a new key — missing alias → null → [VaultError.KeyLost].
 */
object DevicePepper {

    private const val ALIAS = "bip39_vault_pepper_hmac_v1"

    fun hasKey(): Boolean {
        return try {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            ks.containsAlias(ALIAS)
        } catch (_: Exception) {
            false
        }
    }

    /** Create pepper key if missing. Returns false if Keystore unavailable. */
    fun ensureKey(context: Context): Boolean {
        if (hasKey()) return true
        return try {
            val hasStrongBox = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
            val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, "AndroidKeyStore")
            if (hasStrongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    val builder = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setKeySize(256)
                        .setIsStrongBoxBacked(true)
                    kg.init(builder.build())
                    kg.generateKey()
                    return true
                } catch (_: Exception) {
                    // fall through to TEE
                }
            }
            val tee = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setKeySize(256)
            kg.init(tee.build())
            kg.generateKey()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * HMAC-SHA256 over [data] with the device pepper key.
     * Does **not** create a key — returns null if alias missing or crypto fails.
     */
    fun hmac(context: Context, data: ByteArray): ByteArray? {
        return try {
            if (!hasKey()) return null
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val key = ks.getKey(ALIAS, null) as? SecretKey ?: return null
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(key)
            mac.doFinal(data)
        } catch (_: Exception) {
            null
        }
    }

    fun deleteKey() {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (ks.containsAlias(ALIAS)) ks.deleteEntry(ALIAS)
        } catch (_: Exception) { /* best effort */ }
    }
}
