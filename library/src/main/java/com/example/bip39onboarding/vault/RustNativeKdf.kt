package com.example.bip39onboarding.vault

import com.example.bip39onboarding.engine.RustMnemonicEngine

/**
 * JNI façade for Rust Argon2id KDF (vault kdf_id = 1).
 * Shares `libbip39_engine_ffi.so` with [RustMnemonicEngine].
 */
object RustNativeKdf {

    val isAvailable: Boolean
        get() = RustMnemonicEngine.tryLoadNative()

    /** Default production params — matches VAULT_FORMAT / bip39_engine::kdf */
    const val DEFAULT_M_KIB = 32_768
    const val DEFAULT_T = 3
    const val DEFAULT_P = 1
    const val MIN_M_KIB = 32_768
    const val MIN_T = 2

    private external fun nativeArgon2idIkm(
        pin: ByteArray,
        salt: ByteArray,
        mKib: Int,
        t: Int,
        p: Int
    ): ByteArray?

    private external fun nativeDeriveKek(
        pin: ByteArray,
        salt: ByteArray,
        pepper: ByteArray,
        mKib: Int,
        t: Int,
        p: Int
    ): ByteArray?

    private external fun nativeDerivePanicKey(
        pin: ByteArray,
        salt: ByteArray,
        pepper: ByteArray,
        mKib: Int,
        t: Int,
        p: Int
    ): ByteArray?

    private external fun nativePanicMac(panicKey: ByteArray, salt: ByteArray): ByteArray?

    fun pinToUtf8(pin: CharArray): ByteArray =
        String(pin).toByteArray(Charsets.UTF_8)

    fun deriveKek(
        pin: CharArray,
        salt: ByteArray,
        pepper: ByteArray,
        mKib: Int = DEFAULT_M_KIB,
        t: Int = DEFAULT_T,
        p: Int = DEFAULT_P
    ): ByteArray? {
        if (!isAvailable) return null
        val pinBytes = pinToUtf8(pin)
        return try {
            nativeDeriveKek(pinBytes, salt, pepper, mKib, t, p)
        } finally {
            pinBytes.fill(0)
        }
    }

    fun derivePanicKey(
        pin: CharArray,
        salt: ByteArray,
        pepper: ByteArray,
        mKib: Int = DEFAULT_M_KIB,
        t: Int = DEFAULT_T,
        p: Int = DEFAULT_P
    ): ByteArray? {
        if (!isAvailable) return null
        val pinBytes = pinToUtf8(pin)
        return try {
            nativeDerivePanicKey(pinBytes, salt, pepper, mKib, t, p)
        } finally {
            pinBytes.fill(0)
        }
    }

    fun panicMac(panicKey: ByteArray, salt: ByteArray): ByteArray? {
        if (!isAvailable) return null
        return nativePanicMac(panicKey, salt)
    }

    /**
     * Smoke-test native KDF after [System.loadLibrary].
     * Returns false on any link/runtime failure so callers can fall back.
     */
    fun selfTest(): Boolean {
        if (!isAvailable) return false
        return try {
            val pin = "847291".toCharArray()
            val salt = ByteArray(16) { it.toByte() }
            val pepper = ByteArray(32) { (it * 3).toByte() }
            try {
                val kek = deriveKek(pin, salt, pepper, mKib = DEFAULT_M_KIB, t = DEFAULT_T, p = DEFAULT_P)
                    ?: return false
                val ok = kek.size == 32
                kek.fill(0)
                ok
            } finally {
                pin.fill('\u0000')
                salt.fill(0)
                pepper.fill(0)
            }
        } catch (_: Throwable) {
            false
        }
    }
}
