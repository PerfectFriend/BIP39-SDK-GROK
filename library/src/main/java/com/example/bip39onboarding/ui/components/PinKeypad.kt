package com.example.bip39onboarding.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.api.Bip39Features
import com.example.bip39onboarding.ui.input.SecurePinKeypad
import com.example.bip39onboarding.ui.theme.NeonOutlinedButton

@Composable
fun PinKeypad(
    length: Int = 6,
    enabled: Boolean = true,
    onComplete: (CharArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val buffer = remember { CharArray(length) }
    val filled = remember { mutableIntStateOf(0) }
    val dots = remember { mutableStateOf("○".repeat(length)) }

    fun refreshDots() {
        dots.value = buildString {
            repeat(filled.intValue) { append('•') }
            repeat(length - filled.intValue) { append('○') }
        }
    }

    fun push(c: Char) {
        if (!enabled) return
        if (filled.intValue >= length) return
        buffer[filled.intValue] = c
        filled.intValue += 1
        refreshDots()
        if (filled.intValue == length) {
            onComplete(buffer.copyOf())
            buffer.fill('\u0000')
            filled.intValue = 0
            refreshDots()
        }
    }

    fun backspace() {
        if (!enabled) return
        if (filled.intValue > 0) {
            filled.intValue -= 1
            buffer[filled.intValue] = '\u0000'
            refreshDots()
        }
    }

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dots.value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        if (Bip39Features.useInAppKeyboard) {
            SecurePinKeypad(
                onDigit = { push(it) },
                onBackspace = { backspace() },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            LegacyPinGrid(enabled = enabled, onDigit = { push(it) }, onBackspace = { backspace() })
        }
    }
}

@Composable
private fun LegacyPinGrid(
    enabled: Boolean,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("⌫", "0", "")
    )
    rows.forEach { row ->
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row.forEach { label ->
                when (label) {
                    "" -> Spacer(Modifier.size(72.dp))
                    "⌫" -> NeonOutlinedButton(
                        text = "⌫",
                        onClick = onBackspace,
                        enabled = enabled,
                        modifier = Modifier.size(72.dp)
                    )
                    else -> NeonOutlinedButton(
                        text = label,
                        onClick = { onDigit(label[0]) },
                        enabled = enabled,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
