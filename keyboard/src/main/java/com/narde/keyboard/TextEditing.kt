package com.narde.keyboard

import java.text.BreakIterator

/**
 * Результат применения клавиши к тексту: замена диапазона [replaceStart, replaceEnd) на [replacement]
 * и новое выделение [selStart, selEnd]. [text] — полный текст после правки.
 */
public data class TextEdit(
    public val text: String,
    public val selStart: Int,
    public val selEnd: Int,
    public val replaceStart: Int,
    public val replaceEnd: Int,
    public val replacement: String,
)

/**
 * Чистая (без Compose/Android) логика применения клавиш к тексту.
 * Работает с графемными кластерами: ⌫ не ломает эмодзи и составные символы (суррогатные пары, e + ◌́).
 */
public object TextEditing {

    /**
     * Применить [key] к тексту с выделением [selStart]..[selEnd].
     * @return правка или `null`, если клавиша не редактирует текст ([StdKey.Enter] без [insertNewlineOnEnter],
     * [StdKey.Hide], [StdKey.SystemKeyboard]).
     */
    public fun apply(
        text: String,
        selStart: Int,
        selEnd: Int,
        key: StdKey,
        insertNewlineOnEnter: Boolean = false,
    ): TextEdit? {
        val s = minOf(selStart, selEnd).coerceIn(0, text.length)
        val e = maxOf(selStart, selEnd).coerceIn(0, text.length)
        return when (key) {
            is StdKey.CharKey -> replace(text, s, e, key.text)
            StdKey.Space -> replace(text, s, e, " ")
            StdKey.Enter -> if (insertNewlineOnEnter) replace(text, s, e, "\n") else null
            StdKey.Backspace ->
                if (s != e) replace(text, s, e, "")
                else if (s == 0) unchanged(text, 0, 0)
                else replace(text, previousBoundary(text, s), s, "")
            StdKey.DeleteWord ->
                if (s != e) replace(text, s, e, "")
                else if (s == 0) unchanged(text, 0, 0)
                else replace(text, wordStartBefore(text, s), s, "")
            is StdKey.CursorMove -> {
                var pos = if (key.delta < 0) s else e
                repeat(kotlin.math.abs(key.delta)) {
                    pos = if (key.delta < 0) previousBoundary(text, pos) else nextBoundary(text, pos)
                }
                unchanged(text, pos, pos)
            }
            is StdKey.Suggestion ->
                if (s != e) replace(text, s, e, key.word + " ")
                else replace(text, currentWordStart(text, s), s, key.word + " ")
            StdKey.Hide, StdKey.SystemKeyboard -> null
        }
    }

    /** Слово слева от курсора (буквы, цифры, апостроф, дефис внутри слова). Пустая строка, если курсор не в слове. */
    public fun currentWord(text: String, cursor: Int): String {
        val c = cursor.coerceIn(0, text.length)
        return text.substring(currentWordStart(text, c), c)
    }

    /**
     * Нужна ли заглавная буква в позиции [cursor]: начало текста, начало строки или
     * после «. ! ? …» и хотя бы одного пробела. Передавайте результат в `StandardKeyboard(autoShift = ...)`.
     */
    public fun shouldAutoCapitalize(text: String, cursor: Int): Boolean {
        val before = text.substring(0, cursor.coerceIn(0, text.length))
        if (before.isBlank()) return true
        if (before.last() == '\n') return true
        val trimmed = before.trimEnd()
        if (trimmed.length == before.length) return false
        return trimmed.last() in ".!?…"
    }

    // ---------------------------------------------------------------- internals

    private fun replace(text: String, start: Int, end: Int, replacement: String): TextEdit {
        val newText = text.substring(0, start) + replacement + text.substring(end)
        val cursor = start + replacement.length
        return TextEdit(newText, cursor, cursor, start, end, replacement)
    }

    private fun unchanged(text: String, selStart: Int, selEnd: Int): TextEdit =
        TextEdit(text, selStart, selEnd, selStart, selStart, "")

    private const val WINDOW = 64

    /** Начало графемного кластера, заканчивающегося в [offset] (offset > 0). */
    internal fun previousBoundary(text: String, offset: Int): Int {
        if (offset <= 0) return 0
        val from = maxOf(0, offset - WINDOW)
        val window = text.substring(from, offset)
        val it = BreakIterator.getCharacterInstance()
        it.setText(window)
        val p = it.preceding(window.length)
        return if (p == BreakIterator.DONE) from else from + p
    }

    /** Конец графемного кластера, начинающегося в [offset]. */
    internal fun nextBoundary(text: String, offset: Int): Int {
        if (offset >= text.length) return text.length
        val to = minOf(text.length, offset + WINDOW)
        val window = text.substring(offset, to)
        val it = BreakIterator.getCharacterInstance()
        it.setText(window)
        val p = it.following(0)
        return if (p == BreakIterator.DONE) to else offset + p
    }

    /** Начало «слова» для Ctrl+⌫: пропустить пробелы слева, затем слово или один знак/эмодзи. */
    internal fun wordStartBefore(text: String, offset: Int): Int {
        var i = offset
        while (i > 0 && Character.isWhitespace(text.codePointBefore(i))) i -= Character.charCount(text.codePointBefore(i))
        if (i == 0) return 0
        return if (isWordCodePoint(text.codePointBefore(i))) {
            while (i > 0 && isWordCodePoint(text.codePointBefore(i))) i -= Character.charCount(text.codePointBefore(i))
            i
        } else {
            previousBoundary(text, i)
        }
    }

    internal fun currentWordStart(text: String, cursor: Int): Int {
        var i = cursor
        while (i > 0 && isWordCodePoint(text.codePointBefore(i))) i -= Character.charCount(text.codePointBefore(i))
        return i
    }

    private fun isWordCodePoint(cp: Int): Boolean =
        Character.isLetterOrDigit(cp) || cp == '\''.code || cp == '’'.code || cp == '-'.code || cp == '_'.code
}
