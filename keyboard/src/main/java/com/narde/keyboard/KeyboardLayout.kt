package com.narde.keyboard

/** Одна буквенная клавиша: [label] — базовый символ, [alternates] — варианты по долгому нажатию. */
public data class KeySpec(
    public val label: String,
    public val alternates: List<String> = emptyList(),
)

/**
 * Языковая раскладка: ровно 3 буквенных ряда.
 * Ряд 3 дополняется слева ⇧ и справа ⌫ — их ширина подбирается автоматически под ширину ряда 1.
 *
 * Свою раскладку можно описать целиком, не форкая библиотеку:
 * ```
 * val kk = KeyboardLayout("kk", "KK", "Қазақша", listOf(...))
 * StandardKeyboard(layouts = KeyboardLayouts.Default + kk, ...)
 * ```
 */
public data class KeyboardLayout(
    public val id: String,
    /** Надпись на клавише переключения языка. */
    public val label: String,
    /** Надпись на пробеле. */
    public val spaceLabel: String,
    public val rows: List<List<KeySpec>>,
    /** Поддерживает ли язык заглавные (для автокапитализации). */
    public val capitalizes: Boolean = true,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(rows.size == 3) { "KeyboardLayout '$id' must have exactly 3 letter rows, got ${rows.size}" }
        require(rows.all { it.isNotEmpty() }) { "KeyboardLayout '$id' has an empty row" }
        require(rows[0].size >= rows[1].size && rows[0].size >= rows[2].size) {
            "KeyboardLayout '$id': row 1 must be the widest"
        }
    }
}

/** Встроенные раскладки. */
public object KeyboardLayouts {

    /** ЙЦУКЕН. `ё` — по долгому нажатию на `е`. */
    public val Russian: KeyboardLayout = KeyboardLayout(
        id = "ru",
        label = "RU",
        spaceLabel = "Русский",
        rows = listOf(
            row("й ц у к е н г ш щ з х ъ", "е" to "ё"),
            row("ф ы в а п р о л д ж э"),
            row("я ч с м и т ь б ю"),
        ),
    )

    public val English: KeyboardLayout = KeyboardLayout(
        id = "en",
        label = "EN",
        spaceLabel = "English",
        rows = listOf(
            row(
                "q w e r t y u i o p",
                "e" to "è é ê ë ē ę", "y" to "ý ÿ", "u" to "ù ú û ü ū", "i" to "ì í î ï ī", "o" to "ò ó ô ö õ ø œ ō",
            ),
            row("a s d f g h j k l", "a" to "à á â ä æ ã å ā", "s" to "ß ś š", "l" to "ł"),
            row("z x c v b n m", "z" to "ž ź ż", "c" to "ç ć č", "n" to "ñ ń"),
        ),
    )

    /** Украинская. `ґ` — на `г`, апостроф — на `ь`. */
    public val Ukrainian: KeyboardLayout = KeyboardLayout(
        id = "uk",
        label = "UK",
        spaceLabel = "Українська",
        rows = listOf(
            row("й ц у к е н г ш щ з х ї", "г" to "ґ"),
            row("ф і в а п р о л д ж є"),
            row("я ч с м и т ь б ю", "ь" to "'"),
        ),
    )

    /** QWERTZ с умлаутами в основных рядах. `ß` — на `s`. */
    public val German: KeyboardLayout = KeyboardLayout(
        id = "de",
        label = "DE",
        spaceLabel = "Deutsch",
        rows = listOf(
            row("q w e r t z u i o p ü", "e" to "é è ê", "u" to "ú ù û"),
            row("a s d f g h j k l ö ä", "a" to "à á â", "s" to "ß"),
            row("y x c v b n m", "c" to "ç", "n" to "ñ"),
        ),
    )

    /** Раскладки по умолчанию (как в v1): RU + EN. */
    public val Default: List<KeyboardLayout> = listOf(Russian, English)

    /** Все встроенные раскладки. */
    public val All: List<KeyboardLayout> = listOf(Russian, English, Ukrainian, German)

    private fun row(letters: String, vararg alternates: Pair<String, String>): List<KeySpec> {
        val alt = alternates.associate { (k, v) -> k to v.split(' ') }
        return letters.split(' ').map { KeySpec(it, alt[it].orEmpty()) }
    }
}

/** Две страницы символов (?123 → =\<). */
internal object SymbolPages {
    val page1: List<List<KeySpec>> = pageOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
        listOf("*", "\"", "'", ":", ";", "!", "?"),
    )

    val page2: List<List<KeySpec>> = pageOf(
        listOf("[", "]", "{", "}", "<", ">", "^", "~", "=", "`"),
        listOf("\\", "|", "%", "€", "£", "¥", "°", "№", "§", "©"),
        listOf("…", "«", "»", "“", "”", "–", "—"),
    )

    val digits: List<String> = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    /** Варианты для «.» по долгому нажатию. */
    val periodAlternates: List<String> = listOf("?", "!", ":", ";", "…")

    private fun pageOf(vararg rows: List<String>): List<List<KeySpec>> =
        rows.map { r -> r.map { KeySpec(it) } }
}
