package com.example.bip39onboarding.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import android.security.keystore.KeyGenParameterSpec

/**
 * Lightweight device integrity + keystore capability signals for UX warnings.
 * Not a replacement for Play Integrity on the backend.
 */
object DeviceIntegrity {

    enum class KeystoreBackend {
        STRONGBOX,
        TEE,
        SOFTWARE,
        UNKNOWN
    }

    data class Report(
        val isRootedHeuristic: Boolean,
        val isEmulatorHeuristic: Boolean,
        val isDebuggable: Boolean,
        val keystoreBackend: KeystoreBackend,
        val hasStrongBoxFeature: Boolean,
        val warnings: List<String>
    )

    fun assess(context: Context): Report {
        val rooted = detectRoot()
        val emulator = detectEmulator()
        val debuggable =
            (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val hasStrongBox =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        val backend = probeKeystoreBackend(hasStrongBox)

        val warnings = mutableListOf<String>()
        if (rooted) warnings.add("root_suspected")
        if (emulator) warnings.add("emulator_suspected")
        if (debuggable) warnings.add("debuggable_build")
        when (backend) {
            KeystoreBackend.SOFTWARE -> warnings.add("keystore_software_only")
            KeystoreBackend.UNKNOWN -> warnings.add("keystore_unknown")
            else -> Unit
        }
        return Report(rooted, emulator, debuggable, backend, hasStrongBox, warnings)
    }

    /**
     * Probe where a temporary AES key is resident (StrongBox / TEE / software).
     */
    private fun probeKeystoreBackend(preferStrongBox: Boolean): KeystoreBackend {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return KeystoreBackend.UNKNOWN
        val alias = "bip39_integrity_probe_${System.nanoTime()}"
        return try {
            val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
            if (preferStrongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    builder.setIsStrongBoxBacked(true)
                } catch (_: Exception) { }
            }
            try {
                kg.init(builder.build())
                kg.generateKey()
            } catch (_: Exception) {
                // retry without StrongBox
                val b2 = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                kg.init(b2.build())
                kg.generateKey()
            }

            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val key = ks.getKey(alias, null) as SecretKey
            val factory = SecretKeyFactory.getInstance(key.algorithm, "AndroidKeyStore")
            val info = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
            ks.deleteEntry(alias)

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    info.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX ->
                    KeystoreBackend.STRONGBOX
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    info.securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT ->
                    KeystoreBackend.TEE
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    info.securityLevel == KeyProperties.SECURITY_LEVEL_SOFTWARE ->
                    KeystoreBackend.SOFTWARE
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && info.isInsideSecureHardware ->
                    KeystoreBackend.TEE
                else -> KeystoreBackend.SOFTWARE
            }
        } catch (_: Exception) {
            KeystoreBackend.UNKNOWN
        }
    }

    private fun detectRoot(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        if (paths.any { File(it).exists() }) return true
        return try {
            Runtime.getRuntime().exec(arrayOf("which", "su")).inputStream.bufferedReader().readLine() != null
        } catch (_: Exception) {
            false
        }
    }

    private fun detectEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val device = Build.DEVICE
        val product = Build.PRODUCT
        return fingerprint.startsWith("generic") ||
            fingerprint.startsWith("unknown") ||
            model.contains("google_sdk") ||
            model.contains("Emulator") ||
            model.contains("Android SDK built for x86") ||
            manufacturer.contains("Genymotion") ||
            (brand.startsWith("generic") && device.startsWith("generic")) ||
            product == "google_sdk" ||
            product.contains("sdk") ||
            product.contains("emulator")
    }
}
