package com.example.bip39onboarding.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** HKDF-SHA256 (RFC 5869). */
object Hkdf {
    private const val HMAC = "HmacSHA256"
    private const val HASH_LEN = 32

    fun extract(salt: ByteArray?, ikm: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC)
        val key = if (salt == null || salt.isEmpty()) ByteArray(HASH_LEN) else salt
        mac.init(SecretKeySpec(key, HMAC))
        return mac.doFinal(ikm)
    }

    fun expand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        require(length > 0) { "length must be > 0" }
        val mac = Mac.getInstance(HMAC)
        mac.init(SecretKeySpec(prk, HMAC))
        val n = (length + HASH_LEN - 1) / HASH_LEN
        require(n <= 255) { "length too large" }
        val out = ByteArray(length)
        var t = ByteArray(0)
        var offset = 0
        for (i in 1..n) {
            mac.reset()
            mac.update(t)
            mac.update(info)
            mac.update(i.toByte())
            t = mac.doFinal()
            val copy = minOf(HASH_LEN, length - offset)
            System.arraycopy(t, 0, out, offset, copy)
            offset += copy
        }
        return out
    }

    fun derive(ikm: ByteArray, salt: ByteArray?, info: ByteArray, length: Int): ByteArray {
        val prk = extract(salt, ikm)
        return try {
            expand(prk, info, length)
        } finally {
            prk.fill(0)
        }
    }
}
