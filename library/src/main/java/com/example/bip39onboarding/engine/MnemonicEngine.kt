package com.example.bip39onboarding.engine

/**
 * Portable mnemonic crypto surface.
 *
 * **Production path:** [RustMnemonicEngine] when `libbip39_engine_ffi.so` is packaged.
 * **Fallback:** [KotlinMnemonicEngine] ([JvmBip39Core]).
 */
interface MnemonicEngine {
    enum class Strength { WORDS_12, WORDS_24 }

    fun generate(strength: Strength = Strength.WORDS_12): CharArray
    fun validate(mnemonic: CharArray): Boolean
    fun toSeed(mnemonic: CharArray, passphrase: CharArray = charArrayOf()): ByteArray
    fun fingerprint(mnemonic: CharArray): String
    fun clear(data: CharArray?)
    fun clear(data: ByteArray?)
}

/** JVM fallback — [JvmBip39Core] only (no recursion into [Bip39Manager]). */
class KotlinMnemonicEngine : MnemonicEngine {
    override fun generate(strength: MnemonicEngine.Strength): CharArray {
        val s = when (strength) {
            MnemonicEngine.Strength.WORDS_12 -> JvmBip39Core.Strength.WORDS_12
            MnemonicEngine.Strength.WORDS_24 -> JvmBip39Core.Strength.WORDS_24
        }
        return JvmBip39Core.generate(s)
    }

    override fun validate(mnemonic: CharArray): Boolean = JvmBip39Core.validate(mnemonic)

    override fun toSeed(mnemonic: CharArray, passphrase: CharArray): ByteArray =
        JvmBip39Core.toSeed(mnemonic, passphrase)

    override fun fingerprint(mnemonic: CharArray): String = JvmBip39Core.fingerprint(mnemonic)

    override fun clear(data: CharArray?) = JvmBip39Core.clear(data)
    override fun clear(data: ByteArray?) = JvmBip39Core.clear(data)
}

/**
 * Native engine — `libbip39_engine_ffi.so` (Rust `bip39-engine`).
 */
class RustMnemonicEngine private constructor() : MnemonicEngine {

    override fun generate(strength: MnemonicEngine.Strength): CharArray {
        val n = when (strength) {
            MnemonicEngine.Strength.WORDS_12 -> 12
            MnemonicEngine.Strength.WORDS_24 -> 24
        }
        val s = nativeGenerate(n)
            ?: throw IllegalStateException("nativeGenerate returned null")
        return s.toCharArray()
    }

    override fun validate(mnemonic: CharArray): Boolean =
        nativeValidate(String(mnemonic)) == 1

    override fun toSeed(mnemonic: CharArray, passphrase: CharArray): ByteArray {
        return nativeToSeed(String(mnemonic), String(passphrase))
            ?: throw IllegalArgumentException("invalid mnemonic")
    }

    override fun fingerprint(mnemonic: CharArray): String =
        nativeFingerprint(String(mnemonic))
            ?: throw IllegalArgumentException("invalid mnemonic")

    override fun clear(data: CharArray?) {
        data?.fill('\u0000')
    }

    override fun clear(data: ByteArray?) {
        data?.fill(0)
    }

    private external fun nativeGenerate(wordCount: Int): String?
    private external fun nativeValidate(phrase: String): Int
    private external fun nativeToSeed(phrase: String, passphrase: String): ByteArray?
    private external fun nativeFingerprint(phrase: String): String?

    companion object {
        @JvmStatic
        var nativeAvailable: Boolean = false
            private set

        @JvmStatic
        fun createOrNull(): RustMnemonicEngine? {
            if (!tryLoadNative()) return null
            return try {
                RustMnemonicEngine()
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        fun tryLoadNative(): Boolean {
            if (nativeAvailable) return true
            return try {
                System.loadLibrary("bip39_engine_ffi")
                nativeAvailable = true
                true
            } catch (_: UnsatisfiedLinkError) {
                nativeAvailable = false
                false
            }
        }
    }
}

/**
 * Process-wide engine. Prefers Rust after [preferRust].
 */
object MnemonicEngines {
    @Volatile
    private var initialized = false

    @Volatile
    var current: MnemonicEngine = KotlinMnemonicEngine()
        private set

    /** Backend currently selected. */
    @Volatile
    var backend: Backend = Backend.KOTLIN
        private set

    enum class Backend { RUST, KOTLIN }

    /**
     * Prefer Rust native library; fall back to Kotlin JVM.
     * Safe to call multiple times. Invoked automatically from [ensureInitialized].
     */
    @JvmStatic
    fun preferRust(): Boolean {
        val rust = RustMnemonicEngine.createOrNull()
        return if (rust != null) {
            current = rust
            backend = Backend.RUST
            initialized = true
            true
        } else {
            current = KotlinMnemonicEngine()
            backend = Backend.KOTLIN
            initialized = true
            false
        }
    }

    /** Force JVM engine (tests / no native). */
    @JvmStatic
    fun useKotlin() {
        current = KotlinMnemonicEngine()
        backend = Backend.KOTLIN
        initialized = true
    }

    /** @deprecated use [preferRust] */
    @JvmStatic
    fun useRustIfAvailable(): Boolean = preferRust()

    /** Idempotent bootstrap — Rust first when packaged. */
    @JvmStatic
    fun ensureInitialized() {
        if (!initialized) {
            preferRust()
        }
    }
}
