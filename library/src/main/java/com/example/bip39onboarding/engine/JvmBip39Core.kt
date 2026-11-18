package com.example.bip39onboarding.engine

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.Mnemonics.MnemonicCode
import cash.z.ecc.android.bip39.Mnemonics.WordCount
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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

    /**
     * Fingerprint v1: first 4 bytes of HMAC-SHA256("bip39-sdk/fingerprint/v1", seed) as hex.
     * Not a prefix of the raw seed (audit P0-6).
     */
    fun fingerprint(mnemonic: CharArray): String {
        val seed = toSeed(mnemonic)
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec("bip39-sdk/fingerprint/v1".toByteArray(Charsets.UTF_8), "HmacSHA256"))
            val out = mac.doFinal(seed)
            out.take(4).joinToString("") { "%02x".format(it) }.also { out.fill(0) }
        } finally {
            seed.fill(0)
        }
    }

    fun pickRandomPositions(wordCount: Int): List<Int> {
        return (0 until wordCount).shuffled(secureRandom).take(3).sorted()
    }

    /**
     * Quiz distractors: prefer words with same 2–3 letter prefix (harder than pure random).
     * Audit P1-13 — blind random 3-of-5 was ~2–3% pass rate by chance over 3 attempts.
     */
    fun generateOptions(correctWord: String, languageCode: String = "en"): List<String> {
        val wordList = Mnemonics.getCachedWords(languageCode)
        val options = linkedSetOf(correctWord)
        val prefix3 = correctWord.take(3)
        val prefix2 = correctWord.take(2)
        val similar = wordList.filter {
            it != correctWord && (it.startsWith(prefix3) || it.startsWith(prefix2))
        }.shuffled(secureRandom)
        for (w in similar) {
            if (options.size >= 5) break
            options.add(w)
        }
        while (options.size < 5) {
            options.add(wordList.random(secureRandom))
        }
        return options.shuffled(secureRandom)
    }

    /** BIP39 entropy bytes (16 or 32) from a validated mnemonic. */
    fun mnemonicToEntropy(mnemonic: CharArray, languageCode: String = "en"): ByteArray {
        val words = String(mnemonic).trim().lowercase().split(Regex("\\s+"))
        require(words.size in setOf(12, 15, 18, 21, 24)) { "bad word count" }
        val wordList = Mnemonics.getCachedWords(languageCode)
        val indexByWord = HashMap<String, Int>(wordList.size)
        wordList.forEachIndexed { i, w -> indexByWord[w] = i }
        val bits = StringBuilder(words.size * 11)
        for (w in words) {
            val idx = indexByWord[w] ?: throw IllegalArgumentException("unknown word")
            bits.append(idx.toString(2).padStart(11, '0'))
        }
        val entBits = words.size * 32 / 3 // 128 for 12, 256 for 24
        val entropy = ByteArray(entBits / 8)
        for (i in entropy.indices) {
            var b = 0
            for (j in 0 until 8) {
                if (bits[i * 8 + j] == '1') b = b or (1 shl (7 - j))
            }
            entropy[i] = b.toByte()
        }
        MnemonicCode(entropy).validate()
        return entropy
    }

    fun entropyToMnemonic(entropy: ByteArray): CharArray =
        MnemonicCode(entropy).chars

    fun clear(data: CharArray?) {
        data?.fill('\u0000')
    }

    fun clear(data: ByteArray?) {
        data?.fill(0)
    }
}
