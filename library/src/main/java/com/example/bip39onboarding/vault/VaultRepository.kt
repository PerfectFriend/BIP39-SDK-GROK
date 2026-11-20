package com.example.bip39onboarding.vault

import android.content.Context
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.CryptoUtils
import com.example.bip39onboarding.security.SeedStorage
import java.io.File

/**
 * Primary vault I/O under [Context.getNoBackupFilesDir] (audit P0-4).
 * Also can migrate legacy [SeedStorage] on first successful unlock.
 */
class VaultRepository(private val context: Context) {

    private val file: File
        get() = File(context.noBackupFilesDir, FILE_NAME)

    fun isSetup(): Boolean = file.isFile && file.length() > 64

    fun wipe() {
        try {
            if (file.exists()) {
                // best-effort overwrite
                val n = file.length().toInt().coerceAtMost(2_000_000)
                if (n > 0) file.writeBytes(ByteArray(n))
                file.delete()
            }
        } catch (_: Exception) {
            file.delete()
        }
        DevicePepper.deleteKey()
        BiometricPinCache.clear(context)
    }

    fun create(
        mnemonic: CharArray,
        pin: CharArray,
        panicPin: CharArray,
        passphrase: CharArray,
        wordCount: Int
    ) {
        if (!DevicePepper.ensureKey(context)) {
            throw VaultError.Other("keystore pepper unavailable")
        }
        val pepper = DevicePepper.hmac(context, ByteArray(16) { it.toByte() })
            ?: throw VaultError.Other("keystore pepper unavailable")
        try {
            val blob = VaultV1.create(
                VaultV1.CreateInput(
                    mnemonic = mnemonic,
                    pin = pin,
                    panicPin = panicPin,
                    passphrase = passphrase,
                    pepper = pepper,
                    wordCount = wordCount
                )
            )
            // Atomic replace: write temp in same dir, then rename
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, FILE_NAME + ".tmp")
            tmp.writeBytes(blob)
            if (!tmp.renameTo(file)) {
                // fallback if rename fails across FS boundaries
                file.writeBytes(blob)
                tmp.delete()
            }
            blob.fill(0)
        } finally {
            pepper.fill(0)
        }
    }

    fun unlock(pin: CharArray, passphrase: CharArray = charArrayOf()): VaultV1.Opened {
        if (!isSetup()) throw VaultError.NotSetup
        val pepper = DevicePepper.hmac(context, ByteArray(16) { it.toByte() })
            ?: throw VaultError.KeyLost
        val blob = try {
            file.readBytes()
        } catch (_: Exception) {
            pepper.fill(0)
            throw VaultError.Corrupted
        }
        return try {
            VaultV1.unlock(blob, pin, pepper, passphrase)
        } catch (e: VaultError.Panic) {
            wipe()
            throw e
        } finally {
            blob.fill(0)
            pepper.fill(0)
        }
    }

    /**
     * After legacy PIN unlock succeeds, rewrite as vault v1 and wipe legacy storage.
     */
    fun migrateFromLegacy(
        mnemonic: CharArray,
        pin: CharArray,
        panicPin: CharArray,
        passphrase: CharArray,
        wordCount: Int,
        legacy: SeedStorage
    ) {
        create(mnemonic, pin, panicPin, passphrase, wordCount)
        legacy.wipe()
    }

    companion object {
        const val FILE_NAME = "bip39_vault_v1.bin"
    }
}
