package com.example.bip39onboarding.engine

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import java.security.SecureRandom

/**
 * Pure JVM BIP39 implementation (zcash kotlin-bip39).
 * Used only by [KotlinMnemonicEngine] — not by app code.
 */
internal object JvmBip39Core {
    private val secureRandom = SecureRandom()

    enum class Strength(val wordCount: WordCount) {
        WORDS_12(WordCount.COUNT_12),
        WORDS_24(WordCount.COUNT_24);

        val count: Int get() = wordCount.count
    }

    fun generate(strength: Strength = Strength.WORDS_12): CharArray {
        val entropy = ByteArray(strength.wordCount.bitLength / 8)
        secureRandom.nextBytes(entropy)
        return MnemonicCode(entropy).chars
    }

    fun validate(mnemonic: CharArray): Boolean {
        return try {
            MnemonicCode(mnemonic).validate()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun toSeed(mnemonic: CharArray, passphrase: CharArray = charArrayOf()): ByteArray {
        return MnemonicCode(mnemonic).toSeed(passphrase)
    }

    fun fingerprint(mnemonic: CharArray): String {
        val seed = toSeed(mnemonic)
        val hex = seed.take(8).joinToString("") { "%02x".format(it) }
        seed.fill(0)
        return hex
    }

    fun pickRandomPositions(wordCount: Int): List<Int> {
        return (0 until wordCount).shuffled(secureRandom).take(3).sorted()
    }

    fun generateOptions(correctWord: String, languageCode: String = "en"): List<String> {
        val wordList = Mnemonics.getCachedWords(languageCode)
        val options = mutableSetOf(correctWord)
        while (options.size < 5) {
            options.add(wordList.random(secureRandom))
        }
        return options.shuffled(secureRandom)
    }

    fun clear(data: CharArray?) {
        data?.fill('\u0000')
    }

    fun clear(data: ByteArray?) {
        data?.fill(0)
    }
}
