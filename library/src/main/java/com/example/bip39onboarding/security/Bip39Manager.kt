package com.example.bip39onboarding.security

import com.example.bip39onboarding.engine.JvmBip39Core
import com.example.bip39onboarding.engine.WordListValidator
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

        companion object {
            fun fromWordCount(n: Int): Strength? = when (n) {
                12, 15, 18 -> WORDS_12
                21, 24 -> WORDS_24
                else -> null
            }

            fun isValidWordCount(n: Int): Boolean = n in setOf(12, 15, 18, 21, 24)
        }
    }

    private fun engine(): MnemonicEngine {
        MnemonicEngines.ensureInitialized()
        return MnemonicEngines.current
    }

    fun generate(strength: Strength = Strength.WORDS_12): CharArray {
        return engine().generate(strength.toEngine())
    }

    fun validate(mnemonic: CharArray): Boolean = engine().validate(mnemonic)

    fun validate(mnemonic: String): Boolean = validate(mnemonic.toCharArray())

    fun validateDetailed(input: String, languageCode: String = "en"): WordListValidator.Result =
        WordListValidator.validate(input, languageCode)

    fun validateDetailed(input: CharArray, languageCode: String = "en"): WordListValidator.Result =
        WordListValidator.validate(input, languageCode)

    fun suggestWords(prefix: String, limit: Int = 5): List<String> =
        WordListValidator.suggest(prefix, limit = limit)

    fun toSeed(mnemonic: CharArray, passphrase: CharArray = charArrayOf()): ByteArray {
        return engine().toSeed(mnemonic, passphrase)
    }

    fun fingerprint(mnemonic: CharArray): String = engine().fingerprint(mnemonic)

    fun pickRandomPositions(wordCount: Int): List<Int> =
        JvmBip39Core.pickRandomPositions(wordCount)

    fun generateOptions(correctWord: String, languageCode: String = "en"): List<String> =
        JvmBip39Core.generateOptions(correctWord, languageCode)

    fun mnemonicToEntropy(mnemonic: CharArray): ByteArray =
        JvmBip39Core.mnemonicToEntropy(mnemonic)

    fun entropyToMnemonic(entropy: ByteArray): CharArray =
        JvmBip39Core.entropyToMnemonic(entropy)

    fun clear(data: CharArray?) {
        engine().clear(data)
    }

    fun clear(data: ByteArray?) {
        engine().clear(data)
    }

    fun activeBackend(): MnemonicEngines.Backend {
        MnemonicEngines.ensureInitialized()
        return MnemonicEngines.backend
    }
}
