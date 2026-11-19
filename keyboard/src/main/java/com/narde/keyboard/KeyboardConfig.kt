package com.narde.keyboard

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale

/** Режим «одной рукой»: клавиатура прижата к [Left] или [Right] краю, на свободной стороне — мини-панель. */
public enum class OneHandedMode { Off, Left, Right }

/** Тексты для TalkBack (contentDescription). */
@Immutable
public data class KeyboardStrings(
    public val shift: String,
    public val shiftOn: String,
    public val capsLock: String,
    public val backspace: String,
    public val enter: String,
    public val hide: String,
    public val space: String,
    public val symbols: String,
    public val letters: String,
    public val moreSymbols: String,
    public val emoji: String,
    public val language: String,
    public val theme: String,
    public val systemKeyboard: String,
    public val oneHandedSwitch: String,
    public val oneHandedExpand: String,
    public val suggestion: String,
) {
    public companion object {
        public val English: KeyboardStrings = KeyboardStrings(
            shift = "Shift", shiftOn = "Shift on", capsLock = "Caps lock on", backspace = "Backspace",
            enter = "Enter", hide = "Hide keyboard", space = "Space", symbols = "Symbols", letters = "Letters",
            moreSymbols = "More symbols", emoji = "Emoji", language = "Switch language",
            theme = "Change color scheme", systemKeyboard = "Use system keyboard",
            oneHandedSwitch = "Move keyboard to the other side", oneHandedExpand = "Full width keyboard",
            suggestion = "Suggestion",
        )

        public val Russian: KeyboardStrings = KeyboardStrings(
            shift = "Шифт", shiftOn = "Шифт включён", capsLock = "Caps Lock включён", backspace = "Удалить",
            enter = "Ввод", hide = "Скрыть клавиатуру", space = "Пробел", symbols = "Символы", letters = "Буквы",
            moreSymbols = "Другие символы", emoji = "Эмодзи", language = "Сменить язык",
            theme = "Сменить цветовую схему", systemKeyboard = "Системная клавиатура",
            oneHandedSwitch = "Переместить клавиатуру на другую сторону", oneHandedExpand = "Клавиатура на всю ширину",
            suggestion = "Подсказка",
        )

        /** Русские тексты, если язык системы — русский, иначе английские. */
        public fun forSystemLocale(): KeyboardStrings =
            if (Locale.getDefault().language == "ru") Russian else English
    }
}

/** Настройки поведения и внешнего вида. Все значения по умолчанию безопасны. */
@Immutable
public data class KeyboardConfig(
    // --- обратная связь ---
    public val sound: Boolean = true,
    public val haptics: Boolean = true,
    /** «Пузырь» с символом над нажатой клавишей. */
    public val keyPreview: Boolean = true,

    // --- ввод ---
    public val longPressAlternates: Boolean = true,
    public val longPressTimeoutMs: Long = 350L,
    /** Отдельный ряд цифр над буквами. Если выключен — цифры доступны по долгому нажатию на верхний ряд. */
    public val numberRow: Boolean = false,
    /** Подсказки-цифры на верхнем ряду и ввод цифры долгим нажатием (когда [numberRow] = false). */
    public val digitHints: Boolean = true,
    public val backspaceRepeat: Boolean = true,
    /** При долгом удержании ⌫ переходить от символов к словам (шлёт [StdKey.DeleteWord]). */
    public val wordDeleteOnLongHold: Boolean = false,
    /** Свайп по пробелу двигает курсор (шлёт [StdKey.CursorMove]). */
    public val spaceSwipeCursor: Boolean = true,
    /** Двойной тап по ⇧ включает Caps Lock. */
    public val doubleTapShiftForCaps: Boolean = true,

    // --- панель и кнопки ---
    public val showEmojiKey: Boolean = true,
    /** Кнопка 🎨 в панели над клавишами — циклически переключает 8 цветовых схем. */
    public val showThemeKey: Boolean = false,
    /** Кнопка 🌐 — запрос системной клавиатуры (событие [StdKey.SystemKeyboard]). */
    public val showSystemKeyboardKey: Boolean = false,
    /** Кнопка ▼ (событие [StdKey.Hide]). */
    public val showHideKey: Boolean = true,

    // --- приватность и безопасность ---
    /**
     * Режим защищённого ввода: без «пузыря» над клавишей, без подсказок, эмодзи «недавние» не запоминаются.
     * Для паролей, PIN и т.п.
     */
    public val secureMode: Boolean = false,
    /**
     * Игнорировать касания, пока поверх окна есть чужой оверлей (защита от tapjacking).
     * Внимание: флаг ставится на корневой View окна, пока клавиатура на экране. Фильтры «синего света»
     * и подобные оверлеи будут блокировать ввод — поэтому по умолчанию выключено.
     */
    public val secureTouches: Boolean = false,
    /** Не показывать клавиатуру, если подключена аппаратная. */
    public val hideOnHardwareKeyboard: Boolean = false,

    // --- размеры ---
    /** Высота клавиши (без зазора). Масштабируется [heightScale] и системным fontScale. */
    public val keyHeight: Dp = 46.dp,
    /** Видимый зазор между клавишами. Зона касания клавиши включает зазор — «мёртвых» зон нет. */
    public val keyGap: Dp = 5.dp,
    public val cornerRadius: Dp = 8.dp,
    /** Множитель высоты клавиатуры, 0.7..1.5. */
    public val heightScale: Float = 1f,
    /** Добавить navigationBarsPadding() (нужно при edge-to-edge, если хост сам не отступает). */
    public val consumeNavigationBarInsets: Boolean = false,

    public val strings: KeyboardStrings = KeyboardStrings.forSystemLocale(),
)
