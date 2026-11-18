package com.example.bip39onboarding.crypto

import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.security.Bip39Manager

/**
 * Domain-separated keys from BIP39 seed.
 */
object KeyDerivationKit {

    object Domains {
        const val ENCRYPTION = "bip39-sdk/encryption/v1"
        const val SIGNING = "bip39-sdk/signing/v1"
        const val BACKUP_WRAP = "bip39-sdk/backup-wrap/v1"
        const val FINGERPRINT = "bip39-sdk/fingerprint/v1"
    }

    fun derive(seed: ByteArray, domain: String, length: Int = 32, salt: ByteArray? = null): ByteArray {
        require(seed.isNotEmpty()) { "empty seed" }
        require(domain.isNotEmpty()) { "empty domain" }
        return Hkdf.derive(seed, salt, domain.toByteArray(Charsets.UTF_8), length)
    }

    fun derive(payload: SeedPayload, domain: String, length: Int = 32): ByteArray {
        val seed = payload.toSeedBytes()
        return try {
            derive(seed, domain, length)
        } finally {
            Bip39Manager.clear(seed)
        }
    }

    fun encryptionKey(payload: SeedPayload): ByteArray =
        derive(payload, Domains.ENCRYPTION, 32)

    fun signingKey(payload: SeedPayload): ByteArray =
        derive(payload, Domains.SIGNING, 32)

    fun backupWrapKey(payload: SeedPayload): ByteArray =
        derive(payload, Domains.BACKUP_WRAP, , 32)
}
