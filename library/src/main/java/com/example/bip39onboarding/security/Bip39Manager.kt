package com.example.bip39onboarding.security

import com.example.bip39onboarding.engine.JvmBip39Core
import com.example.bip39onboarding.engine.MnemonicEngine
import com.example.bip39onboarding.engine.MnemonicEngines

/**
 * BIP39 façade for the app and library UI.
 *
 * Crypto (generate / validate / toSeed / fingerprint) is delegated to
 * [MnemonicEngines.current] — **Rust by default** when `libbip39_engine_ffi.so`
 * is present, otherwise JVM ([JvmBip39Core]).
 *
 * Quiz helpers (wordlist options) stay on JVM.
 */
object Bip39Manager {

    enum class Strength {
        WORDS_12,
        WORDS_24;

        val count: Int
            get() = when (this) {
                WORDS_12 -> 12
                WORDS_24 -> 24
            }

        fun toEngine(): MnemonicEngine.Strength = when (this) {
            WORDS_12 -> MnemonicEngine.Strength.WORDS_12
            WORDS_24 -> MnemonicEngine.Strength.WORDS_24
        }
    }

    private fun engine(): MnemonicEngine {
        MnemonicEngines.ensureInitialized()
        return MnemonicEngines.current
    }

    /**
     * Generates a mnemonic of the given strength.
     * @return CharArray (caller must [clear])
     */
    fun generate(strength: Strength = Strength.WORDS_12): CharArray {
        return engine().generate(strength.toEngine())
    }

    fun validate(mnemonic: CharArray): Boolean = engine().validate(mnemonic)

    fun validate(mnemonic: String): Boolean = validate(mnemonic.toCharArray())

    /** BIP39 seed (64 bytes). */
    fun toSeed(mnemonic: CharArray, passphrase: CharArray = charArrayOf()): ByteArray {
        return engine().toSeed(mnemonic, passphrase)
    }

    /** First 8 bytes of seed as hex (UI fingerprint). */
    fun fingerprint(mnemonic: CharArray): String = engine().fingerprint(mnemonic)

    /** 3 random positions for the verification quiz (JVM). */
    fun pickRandomPositions(wordCount: Int): List<Int> =
        JvmBip39Core.pickRandomPositions(wordCount)

    /** 5 answer options for quiz (JVM wordlist). */
    fun generateOptions(correctWord: String, languageCode: String = "en"): List<String> =
        JvmBip39Core.generateOptions(correctWord, languageCode)

    fun clear(data: CharArray?) {
        engine().clear(data)
    }

    fun clear(data: ByteArray?) {
        engine().clear(data)
    }

    /** Which backend is active after init. */
    fun activeBackend(): MnemonicEngines.Backend {
        MnemonicEngines.ensureInitialized()
        return MnemonicEngines.backend
    }
}
