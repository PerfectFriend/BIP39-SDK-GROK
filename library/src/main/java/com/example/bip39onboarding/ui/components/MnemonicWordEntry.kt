package com.example.bip39onboarding.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.api.Bip39Features
import com.example.bip39onboarding.engine.WordListValidator
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.ui.input.SecureSeedKeyboard
import com.example.bip39onboarding.ui.theme.NeonPrimaryButton

/**
 * Per-word BIP39 entry with wordlist autocomplete.
 * When [Bip39Features.useInAppKeyboard], system IME is not used — [SecureSeedKeyboard] edits the focused slot.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MnemonicWordEntry(
    wordCount: Int = 12,
    onValidMnemonic: (CharArray) -> Unit,
    modifier: Modifier = Modifier,
    enabledCounts: Set<Int> = WordListValidator.VALID_COUNTS
) {
    var count by remember { mutableStateOf(wordCount) }
    var words by remember(count) { mutableStateOf(List(count) { "" }) }
    var focused by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    val useKbd = Bip39Features.useInAppKeyboard

    fun currentPhrase(): String =
        words.joinToString(" ") { it.trim().lowercase() }.replace(Regex("\\s+"), " ").trim()

    val focusedValue = TextFieldValue(
        text = words.getOrElse(focused) { "" },
        selection = TextRange(words.getOrElse(focused) { "" }.length)
    )

    Column(modifier.fillMaxWidth()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            enabledCounts.sorted().forEach { n ->
                val selected = count == n
                SuggestionChip(
                    onClick = {
                        count = n
                        words = List(n) { idx -> words.getOrElse(idx) { "" } }
                        focused = focused.coerceIn(0, n - 1)
                        error = null
                    },
                    label = {
                        Text(
                            "$n",
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        words.forEachIndexed { index, value ->
            val suggestions = if (focused == index && value.length >= 2) {
                Bip39Manager.suggestWords(value.lowercase(), limit = 5)
            } else emptyList()

            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    if (useKbd) return@OutlinedTextField
                    val cleaned = input.lowercase().filter { it.isLetter() }
                    words = words.toMutableList().also { it[index] = cleaned }
                    focused = index
                    error = null
                    if (cleaned.length >= 3 && WordListValidator.isValidWord(cleaned) && index < count - 1) {
                        focused = index + 1
                    }
                },
                label = { Text("Word ${index + 1}") },
                singleLine = true,
                readOnly = useKbd,
                isError = value.isNotEmpty() && value.length >= 3 &&
                    !WordListValidator.isValidWord(value.lowercase()),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = if (index == count - 1) ImeAction.Done else ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { focused = index },
            )

            if (suggestions.isNotEmpty() && !useKbd) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEach { s ->
                        SuggestionChip(
                            onClick = {
                                words = words.toMutableList().also { it[index] = s }
                                if (index < count - 1) focused = index + 1
                            },
                            label = { Text(s) }
                        )
                    }
                }
            }
        }

        if (useKbd) {
            Spacer(Modifier.height(8.dp))
            SecureSeedKeyboard(
                value = focusedValue,
                onValueChange = { tfv ->
                    val cleaned = tfv.text.lowercase().filter { it.isLetter() }
                    words = words.toMutableList().also { it[focused] = cleaned }
                    if (cleaned.length >= 3 && WordListValidator.isValidWord(cleaned) && focused < count - 1) {
                        focused = focused + 1
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        NeonPrimaryButton(
            onClick = {
                val phrase = currentPhrase()
                val arr = phrase.toCharArray()
                if (WordListValidator.isValidMnemonic(arr)) {
                    onValidMnemonic(arr)
                } else {
                    error = "Invalid mnemonic"
                    Bip39Manager.clear(arr)
                }
            },
            text = "Continue",
            enabled = words.all { it.isNotBlank() }
        )
    }
}
