package com.example.bip39onboarding.security

/**
 * Common storage contract so host can inject ESP or Tink implementation.
 */
interface SeedStorage {
    fun isSetup(): Boolean
    fun save(
        payload: CryptoUtils.EncryptedPayload,
        pinHash: String,
        panicPinHash: String,
        wordCount: Int,
        fingerprint: String,
        hasPassphrase: Boolean = false
    )
    fun getPayload(): CryptoUtils.EncryptedPayload?
    fun getPinHash(): String?
    fun getPanicPinHash(): String?
    fun getFingerprint(): String?
    fun getWordCount(): Int
    fun hasPassphrase(): Boolean = false
    fun wipe()
}

/** Adapter: legacy EncryptedSharedPreferences. */
class EspSeedStorageAdapter(private val inner: SecureSeedStorage) : SeedStorage {
    override fun isSetup() = inner.isSetup()
    override fun save(
        payload: CryptoUtils.EncryptedPayload,
        pinHash: String,
        panicPinHash: String,
        wordCount: Int,
        fingerprint: String,
        hasPassphrase: Boolean
    ) = inner.save(payload, pinHash, panicPinHash, wordCount, fingerprint)
    override fun getPayload() = inner.getPayload()
    override fun getPinHash() = inner.getPinHash()
    override fun getPanicPinHash() = inner.getPanicPinHash()
    override fun getFingerprint() = inner.getFingerprint()
    override fun getWordCount() = inner.getWordCount()
    override fun hasPassphrase() = false
    override fun wipe() = inner.wipe()
}

/** Adapter: DataStore + Tink. */
class TinkSeedStorageAdapter(private val inner: TinkSeedStorage) : SeedStorage {
    override fun isSetup() = inner.isSetup()
    override fun save(
        payload: CryptoUtils.EncryptedPayload,
        pinHash: String,
        panicPinHash: String,
        wordCount: Int,
        fingerprint: String,
        hasPassphrase: Boolean
    ) = inner.save(payload, pinHash, panicPinHash, wordCount, fingerprint, hasPassphrase)
    override fun getPayload() = inner.getPayload()
    override fun getPinHash() = inner.getPinHash()
    override fun getPanicPinHash() = inner.getPanicPinHash()
    override fun getFingerprint() = inner.getFingerprint()
    override fun getWordCount() = inner.getWordCount()
    override fun hasPassphrase() = inner.hasPassphrase()
    override fun wipe() = inner.wipe()
}
