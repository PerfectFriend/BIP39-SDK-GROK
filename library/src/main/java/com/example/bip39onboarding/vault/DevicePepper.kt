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
 * Binds vault unwrap to this device (audit P0-2).
 */
object DevicePepper {

    private const val ALIAS = "bip39_vault_pepper_hmac_v1"

    fun ensureKey(context: Context): Boolean {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (ks.containsAlias(ALIAS)) return true
        return try {
            val hasStrongBox = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
            val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setKeySize(256)
            if (hasStrongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    builder.setIsStrongBoxBacked(true)
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

    fun hmac(context: Context, data: ByteArray): ByteArray? {
        return try {
            if (!ensureKey(context)) return null
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
