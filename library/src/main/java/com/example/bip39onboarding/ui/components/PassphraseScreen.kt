package com.example.bip39onboarding.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.R
import com.example.bip39onboarding.ui.theme.NeonOutlinedButton
import com.example.bip39onboarding.ui.theme.NeonPrimaryButton
import com.example.bip39onboarding.ui.theme.OnboardingBody
import com.example.bip39onboarding.ui.theme.OnboardingScreenScaffold
import com.example.bip39onboarding.ui.theme.OnboardingTitle

/**
 * Optional BIP39 passphrase (25th word).
 * Empty = no passphrase (standard).
 */
@Composable
fun PassphraseScreen(
    onSkip: () -> Unit,
    onConfirm: (CharArray) -> Unit
) {
    var phrase by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    OnboardingScreenScaffold {
        OnboardingTitle(stringResource(R.string.bip39_passphrase_title))
        Spacer(Modifier.height(8.dp))
        OnboardingBody(stringResource(R.string.bip39_passphrase_body))
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = phrase,
            onValueChange = { phrase = it; error = null },
            label = { Text(stringResource(R.string.bip39_passphrase_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            label = { Text(stringResource(R.string.bip39_passphrase_confirm)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        NeonPrimaryButton(
            text = stringResource(R.string.bip39_continue),
            onClick = {
                when {
                    phrase.isEmpty() -> onSkip()
                    phrase != confirm -> error = "Passphrases do not match"
                    else -> onConfirm(phrase.toCharArray())
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        NeonOutlinedButton(
            text = stringResource(R.string.bip39_passphrase_skip),
            onClick = onSkip
        )
    }
}
