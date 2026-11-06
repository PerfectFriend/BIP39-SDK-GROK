package com.example.bip39onboarding.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator

/**
 * Pre-creates the Tink master key alias with StrongBox when the device supports it.
 * If the alias already exists (any backend), it is left untouched.
 */
object KeystoreMasterKey {

    /** Must match [TinkSeedStorage] master key URI path after android-keystore:// */
    const val TINK_MASTER_ALIAS = "bip39_tink_master_key"

    /**
     * @return backend used for a newly created key, or existing/unknown if already present / failed
     */
    fun ensureTinkMasterKey(context: Context): DeviceIntegrity.KeystoreBackend {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (ks.containsAlias(TINK_MASTER_ALIAS)) {
            return DeviceIntegrity.KeystoreBackend.UNKNOWN // already provisioned
        }

        val hasStrongBox = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        fun generate(strongBox: Boolean) {
            val builder = KeyGenParameterSpec.Builder(
                TINK_MASTER_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
            if (strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.setIsStrongBoxBacked(true)
            }
            kg.init(builder.build())
            kg.generateKey()
        }

        return try {
            if (hasStrongBox) {
                try {
                    generate(strongBox = true)
                    DeviceIntegrity.KeystoreBackend.STRONGBOX
                } catch (_: Exception) {
                    generate(strongBox = false)
                    DeviceIntegrity.KeystoreBackend.TEE
                }
            } else {
                generate(strongBox = false)
                DeviceIntegrity.KeystoreBackend.TEE
            }
        } catch (_: Exception) {
            DeviceIntegrity.KeystoreBackend.UNKNOWN
        }
    }
}
