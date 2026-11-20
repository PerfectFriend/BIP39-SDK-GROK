package com.example.bip39onboarding.vault

import com.example.bip39onboarding.security.Bip39Manager
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Vault format v1 encode/decode (docs/VAULT_FORMAT.md).
 * Writes **Argon2id** (`kdf_id=1`) when native lib is present; otherwise PBKDF2 (`kdf_id=2`).
 * Body stores **entropy only** (VAULT_FORMAT); unlock still accepts legacy UTF-8 mnemonic bodies.
 * Unlock accepts both KDF ids.
 */
object VaultV1 {

    private const val HEADER_LEN = 39

    data class Opened(
        val mnemonic: CharArray,
        val wordCount: Int,
        val fingerprint: String,
        val hasPassphrase: Boolean,
        val passphrase: CharArray
    ) {
        fun clear() {
            Bip39Manager.clear(mnemonic)
            Bip39Manager.clear(passphrase)
        }
    }

    data class CreateInput(
        val mnemonic: CharArray,
        val pin: CharArray,
        val panicPin: CharArray,
        val passphrase: CharArray = charArrayOf(),
        val pepper: ByteArray,
        val wordCount: Int,
        val iterations: Int = VaultCrypto.PBKDF2_ITERATIONS
    )

    fun create(input: CreateInput): ByteArray {
        require(input.pepper.size == 32) { "pepper must be 32 bytes" }
        require(input.panicPin.isEmpty() || !input.pin.contentEquals(input.panicPin)) {
            "panic pin must differ from PIN"
        }

        val salt = VaultCrypto.randomBytes(VaultCrypto.SALT_LEN)
        val dek = VaultCrypto.randomBytes(VaultCrypto.DEK_LEN)
        val useArgon = VaultCrypto.preferArgon2id()
        val kdfId = if (useArgon) VaultCrypto.KDF_ARGON2ID else VaultCrypto.KDF_PBKDF2
        val p1 = if (useArgon) RustNativeKdf.DEFAULT_M_KIB
            else input.iterations.coerceAtLeast(VaultCrypto.MIN_PBKDF2_ITERATIONS)
        val p2 = if (useArgon) RustNativeKdf.DEFAULT_T else 0
        val p3 = if (useArgon) RustNativeKdf.DEFAULT_P else 0
        val hasPp = input.passphrase.isNotEmpty()
        val flags = if (hasPp) 1 else 0
        val header = buildHeader(salt, kdfId, p1, p2, p3, flags)

        val kek = VaultCrypto.deriveKek(input.pin, salt, input.pepper, kdfId, p1, p2, p3)
        val wrapPin = try {
            VaultCrypto.aeadEncrypt(kek, dek, header)
        } finally {
            kek.fill(0)
        }

        val panicMac = if (input.panicPin.isEmpty()) {
            VaultCrypto.randomBytes(32)
        } else {
            val panicKey = VaultCrypto.derivePanicKey(input.panicPin, salt, input.pepper, kdfId, p1, p2, p3)
            try {
                VaultCrypto.panicMac(panicKey, salt)
            } finally {
                panicKey.fill(0)
            }
        }

        val seedNoPp = Bip39Manager.toSeed(input.mnemonic, charArrayOf())
        val seedWithPp = Bip39Manager.toSeed(input.mnemonic, input.passphrase)
        val fp = VaultCrypto.hmacSha256("bip39-sdk/fingerprint/v1", seedNoPp).copyOf(4)
        val ppCheck = VaultCrypto.hmacSha256("bip39-sdk/pp-check/v1", seedWithPp).copyOf(4)
        seedNoPp.fill(0)
        seedWithPp.fill(0)

        // VAULT_FORMAT body: entropy only (not UTF-8 mnemonic)
        val entropy = Bip39Manager.mnemonicToEntropy(input.mnemonic)
        val bodyPlain = ByteBuffer.allocate(1 + entropy.size + 1 + 4 + 4 + 8)
            .order(ByteOrder.BIG_ENDIAN)
            .put(entropy.size.toByte())
            .put(entropy)
            .put(input.wordCount.toByte())
            .put(ppCheck)
            .put(fp)
            .putLong(System.currentTimeMillis() / 1000)
            .array()
        entropy.fill(0)
        ppCheck.fill(0)

        val body = try {
            VaultCrypto.aeadEncrypt(dek, bodyPlain, header)
        } finally {
            bodyPlain.fill(0)
            dek.fill(0)
        }
        fp.fill(0)

        val out = ByteBuffer.allocate(header.size + 2 + wrapPin.size + 2 + body.size + 32)
            .order(ByteOrder.BIG_ENDIAN)
        out.put(header)
        out.putShort(wrapPin.size.toShort())
        out.put(wrapPin)
        out.putShort(body.size.toShort())
        out.put(body)
        out.put(panicMac)
        panicMac.fill(0)
        return out.array()
    }

    fun unlock(
        blob: ByteArray,
        pin: CharArray,
        pepper: ByteArray,
        passphrase: CharArray = charArrayOf()
    ): Opened {
        if (blob.size < HEADER_LEN + 2 + 16 + 2 + 16 + 32) throw VaultError.Corrupted
        if (blob[0] != VaultCrypto.MAGIC0 || blob[1] != VaultCrypto.MAGIC1 ||
            blob[2] != VaultCrypto.MAGIC2 || blob[3] != VaultCrypto.MAGIC3
        ) throw VaultError.Corrupted

        val hdr = ByteBuffer.wrap(blob, 0, HEADER_LEN).order(ByteOrder.BIG_ENDIAN)
        hdr.position(4)
        val version = hdr.short
        if (version.toInt() != 1) throw VaultError.Corrupted
        val kdfId = hdr.get()
        if (kdfId != VaultCrypto.KDF_ARGON2ID && kdfId != VaultCrypto.KDF_PBKDF2) {
            throw VaultError.Corrupted
        }
        val p1 = hdr.int
        val p2 = hdr.int
        val p3 = hdr.int
        when (kdfId) {
            VaultCrypto.KDF_ARGON2ID -> {
                if (p1 < RustNativeKdf.MIN_M_KIB || p2 < RustNativeKdf.MIN_T || p3 < 1) {
                    throw VaultError.Corrupted // anti-downgrade
                }
                if (!RustNativeKdf.isAvailable) {
                    throw VaultError.Other("Argon2id vault requires native library")
                }
            }
            VaultCrypto.KDF_PBKDF2 -> {
                if (p1 < VaultCrypto.MIN_PBKDF2_ITERATIONS) throw VaultError.Corrupted
            }
        }
        val salt = ByteArray(VaultCrypto.SALT_LEN).also { hdr.get(it) }
        val flags = hdr.short.toInt() and 0xFFFF
        val headerLen = hdr.short.toInt() and 0xFFFF
        if (headerLen != HEADER_LEN) throw VaultError.Corrupted
        val header = blob.copyOfRange(0, HEADER_LEN)

        val rest = ByteBuffer.wrap(blob, HEADER_LEN, blob.size - HEADER_LEN).order(ByteOrder.BIG_ENDIAN)
        val wrapLen = rest.short.toInt() and 0xFFFF
        if (wrapLen <= 0 || rest.remaining() < wrapLen + 2 + 32) throw VaultError.Corrupted
        val wrapPin = ByteArray(wrapLen).also { rest.get(it) }
        val bodyLen = rest.short.toInt() and 0xFFFF
        if (bodyLen <= 0 || rest.remaining() < bodyLen + 32) throw VaultError.Corrupted
        val body = ByteArray(bodyLen).also { rest.get(it) }
        val panicMacStored = ByteArray(32).also { rest.get(it) }

        val panicKey = VaultCrypto.derivePanicKey(pin, salt, pepper, kdfId, p1, p2, p3)
        val attemptMac = try {
            VaultCrypto.panicMac(panicKey, salt)
        } finally {
            panicKey.fill(0)
        }
        val isPanic = VaultCrypto.constantTimeEquals(attemptMac, panicMacStored)
        attemptMac.fill(0)
        if (isPanic) {
            panicMacStored.fill(0)
            throw VaultError.Panic
        }

        val kek = VaultCrypto.deriveKek(pin, salt, pepper, kdfId, p1, p2, p3)
        val dek = try {
            VaultCrypto.aeadDecrypt(kek, wrapPin, header)
        } finally {
            kek.fill(0)
        }
        if (dek == null) {
            panicMacStored.fill(0)
            throw VaultError.WrongPin
        }
        if (dek.size != VaultCrypto.DEK_LEN) {
            dek.fill(0)
            panicMacStored.fill(0)
            throw VaultError.Corrupted
        }

        val bodyPlain = VaultCrypto.aeadDecrypt(dek, body, header)
        dek.fill(0)
        panicMacStored.fill(0)
        if (bodyPlain == null) throw VaultError.Corrupted

        return try {
            parseBody(bodyPlain, passphrase, hasPassphraseFlag = (flags and 1) != 0)
        } finally {
            bodyPlain.fill(0)
        }
    }

    private fun parseBody(bodyPlain: ByteArray, passphrase: CharArray, hasPassphraseFlag: Boolean): Opened {
        val bb = ByteBuffer.wrap(bodyPlain).order(ByteOrder.BIG_ENDIAN)
        val first = bb.get().toInt() and 0xFF

        // New layout (VAULT_FORMAT): entropy_len ∈ {16,20,24,28,32} for 12/15/18/21/24 words
        // Legacy layout: word_count ∈ {12,15,18,21,24} then has_pp flag
        val mnemonic: CharArray
        val wordCount: Int
        val ppCheck: ByteArray
        val fp: ByteArray
        var hasPpFlag: Boolean

        val isEntropyBody = first in setOf(16, 20, 24, 28, 32)
        if (isEntropyBody) {
            val entropy = ByteArray(first).also { bb.get(it) }
            wordCount = bb.get().toInt() and 0xFF
            ppCheck = ByteArray(4).also { bb.get(it) }
            fp = ByteArray(4).also { bb.get(it) }
            mnemonic = try {
                Bip39Manager.entropyToMnemonic(entropy)
            } finally {
                entropy.fill(0)
            }
            hasPpFlag = hasPassphraseFlag
        } else {
            // Legacy UTF-8 mnemonic body
            wordCount = first
            hasPpFlag = bb.get().toInt() and 0xFF == 1
            val mnemonicLen = bodyPlain.size - 2 - 4 - 4 - 8
            if (mnemonicLen <= 0) throw VaultError.Corrupted
            val mnemonicBytes = ByteArray(mnemonicLen).also { bb.get(it) }
            ppCheck = ByteArray(4).also { bb.get(it) }
            fp = ByteArray(4).also { bb.get(it) }
            mnemonic = String(mnemonicBytes, Charsets.UTF_8).toCharArray()
            mnemonicBytes.fill(0)
        }

        if (wordCount !in setOf(12, 15, 18, 21, 24)) {
            Bip39Manager.clear(mnemonic)
            throw VaultError.Corrupted
        }

        val seedWithPp = Bip39Manager.toSeed(mnemonic, passphrase)
        val check = VaultCrypto.hmacSha256("bip39-sdk/pp-check/v1", seedWithPp).copyOf(4)
        seedWithPp.fill(0)
        val ok = VaultCrypto.constantTimeEquals(check, ppCheck)
        check.fill(0)
        ppCheck.fill(0)

        if (hasPpFlag && passphrase.isEmpty()) {
            Bip39Manager.clear(mnemonic)
            fp.fill(0)
            throw VaultError.WrongPassphrase
        }
        if (passphrase.isNotEmpty() && !ok) {
            Bip39Manager.clear(mnemonic)
            fp.fill(0)
            throw VaultError.WrongPassphrase
        }

        val fpHex = fp.joinToString("") { "%02x".format(it) }
        fp.fill(0)
        return Opened(
            mnemonic = mnemonic,
            wordCount = wordCount,
            fingerprint = fpHex,
            hasPassphrase = hasPpFlag,
            passphrase = passphrase.copyOf()
        )
    }

    private fun buildHeader(
        salt: ByteArray,
        kdfId: Byte,
        p1: Int,
        p2: Int,
        p3: Int,
        flags: Int
    ): ByteArray {
        val buf = ByteBuffer.allocate(HEADER_LEN).order(ByteOrder.BIG_ENDIAN)
        buf.put(VaultCrypto.MAGIC0)
        buf.put(VaultCrypto.MAGIC1)
        buf.put(VaultCrypto.MAGIC2)
        buf.put(VaultCrypto.MAGIC3)
        buf.putShort(VaultCrypto.VERSION)
        buf.put(kdfId)
        buf.putInt(p1)
        buf.putInt(p2)
        buf.putInt(p3)
        buf.put(salt)
        buf.putShort(flags.toShort())
        buf.putShort(HEADER_LEN.toShort())
        return buf.array()
    }
}
