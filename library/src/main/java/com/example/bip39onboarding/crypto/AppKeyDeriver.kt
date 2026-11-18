package com.example.bip39onboarding.crypto

import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.security.Bip39Manager

/**
 * Host-facing key derivation after onboarding unlock.
 * Prefer [KeyDerivationKit] for domain keys; this interface is the stable host contract.
 */
interface AppKeyDeriver {
    fun bip39Seed(payload: SeedPayload, passphrase: CharArray = charArrayOf()): ByteArray
    fun hkdf(payload: SeedPayload, namespace: String, length: Int = 32): ByteArray
}

object DefaultAppKeyDeriver : AppKeyDeriver {
    override fun bip39Seed(payload: SeedPayload, passphrase: CharArray): ByteArray {
        return Bip39Manager.toSeed(payload.mnemonic, passphrase)
    }

    override fun hkdf(payload: SeedPayload, namespace: String, length: Int): ByteArray {
        return KeyDerivationKit.derive(payload, namespace, length)
    }
}
