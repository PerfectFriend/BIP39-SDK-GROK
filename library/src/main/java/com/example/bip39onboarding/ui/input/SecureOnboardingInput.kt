package com.example.bip39onboarding.ui.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.example.bip39onboarding.api.Bip39Features
import com.example.bip39onboarding.ui.theme.KeyboardThemeBridge
import com.narde.keyboard.KeyboardColorScheme
import com.narde.keyboard.KeyboardConfig
import com.narde.keyboard.NumericKeypad
import com.narde.keyboard.SecureWindowEffect
import com.narde.keyboard.StandardKeyboard
import com.narde.keyboard.StdKey
import com.narde.keyboard.SystemKeyboardGate
import com.narde.keyboard.applyKey
import com.narde.keyboard.rememberKeyboardState

@Composable
fun SecureOnboardingShell(
    secureWindow: Boolean = true,
    blockSystemIme: Boolean = Bip39Features.useInAppKeyboard,
    content: @Composable () -> Unit,
) {
    SecureWindowEffect(enabled = secureWindow)
    SystemKeyboardGate(blockSystemKeyboard = blockSystemIme) {
        content()
    }
}

@Composable
fun SecureSeedKeyboard(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onDone: (() -> Unit)? = null,
    scheme: KeyboardColorScheme? = KeyboardThemeBridge.schemeForCurrentTheme(),
) {
    if (!Bip39Features.useInAppKeyboard) return
    val state = rememberKeyboardState(initialScheme = scheme)
    val config = remember {
        KeyboardConfig(
            secureMode = true,
            showEmojiKey = false,
            showThemeKey = false,
            showSystemKeyboardKey = false,
            keyPreview = false,
            sound = false,
            haptics = true,
            numberRow = false,
        )
    }
    StandardKeyboard(
        onKey = { key ->
            when (key) {
                StdKey.Hide -> Unit
                StdKey.Enter -> onDone?.invoke()
                else -> onValueChange(value.applyKey(key))
            }
        },
        modifier = modifier.fillMaxWidth(),
        state = state,
        config = config,
    )
}

@Composable
fun SecurePinKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onDone: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (!Bip39Features.useInAppKeyboard) return
    NumericKeypad(
        onKey = { key ->
            when (key) {
                is StdKey.CharKey -> {
                    val ch = key.text.firstOrNull()
                    if (ch != null && ch.isDigit()) onDigit(ch)
                }
                StdKey.Backspace -> onBackspace()
                StdKey.Enter -> onDone?.invoke()
                else -> Unit
            }
        },
        modifier = modifier.fillMaxWidth(),
        shuffle = Bip39Features.shufflePinKeypad,
        config = KeyboardConfig(secureMode = true, showEmojiKey = false, sound = false),
    )
}
