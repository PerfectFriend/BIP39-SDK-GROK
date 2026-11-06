package com.example.bip39onboarding.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
// TextButton via material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.R
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.CryptoUtils
import com.example.bip39onboarding.ui.components.ClipboardHelper
import com.example.bip39onboarding.ui.components.PdfExporter
import com.example.bip39onboarding.ui.components.SecureScreen
import com.example.bip39onboarding.ui.theme.NeonOutlinedButton
import com.example.bip39onboarding.ui.theme.NeonPrimaryButton
import com.example.bip39onboarding.ui.theme.LocalOnboardingColors
import com.example.bip39onboarding.ui.theme.OnboardingBody
import com.example.bip39onboarding.ui.theme.OnboardingScreenScaffold
import com.example.bip39onboarding.ui.theme.OnboardingTitle

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.bip39_app_name), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.bip39_splash_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bip39_start))
        }
    }
}

@Composable
fun ChooseScreen(onCreate: () -> Unit, onRestore: () -> Unit) {
    OnboardingScreenScaffold {
        OnboardingTitle(stringResource(R.string.bip39_choose_title))
        Spacer(Modifier.height(32.dp))
        NeonPrimaryButton(stringResource(R.string.bip39_create_account), onClick = onCreate)
        Spacer(Modifier.height(16.dp))
        NeonOutlinedButton(stringResource(R.string.bip39_restore_account), onClick = onRestore)
    }
}

@Composable
fun StrengthScreen(onSelect: (Bip39Manager.Strength) -> Unit) {
    OnboardingScreenScaffold {
        OnboardingTitle(stringResource(R.string.bip39_strength_title))
        Spacer(Modifier.height(12.dp))
        OnboardingBody(stringResource(R.string.bip39_strength_hint))
        Spacer(Modifier.height(32.dp))
        NeonPrimaryButton(stringResource(R.string.bip39_words_12), onClick = { onSelect(Bip39Manager.Strength.WORDS_12) })
        Spacer(Modifier.height(12.dp))
        NeonOutlinedButton(stringResource(R.string.bip39_words_24), onClick = { onSelect(Bip39Manager.Strength.WORDS_24) })
    }
}

@Composable
fun DisclaimerScreen(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenScaffold(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
        OnboardingTitle(stringResource(R.string.bip39_disclaimer_title))
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Text(
                stringResource(R.string.bip39_disclaimer_body),
                Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = accepted, onCheckedChange = onAcceptedChange)
            Text(stringResource(R.string.bip39_disclaimer_checkbox), color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(32.dp))
        NeonPrimaryButton(
            text = stringResource(R.string.bip39_continue),
            onClick = onContinue,
            enabled = accepted
        )
        }
    }
}

@Composable
fun ShowMnemonicScreen(
    mnemonic: CharArray,
    fingerprint: String,
    onContinue: () -> Unit
) {
    SecureScreen { captureWarning ->
        val words = remember(mnemonic) { String(mnemonic).split(" ") }
        var visible by remember { mutableStateOf(true) }
        val context = LocalContext.current

        val colors = LocalOnboardingColors.current
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingTitle(stringResource(R.string.bip39_write_down_title))
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.bip39_screenshots_blocked),
                color = colors.danger,
                style = MaterialTheme.typography.bodySmall
            )
            if (captureWarning) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.bip39_screen_capture_warning),
                    color = colors.danger,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                stringResource(R.string.bip39_fingerprint, fingerprint),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )

            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.heightIn(max = 420.dp),
                userScrollEnabled = false
            ) {
                itemsIndexed(words) { index, word ->
                    val shard = colors.mosaicPalette[index % colors.mosaicPalette.size]
                    Card(
                        Modifier.padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.panelElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, shard.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = if (visible) "${index + 1}. $word" else "${index + 1}. ••••••",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.bip39_paper_first_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.strokeNeon
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonOutlinedButton(
                    text = stringResource(if (visible) R.string.bip39_hide else R.string.bip39_show),
                    onClick = { visible = !visible },
                    modifier = Modifier.weight(1f)
                )
                NeonOutlinedButton(
                    text = stringResource(R.string.bip39_copy_all),
                    onClick = { ClipboardHelper.copySensitive(context, String(mnemonic)) },
                    modifier = Modifier.weight(1f),
                    borderColor = colors.strokeNeon.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(12.dp))
            var showAdvanced by remember { mutableStateOf(false) }
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(stringResource(R.string.bip39_advanced_backup), color = colors.textMuted)
            }
            if (showAdvanced) {
                Text(
                    stringResource(R.string.bip39_pdf_advanced_warning),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.danger
                )
                Spacer(Modifier.height(8.dp))
                NeonOutlinedButton(
                    text = stringResource(R.string.bip39_export_pdf),
                    onClick = { PdfExporter.export(context, mnemonic, fingerprint) }
                )
            }

            Spacer(Modifier.height(32.dp))
            NeonPrimaryButton(
                text = stringResource(R.string.bip39_i_wrote_it),
                onClick = onContinue
            )
        }
    }
}

@Composable
fun VerifyMnemonicScreen(
    positions: List<Int>,
    options: Map<Int, List<String>>,
    error: String?,
    onCheck: (Map<Int, String>) -> Unit
) {
    var selected by remember { mutableStateOf(mapOf<Int, String>()) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.bip39_verify_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.bip39_verify_subtitle))
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        positions.forEach { pos ->
            Spacer(Modifier.height(20.dp))
            Text("№${pos + 1}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            options[pos]?.chunked(3)?.forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { word ->
                        FilterChip(
                            selected = selected[pos] == word,
                            onClick = { selected = selected + (pos to word) },
                            label = { Text(word) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onCheck(selected) },
            enabled = selected.size == positions.size,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.bip39_check))
        }
    }
}

@Composable
fun SetPinScreen(
    titleRes: Boolean,
    isPanic: Boolean,
    errorMessage: String? = null,
    onComplete: (CharArray) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) error = errorMessage
    }

    OnboardingScreenScaffold {
        OnboardingTitle(
            stringResource(if (isPanic) R.string.bip39_set_panic_title else R.string.bip39_set_pin_title)
        )
        Spacer(Modifier.height(8.dp))
        OnboardingBody(
            stringResource(if (isPanic) R.string.bip39_set_panic_subtitle else R.string.bip39_set_pin_subtitle)
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) pin = it },
            label = { Text(stringResource(R.string.bip39_pin_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) confirm = it },
            label = { Text(stringResource(R.string.bip39_pin_confirm)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        val errMin = stringResource(R.string.bip39_pin_min)
        val errMismatch = stringResource(R.string.bip39_pin_mismatch)
        val errWeak = stringResource(R.string.bip39_pin_weak)

        NeonPrimaryButton(
            text = stringResource(R.string.bip39_continue),
            onClick = {
                when {
                    pin.length < 6 -> error = errMin
                    pin != confirm -> error = errMismatch
                    !isPanic && CryptoUtils.isWeakPin(pin) -> error = errWeak
                    else -> {
                        error = null
                        onComplete(pin.toCharArray())
                    }
                }
            },
            enabled = pin.isNotEmpty() && confirm.isNotEmpty()
        )
    }
}
