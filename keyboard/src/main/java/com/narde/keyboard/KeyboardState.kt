package com.narde.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

public enum class ShiftState { Off, Once, CapsLock }

public enum class KeyboardPage { Letters, Symbols1, Symbols2, Emoji }

@Stable
public class KeyboardState(
    initialLayoutId: String = "ru",
    initialScheme: KeyboardColorScheme? = null,
    initialOneHanded: OneHandedMode = OneHandedMode.Off,
    initialRecentEmoji: List<String> = emptyList(),
) {
    public var layoutId: String by mutableStateOf(initialLayoutId)
    public var scheme: KeyboardColorScheme? by mutableStateOf(initialScheme)
    public var oneHanded: OneHandedMode by mutableStateOf(initialOneHanded)
    public var recentEmoji: List<String> by mutableStateOf(initialRecentEmoji)
    public var page: KeyboardPage by mutableStateOf(KeyboardPage.Letters)
    public var shift: ShiftState by mutableStateOf(ShiftState.Off)
    internal var lastShiftTapMs: Long = 0L

    public fun nextLayout(layouts: List<KeyboardLayout>) {
        if (layouts.isEmpty()) return
        val i = layouts.indexOfFirst { it.id == layoutId }
        layoutId = layouts[(i + 1).mod(layouts.size)].id
    }

    public fun nextScheme() {
        val all = KeyboardColorScheme.entries
        val current = scheme
        scheme = if (current == null) all.first() else all.getOrNull(current.ordinal + 1)
    }

    public fun addRecentEmoji(emoji: String, max: Int = 32) {
        recentEmoji = (listOf(emoji) + recentEmoji.filter { it != emoji }).take(max)
    }

    internal fun onShiftTap(nowMs: Long, doubleTapForCaps: Boolean) {
        shift = nextShiftState(shift, nowMs - lastShiftTapMs, doubleTapForCaps)
        lastShiftTapMs = nowMs
    }

    public companion object {
        public val Saver: Saver<KeyboardState, Any> = listSaver(
            save = {
                listOf(it.layoutId, it.scheme?.name ?: "", it.oneHanded.name, ArrayList(it.recentEmoji))
            },
            restore = { v ->
                KeyboardState(
                    initialLayoutId = v[0] as String,
                    initialScheme = (v[1] as String).let { n -> KeyboardColorScheme.entries.firstOrNull { it.name == n } },
                    initialOneHanded = OneHandedMode.valueOf(v[2] as String),
                    initialRecentEmoji = (v[3] as List<*>).filterIsInstance<String>(),
                )
            },
        )
    }
}

@Composable
public fun rememberKeyboardState(
    initialLayoutId: String = "ru",
    initialScheme: KeyboardColorScheme? = null,
    initialOneHanded: OneHandedMode = OneHandedMode.Off,
    initialRecentEmoji: List<String> = emptyList(),
): KeyboardState = rememberSaveable(saver = KeyboardState.Saver) {
    KeyboardState(initialLayoutId, initialScheme, initialOneHanded, initialRecentEmoji)
}

internal const val DOUBLE_TAP_MS: Long = 350L

internal fun nextShiftState(current: ShiftState, msSinceLastTap: Long, doubleTapForCaps: Boolean): ShiftState =
    when (current) {
        ShiftState.Off -> ShiftState.Once
        ShiftState.Once ->
            if (doubleTapForCaps && msSinceLastTap in 0..DOUBLE_TAP_MS) ShiftState.CapsLock else ShiftState.Off
        ShiftState.CapsLock -> ShiftState.Off
    }
