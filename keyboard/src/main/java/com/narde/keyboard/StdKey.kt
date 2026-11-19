package com.narde.keyboard

/**
 * Keyboard events delivered to the host via `onKey`.
 * Internal keys (shift, lang, symbols, emoji, theme) are handled inside the library.
 */
public sealed interface StdKey {
    public data class CharKey(public val text: String) : StdKey
    public data object Space : StdKey
    public data object Backspace : StdKey
    public data object DeleteWord : StdKey
    public data object Enter : StdKey
    public data object Hide : StdKey
    public data object SystemKeyboard : StdKey
    public data class CursorMove(public val delta: Int) : StdKey
    public data class Suggestion(public val word: String) : StdKey
}
