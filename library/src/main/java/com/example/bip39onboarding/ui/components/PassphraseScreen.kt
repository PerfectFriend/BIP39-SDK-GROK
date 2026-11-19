package com.example.bip39onboarding.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.R
import com.example.bip39onboarding.api.Bip39Features
import com.example.bip39onboarding.ui.input.SecureOnboardingShell
import com.example.bip39onboarding.ui.input.SecureSeedKeyboard
import com.example.bip39onboarding.ui.theme.NeonPrimaryButton
import com.example.bip39onboarding.ui.theme.OnboardingBody
import com.example.bip39onboarding.ui.theme.OnboardingScreenScaffold
import com.example.bip39onboarding.ui.theme.OnboardingTitle

/**
 * Optional BIP39 passphrase (25th word).
 * Uses in-app keyboard when [Bip39Features.useInAppKeyboard].
 */
@Composable
fun PassphraseScreen(
    onSkip: () -> Unit,
    onConfirm: (CharArray) -> Unit
) {
    var phrase by remember { mutableStateOf(TextFieldValue()) }
    var confirm by remember { mutableStateOf(TextFieldValue()) }
    var editingConfirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val useKbd = Bip39Features.useInAppKeyboard

    SecureOnboardingShell {
        OnboardingScreenScaffold {
            OnboardingTitle(stringResource(R.string.bip39_passphrase_title))
            Spacer(Modifier.height(8.dp))
            OnboardingBody(stringResource(R.string.bip39_passphrase_body))
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = phrase,
                onValueChange = { if (!useKbd) { phrase = it; error = null } },
                label = { Text(stringResource(R.string.bip39_passphrase_label)) },
                visualTransformation = PasswordVisualTransformation(),
                readOnly = useKbd,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { if (!useKbd) { confirm = it; error = null } },
                label = { Text(stringResource(R.string.bip39_passphrase_confirm)) },
                visualTransformation = PasswordVisualTransformation(),
                readOnly = useKbd,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (useKbd) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (editingConfirm) "Confirm passphrase" else "Enter passphrase",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SecureSeedKeyboard(
                    value = if (editingConfirm) confirm else phrase,
                    onValueChange = {
                        if (editingConfirm) confirm = it else phrase = it
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            NeonPrimaryButton(
                onClick = {
                    if (phrase.text != confirm.text) {
                        error = "Passphrases do not match"
                        return@NeonPrimaryButton
                    }
                    onConfirm(phrase.text.toCharArray())
                },
                text = stringResource(R.string.bip39_continue),
                enabled = phrase.text.isNotEmpty()
            )
            Spacer(Modifier.height(12.dp))
            NeonPrimaryButton(
                onClick = onSkip,
                text = stringResource(R.string.bip39_skip_passphrase),
                enabled = true
            )
        }
    }
}
