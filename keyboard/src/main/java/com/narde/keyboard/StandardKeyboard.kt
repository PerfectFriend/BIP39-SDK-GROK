package com.narde.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private const val DIGITS = "1234567890"

/**
 * Встроенная (in-app) клавиатура. Не использует IME, разрешения, сеть и диск.
 *
 * @param onKey события для хоста (см. [StdKey]); применяйте их через `TextFieldValue.applyKey`,
 *   `TextFieldState.applyKey` или `EditText.applyKey`.
 * @param state состояние (язык, ⇧, страница, схема, одна рука). `rememberKeyboardState()` переживает поворот.
 * @param config поведение и размеры.
 * @param layouts доступные раскладки; можно добавлять свои.
 * @param colors свои цвета; если `null` — берётся схема из [KeyboardState.scheme] (или системная светлая/тёмная).
 * @param suggestions подсказки над клавишами (получите их из [SuggestionProvider]); скрыты в [KeyboardConfig.secureMode].
 * @param autoShift `true`, когда следующая буква должна быть заглавной (см. `TextFieldValue.shouldAutoCapitalize`).
 */
@Composable
public fun StandardKeyboard(
    onKey: (StdKey) -> Unit,
    modifier: Modifier = Modifier,
    state: KeyboardState = rememberKeyboardState(),
    config: KeyboardConfig = KeyboardConfig(),
    layouts: List<KeyboardLayout> = KeyboardLayouts.Default,
    colors: KeyboardColors? = null,
    suggestions: List<String> = emptyList(),
    autoShift: Boolean = false,
) {
    require(layouts.isNotEmpty()) { "layouts must not be empty" }
    val hidden = config.hideOnHardwareKeyboard && rememberHardwareKeyboardPresent()
    if (!hidden) {
        KeyboardContent(onKey, modifier, state, config, layouts, colors, suggestions, autoShift)
    }
}

/** Совместимость с v1: `lang = "RU"` / `"EN"`. Используйте основной вариант с [KeyboardState]. */
@Deprecated(
    "Используйте StandardKeyboard(onKey, state = rememberKeyboardState(\"ru\"))",
    ReplaceWith("StandardKeyboard(onKey = onKey, modifier = modifier, state = rememberKeyboardState(lang.lowercase()))"),
)
@Composable
public fun StandardKeyboard(
    onKey: (StdKey) -> Unit,
    lang: String,
    modifier: Modifier = Modifier,
) {
    StandardKeyboard(onKey = onKey, modifier = modifier, state = rememberKeyboardState(lang.lowercase()))
}

@Composable
private fun KeyboardContent(
    onKey: (StdKey) -> Unit,
    modifier: Modifier,
    state: KeyboardState,
    config: KeyboardConfig,
    layouts: List<KeyboardLayout>,
    colorsOverride: KeyboardColors?,
    suggestions: List<String>,
    autoShift: Boolean,
) {
    val layout = layouts.firstOrNull { it.id == state.layoutId } ?: layouts.first()
    val scheme = state.scheme ?: if (isSystemInDarkTheme()) KeyboardColorScheme.Dark else KeyboardColorScheme.Light
    val palette = colorsOverride ?: scheme.colors

    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.3f)
    val keyHeight = config.keyHeight * config.heightScale.coerceIn(0.7f, 1.5f) * fontScale

    val onKeyState by rememberUpdatedState(onKey)
    val feedback = rememberKeyFeedback(config)
    val env = KeyEnv(palette, config, state, keyHeight, feedback) { onKeyState(it) }

    LaunchedEffect(autoShift) {
        if (autoShift && layout.capitalizes && state.shift == ShiftState.Off && state.page == KeyboardPage.Letters) {
            state.shift = ShiftState.Once
        }
    }
    TouchFilterEffect(config.secureTouches)

    val areaHeight = env.outerHeight * (if (config.numberRow) 5 else 4)
    val oh = state.oneHanded

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.background)
            .then(if (config.consumeNavigationBarInsets) Modifier.navigationBarsPadding() else Modifier)
            .padding(horizontal = 2.dp, vertical = 2.dp),
    ) {
        Toolbar(env, suggestions, layouts)
        Row(Modifier.fillMaxWidth()) {
            if (oh == OneHandedMode.Right) OneHandedRail(env, areaHeight, Modifier.weight(0.16f))
            Column(Modifier.weight(if (oh == OneHandedMode.Off) 1f else 0.84f)) {
                if (state.page == KeyboardPage.Emoji) {
                    EmojiPanel(env, if (config.numberRow) 5 else 4)
                } else {
                    LetterPages(env, layout, layouts)
                }
            }
            if (oh == OneHandedMode.Left) OneHandedRail(env, areaHeight, Modifier.weight(0.16f))
        }
    }
}

// =====================================================================================================
// Панель над клавишами: подсказки + кнопки 🎨 🌐 ▼
// =====================================================================================================

@Composable
private fun Toolbar(env: KeyEnv, suggestions: List<String>, layouts: List<KeyboardLayout>) {
    val cfg = env.config
    val c = env.colors
    val s = cfg.strings
    val shownSuggestions = if (cfg.secureMode) emptyList() else suggestions.take(3)
    val hasButtons = cfg.showThemeKey || cfg.showSystemKeyboardKey || cfg.showHideKey
    if (shownSuggestions.isEmpty() && !hasButtons) return

    val h = env.outerHeight * 0.85f
    Row(Modifier.fillMaxWidth().height(h), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            shownSuggestions.forEachIndexed { i, word ->
                if (i > 0) Box(Modifier.width(1.dp).fillMaxHeight(0.5f).background(c.divider))
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { role = Role.Button; contentDescription = "${s.suggestion}: $word" }
                        .clickable {
                            env.feedback()
                            env.emit(StdKey.Suggestion(word))
                            if (env.state.shift == ShiftState.Once) env.state.shift = ShiftState.Off
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    KeyLabel(word, c.keyContent, sizeSp = 16f)
                }
            }
        }
        if (cfg.showThemeKey) {
            TapKey(env, Modifier.width(44.dp), KeyStyle.Flat, s.theme, "key_theme", { env.state.nextScheme() }, h) {
                KeyIconView(KeyIcon.Palette, c.specialContent)
            }
        }
        if (cfg.showSystemKeyboardKey) {
            TapKey(env, Modifier.width(44.dp), KeyStyle.Flat, s.systemKeyboard, "key_system", { env.emit(StdKey.SystemKeyboard) }, h) {
                KeyIconView(KeyIcon.Globe, c.specialContent)
            }
        }
        if (cfg.showHideKey) {
            TapKey(env, Modifier.width(44.dp), KeyStyle.Flat, s.hide, "key_hide", { env.emit(StdKey.Hide) }, h) {
                KeyIconView(KeyIcon.Hide, c.specialContent)
            }
        }
    }
}

// =====================================================================================================
// Мини-панель режима «одной рукой»
// =====================================================================================================

@Composable
private fun OneHandedRail(env: KeyEnv, areaHeight: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    val s = env.config.strings
    val state = env.state
    val half = areaHeight / 2
    val toRight = state.oneHanded == OneHandedMode.Left // клавиатура слева → «переместить» вправо
    Column(modifier) {
        TapKey(
            env, Modifier.fillMaxWidth(), KeyStyle.Special, s.oneHandedSwitch, "key_onehand_switch",
            { state.oneHanded = if (toRight) OneHandedMode.Right else OneHandedMode.Left },
            half,
        ) { KeyIconView(if (toRight) KeyIcon.ChevronRight else KeyIcon.ChevronLeft, env.colors.specialContent) }
        TapKey(
            env, Modifier.fillMaxWidth(), KeyStyle.Special, s.oneHandedExpand, "key_onehand_expand",
            { state.oneHanded = OneHandedMode.Off },
            half,
        ) { KeyIconView(KeyIcon.Expand, env.colors.specialContent) }
    }
}

// =====================================================================================================
// Буквы и символы
// =====================================================================================================

/** Ряд с «полями» по бокам в [padUnits] единиц ширины клавиши (выравнивание коротких рядов по центру). */
@Composable
private fun KeyRowFrame(padUnits: Float, content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        if (padUnits > 0f) Spacer(Modifier.weight(padUnits))
        content()
        if (padUnits > 0f) Spacer(Modifier.weight(padUnits))
    }
}

@Composable
private fun LetterPages(env: KeyEnv, layout: KeyboardLayout, layouts: List<KeyboardLayout>) {
    val state = env.state
    val cfg = env.config
    val c = env.colors
    val s = cfg.strings
    val page = state.page
    val isLetters = page == KeyboardPage.Letters

    val rows: List<List<KeySpec>> = when (page) {
        KeyboardPage.Letters -> layout.rows
        KeyboardPage.Symbols1 -> SymbolPages.page1
        else -> SymbolPages.page2
    }
    val units = rows[0].size.toFloat()
    val useHints = isLetters && !cfg.numberRow && cfg.digitHints

    // ряд цифр (на символьных страницах цифры уже есть — оставляем пустое место, чтобы высота не «прыгала»)
    if (!isLetters && cfg.numberRow) Spacer(Modifier.height(env.outerHeight))
    if (isLetters && cfg.numberRow) {
        KeyRowFrame((units - 10f).coerceAtLeast(0f) / 2f) {
            SymbolPages.digits.forEach { d -> CharKeyButton(env, KeySpec(d), Modifier.weight(1f)) }
        }
    }

    // ряд 1
    KeyRowFrame(0f) {
        rows[0].forEachIndexed { i, spec ->
            val digit = if (useHints && i < DIGITS.length) DIGITS[i].toString() else null
            CharKeyButton(env, spec, Modifier.weight(1f), hint = digit, extraAlternates = listOfNotNull(digit))
        }
    }

    // ряд 2
    KeyRowFrame((units - rows[1].size) / 2f) {
        rows[1].forEach { spec -> CharKeyButton(env, spec, Modifier.weight(1f)) }
    }

    // ряд 3: слева ⇧ (буквы) или переключатель страниц (символы), справа ⌫
    val side = ((units - rows[2].size) / 2f).coerceAtLeast(1f)
    KeyRowFrame(0f) {
        if (isLetters) {
            ShiftKey(env, Modifier.weight(side))
        } else {
            val toPage2 = page == KeyboardPage.Symbols1
            TapKey(
                env, Modifier.weight(side), KeyStyle.Special, s.moreSymbols, "key_page",
                { state.page = if (toPage2) KeyboardPage.Symbols2 else KeyboardPage.Symbols1 },
            ) { KeyLabel(if (toPage2) "=\\<" else "?123", c.specialContent, sizeSp = 14f, bold = true) }
        }
        rows[2].forEach { spec -> CharKeyButton(env, spec, Modifier.weight(1f)) }
        BackspaceKey(env, Modifier.weight(side))
    }

    // нижний ряд: ?123 | язык | , | 🙂 | пробел | . | ⏎
    val showLang = isLetters && layouts.size > 1
    val fixed = 1.5f + 1f + 1f + 1.5f + (if (showLang) 1.2f else 0f) + (if (cfg.showEmojiKey) 1.1f else 0f)
    val spaceWeight = 11f - fixed
    KeyRowFrame(0f) {
        TapKey(
            env, Modifier.weight(1.5f), KeyStyle.Special,
            if (isLetters) s.symbols else s.letters, "key_mode",
            { state.page = if (isLetters) KeyboardPage.Symbols1 else KeyboardPage.Letters },
        ) { KeyLabel(if (isLetters) "?123" else "ABC", c.specialContent, sizeSp = 14f, bold = true) }

        if (showLang) {
            TapKey(
                env, Modifier.weight(1.2f), KeyStyle.Special, s.language, "key_lang",
                { state.nextLayout(layouts) },
            ) { KeyLabel(layout.label, c.specialContent, sizeSp = 14f, bold = true) }
        }

        CharKeyButton(env, KeySpec(","), Modifier.weight(1f))

        if (cfg.showEmojiKey) {
            TapKey(
                env, Modifier.weight(1.1f), KeyStyle.Special, s.emoji, "key_emoji",
                { state.page = KeyboardPage.Emoji },
            ) { KeyIconView(KeyIcon.Emoji, c.specialContent) }
        }

        SpaceKey(env, Modifier.weight(spaceWeight), layout.spaceLabel)

        CharKeyButton(env, KeySpec(".", SymbolPages.periodAlternates), Modifier.weight(1f))

        TapKey(
            env, Modifier.weight(1.5f), KeyStyle.Accent, s.enter, "key_enter",
            { env.emit(StdKey.Enter) },
        ) { KeyIconView(KeyIcon.Enter, c.accentContent) }
    }
}

@Composable
private fun ShiftKey(env: KeyEnv, modifier: Modifier) {
    val state = env.state
    val s = env.config.strings
    val shift = state.shift
    val style = if (shift == ShiftState.Off) KeyStyle.Special else KeyStyle.Accent
    val icon = when (shift) {
        ShiftState.Off -> KeyIcon.ShiftOff
        ShiftState.Once -> KeyIcon.ShiftOn
        ShiftState.CapsLock -> KeyIcon.CapsLock
    }
    val description = when (shift) {
        ShiftState.Off -> s.shift
        ShiftState.Once -> s.shiftOn
        ShiftState.CapsLock -> s.capsLock
    }
    TapKey(
        env, modifier, style, description, "key_shift",
        { state.onShiftTap(System.currentTimeMillis(), env.config.doubleTapShiftForCaps) },
    ) { KeyIconView(icon, env.contentColor(style)) }
}
