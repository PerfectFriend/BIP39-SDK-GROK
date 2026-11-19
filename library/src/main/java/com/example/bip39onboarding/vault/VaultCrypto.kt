package com.example.bip39onboarding.vault

import com.example.bip39onboarding.crypto.Hkdf
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Crypto helpers for vault v1.
 *
 * Prefer **Argon2id** via [RustNativeKdf] (`kdf_id = 1`) when native lib is loaded.
 * Fall back to PBKDF2-HMAC-SHA512 (`kdf_id = 2`) on JVM-only builds.
 */
internal object VaultCrypto {

    const val MAGIC0 = 'B'.code.toByte()
    const val MAGIC1 = '3'.code.toByte()
    const val MAGIC2 = '9'.code.toByte()
    const val MAGIC3 = 'V'.code.toByte()
    const val VERSION: Short = 1
    const val KDF_ARGON2ID: Byte = 1
    const val KDF_PBKDF2: Byte = 2
    const val SALT_LEN = 16
    const val NONCE_LEN = 12
    const val TAG_BITS = 128
    const val DEK_LEN = 32
    const val PBKDF2_ITERATIONS = 600_000
    const val MIN_PBKDF2_ITERATIONS = 310_000

    private val random = SecureRandom()

    fun randomBytes(n: Int): ByteArray = ByteArray(n).also { random.nextBytes(it) }

    fun preferArgon2id(): Boolean = RustNativeKdf.isAvailable

    fun pbkdf2(pin: CharArray, salt: ByteArray, iterations: Int = PBKDF2_ITERATIONS): ByteArray {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        val spec = PBEKeySpec(pin, salt, iterations, 256)
        return try {
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun deriveKekPbkdf2(pin: CharArray, salt: ByteArray, pepper: ByteArray, iterations: Int): ByteArray {
        val ikm = pbkdf2(pin, salt, iterations)
        return try {
            val combined = ikm + pepper
            try {
                Hkdf.derive(combined, salt, "bip39-sdk/kek/v1".toByteArray(Charsets.UTF_8), 32)
            } finally {
                combined.fill(0)
            }
        } finally {
            ikm.fill(0)
        }
    }

    fun derivePanicKeyPbkdf2(
        panicPin: CharArray,
        salt: ByteArray,
        pepper: ByteArray,
        iterations: Int
    ): ByteArray {
        val ikm = pbkdf2(panicPin, salt, iterations)
        return try {
            val combined = ikm + pepper
            try {
                Hkdf.derive(combined, salt, "bip39-sdk/panic/v1".toByteArray(Charsets.UTF_8), 32)
            } finally {
                combined.fill(0)
            }
        } finally {
            ikm.fill(0)
        }
    }

    fun deriveKek(
        pin: CharArray,
        salt: ByteArray,
        pepper: ByteArray,
        kdfId: Byte,
        p1: Int,
        p2: Int = RustNativeKdf.DEFAULT_T,
        p3: Int = RustNativeKdf.DEFAULT_P
    ): ByteArray {
        if (kdfId == KDF_ARGON2ID) {
            val native = RustNativeKdf.deriveKek(pin, salt, pepper, p1, p2, p3)
                ?: throw VaultError.Other("Argon2id native unavailable")
            return native
        }
        return deriveKekPbkdf2(pin, salt, pepper, p1)
    }

    fun derivePanicKey(
        panicPin: CharArray,
        salt: ByteArray,
        pepper: ByteArray,
        kdfId: Byte,
        p1: Int,
        p2: Int = RustNativeKdf.DEFAULT_T,
        p3: Int = RustNativeKdf.DEFAULT_P
    ): ByteArray {
        if (kdfId == KDF_ARGON2ID) {
            return RustNativeKdf.derivePanicKey(panicPin, salt, pepper, p1, p2, p3)
                ?: throw VaultError.Other("Argon2id native unavailable")
        }
        return derivePanicKeyPbkdf2(panicPin, salt, pepper, p1)
    }

    fun deriveKek(pin: CharArray, salt: ByteArray, pepper: ByteArray, iterations: Int): ByteArray =
        deriveKekPbkdf2(pin, salt, pepper, iterations)

    fun derivePanicKey(panicPin: CharArray, salt: ByteArray, pepper: ByteArray, iterations: Int): ByteArray =
        derivePanicKeyPbkdf2(panicPin, salt, pepper, iterations)

    fun panicMac(panicKey: ByteArray, salt: ByteArray): ByteArray {
        val native = RustNativeKdf.panicMac(panicKey, salt)
        if (native != null) return native
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(panicKey, "HmacSHA256"))
        mac.update(salt)
        mac.update("panic".toByteArray(Charsets.UTF_8))
        return mac.doFinal()
    }

    fun hmacSha256(keyAscii: String, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(keyAscii.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(data)
    }

    fun aeadEncrypt(key: ByteArray, plaintext: ByteArray, aad: ByteArray): ByteArray {
        val nonce = randomBytes(NONCE_LEN)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(aad)
        val ct = cipher.doFinal(plaintext)
        return nonce + ct
    }

    fun aeadDecrypt(key: ByteArray, blob: ByteArray, aad: ByteArray): ByteArray? {
        if (blob.size < NONCE_LEN + 16) return null
        return try {
            val nonce = blob.copyOfRange(0, NONCE_LEN)
            val ct = blob.copyOfRange(NONCE_LEN, blob.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            cipher.updateAAD(aad)
            cipher.doFinal(ct)
        } catch (_: Exception) {
            null
        }
    }

    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        return MessageDigest.isEqual(a, b)
    }
}
