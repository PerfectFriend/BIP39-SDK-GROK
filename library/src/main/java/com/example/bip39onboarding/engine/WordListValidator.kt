package com.example.bip39onboarding.engine

/**
 * BIP39 word-list validation and suggestions for restore UI.
 */
object WordListValidator {

    val VALID_COUNTS: Set<Int> = setOf(12, 15, 18, 21, 24)

    sealed class Result {
        data class Ok(val normalized: CharArray, val wordCount: Int) : Result() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Ok) return false
                return wordCount == other.wordCount && normalized.contentEquals(other.normalized)
            }
            override fun hashCode(): Int = 31 * wordCount + normalized.contentHashCode()
        }
        data class UnknownWord(val index: Int, val word: String) : Result()
        data class BadWordCount(val count: Int) : Result()
        data class ChecksumFailed(val wordCount: Int) : Result()
        object Empty : Result()
    }

    fun isValidWord(word: String, languageCode: String = "en"): Boolean {
        val w = word.trim().lowercase()
        if (w.isEmpty()) return false
        return try {
            cash.z.ecc.android.bip39.Mnemonics.getCachedWords(languageCode).contains(w)
        } catch (_: Exception) {
            false
        }
    }

    fun suggest(prefix: String, limit: Int = 5, languageCode: String = "en"): List<String> {
        val p = prefix.trim().lowercase()
        if (p.isEmpty()) return emptyList()
        val list = try {
            cash.z.ecc.android.bip39.Mnemonics.getCachedWords(languageCode)
        } catch (_: Exception) {
            return emptyList()
        }
        return list.asSequence().filter { it.startsWith(p) }.take(limit).toList()
    }

    fun validate(input: String, languageCode: String = "en"): Result {
        val trimmed = input.trim().lowercase().replace(Regex("\\s+"), " ")
        if (trimmed.isEmpty()) return Result.Empty
        val words = trimmed.split(" ")
        if (words.size !in VALID_COUNTS) return Result.BadWordCount(words.size)
        val list = try {
            cash.z.ecc.android.bip39.Mnemonics.getCachedWords(languageCode)
        } catch (_: Exception) {
            return Result.ChecksumFailed(words.size)
        }
        words.forEachIndexed { i, w ->
            if (w !in list) return Result.UnknownWord(i, w)
        }
        val chars = trimmed.toCharArray()
        return if (JvmBip39Core.validate(chars)) {
            Result.Ok(chars, words.size)
        } else {
            Result.ChecksumFailed(words.size)
        }
    }

    fun validate(input: CharArray, languageCode: String = "en"): Result =
        validate(String(input), languageCode)

    fun message(result: Result): String = when (result) {
        is Result.Ok -> "OK"
        is Result.Empty -> "Enter the recovery phrase"
        is Result.BadWordCount -> "Need 12/15/18/21/24 words, got ${'$'}{result.count}"
        is Result.UnknownWord -> "Word ${'$'}{result.index + 1}: unknown «${'$'}{result.word}»"
        is Result.ChecksumFailed -> "Checksum failed — check word order"
    }
}
