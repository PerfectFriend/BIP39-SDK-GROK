package com.narde.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

@Composable
public fun NumericKeypad(
    onKey: (StdKey) -> Unit,
    modifier: Modifier = Modifier,
    shuffle: Boolean = true,
    colors: KeyboardColors? = null,
    config: KeyboardConfig = KeyboardConfig(secureMode = true, showEmojiKey = false),
) {
    val scheme = if (isSystemInDarkTheme()) KeyboardColorScheme.Dark else KeyboardColorScheme.Light
    val palette = colors ?: scheme.colors
    val fontScale = LocalDensity.current.fontScale.coerceIn(1f, 1.3f)
    val keyHeight = config.keyHeight * config.heightScale.coerceIn(0.7f, 1.5f) * fontScale * 1.15f
    val onKeyState by rememberUpdatedState(onKey)
    val feedback = rememberKeyFeedback(config)
    val state = remember { KeyboardState() }
    val env = KeyEnv(palette, config, state, keyHeight, feedback) { onKeyState(it) }
    val s = config.strings

    val digits: List<String> = remember(shuffle) {
        val base = SymbolPages.digits
        if (shuffle) base.shuffled(SecureRandom().asKotlinRandom()) else base
    }
    val grid: List<String> = digits.take(9)
    val last: String = digits[9]

    Column(
        modifier.fillMaxWidth().background(palette.background).padding(2.dp),
    ) {
        for (r in 0 until 3) {
            Row(Modifier.fillMaxWidth()) {
                for (cIdx in 0 until 3) {
                    val d = grid[r * 3 + cIdx]
                    CharKeyButton(env, KeySpec(d), Modifier.weight(1f))
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            TapKey(
                env, Modifier.weight(1f), KeyStyle.Accent, s.enter, "key_enter",
                { env.emit(StdKey.Enter) },
            ) { KeyIconView(KeyIcon.Enter, palette.accentContent) }
            CharKeyButton(env, KeySpec(last), Modifier.weight(1f))
            BackspaceKey(env, Modifier.weight(1f))
        }
    }
}
