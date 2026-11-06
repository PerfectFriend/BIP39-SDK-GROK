package com.example.bip39onboarding.ui.unlock

import com.example.bip39onboarding.engine.MnemonicEngines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.bip39onboarding.R
import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.data.UnlockResult
import com.example.bip39onboarding.security.BiometricHelper
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.DeviceIntegrity
import com.example.bip39onboarding.security.PinLockout
import com.example.bip39onboarding.security.PinUnlock
import com.example.bip39onboarding.security.SecureSeedStorage
import com.example.bip39onboarding.security.SeedStorage
import com.example.bip39onboarding.security.EspSeedStorageAdapter
import com.example.bip39onboarding.ui.theme.NeonOutlinedButton
import com.example.bip39onboarding.ui.theme.NeonPrimaryButton
import com.example.bip39onboarding.ui.theme.OnboardingScreenScaffold
import com.example.bip39onboarding.ui.theme.OnboardingTitle
import com.example.bip39onboarding.ui.theme.OnboardingBody
import kotlinx.coroutines.delay

/**
 * Daily unlock screen with exponential lockout, optional biometrics, integrity warning.
 */
@Composable
fun PinUnlockScreen(
    storage: SecureSeedStorage,
    onSuccess: (SeedPayload) -> Unit,
    onPanic: () -> Unit,
    fingerprintHint: String? = storage.getFingerprint(),
    enableBiometric: Boolean = true,
    showIntegrityWarning: Boolean = true
) {
    PinUnlockScreen(
        storage = EspSeedStorageAdapter(storage),
        onSuccess = onSuccess,
        onPanic = onPanic,
        fingerprintHint = fingerprintHint,
        enableBiometric = enableBiometric,
        showIntegrityWarning = showIntegrityWarning
    )
}

@Composable
fun PinUnlockScreen(
    storage: SeedStorage,
    onSuccess: (SeedPayload) -> Unit,
    onPanic: () -> Unit,
    fingerprintHint: String? = storage.getFingerprint(),
    enableBiometric: Boolean = true,
    showIntegrityWarning: Boolean = true
) {
    MnemonicEngines.ensureInitialized()
    val context = LocalContext.current
    val lockout = remember { PinLockout(context) }
    val integrity = remember { if (showIntegrityWarning) DeviceIntegrity.assess(context) else null }

    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var remainingMs by remember { mutableLongStateOf(lockout.remainingLockMs()) }
    var biometricHint by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(remainingMs) {
        while (remainingMs > 0) {
            delay(250)
            remainingMs = lockout.remainingLockMs()
        }
    }

    val locked = remainingMs > 0
    val canBio = enableBiometric && BiometricHelper.canAuthenticate(context)

    var pendingMnemonic by remember { mutableStateOf<CharArray?>(null) }
    var passphrase by remember { mutableStateOf("") }
    val needsPassphrase = storage.hasPassphrase()

    fun emitSuccess(mnemonic: CharArray, pp: CharArray) {
        val fp = storage.getFingerprint() ?: Bip39Manager.fingerprint(mnemonic)
        val wc = storage.getWordCount()
        onSuccess(
            SeedPayload(
                mnemonic = mnemonic,
                fingerprint = fp,
                wordCount = wc,
                passphrase = pp,
                hasPassphrase = needsPassphrase
            )
        )
        pin = ""
        passphrase = ""
        pendingMnemonic = null
    }

    fun doUnlock(chars: CharArray) {
        when (val result = PinUnlock.tryUnlock(chars, storage)) {
            is UnlockResult.Success -> {
                lockout.registerSuccess()
                pin = ""
                if (needsPassphrase) {
                    pendingMnemonic = result.mnemonic
                } else {
                    emitSuccess(result.mnemonic, charArrayOf())
                }
            }
            UnlockResult.Panic -> {
                lockout.registerSuccess()
                Bip39Manager.clear(chars)
                pin = ""
                onPanic()
            }
            UnlockResult.WrongPin -> {
                lockout.registerFailure()
                remainingMs = lockout.remainingLockMs()
                error = context.getString(R.string.bip39_wrong_pin)
                pin = ""
            }
            else -> {
                error = context.getString(R.string.bip39_wrong_pin)
                pin = ""
            }
        }
        Bip39Manager.clear(chars)
    }

    OnboardingScreenScaffold {
        OnboardingTitle(stringResource(R.string.bip39_unlock_title))
        if (!fingerprintHint.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.bip39_fingerprint, fingerprintHint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (storage.hasPassphrase()) {
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.bip39_passphrase_active_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (integrity != null && integrity.warnings.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(
                    stringResource(R.string.bip39_integrity_warning),
                    Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (integrity?.keystoreBackend == DeviceIntegrity.KeystoreBackend.SOFTWARE ||
            integrity?.keystoreBackend == DeviceIntegrity.KeystoreBackend.UNKNOWN
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.bip39_keystore_weak_warning),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 8 && it.all(Char::isDigit)) {
                    pin = it
                    error = null
                }
            },
            label = { Text(stringResource(R.string.bip39_pin_label)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            enabled = !locked,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        if (biometricHint != null) {
            Spacer(Modifier.height(8.dp))
            Text(biometricHint!!, color = MaterialTheme.colorScheme.error)
        }
        if (locked) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.bip39_locked_out, (remainingMs / 1000).toInt().coerceAtLeast(1)),
                color = MaterialTheme.colorScheme.error
            )
        }

        if (pendingMnemonic != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.bip39_unlock_passphrase_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                label = { Text(stringResource(R.string.bip39_passphrase_label)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            NeonPrimaryButton(
                text = stringResource(R.string.bip39_continue),
                onClick = {
                    val m = pendingMnemonic ?: return@NeonPrimaryButton
                    emitSuccess(m, passphrase.toCharArray())
                }
            )
        } else {
        Spacer(Modifier.height(24.dp))
        NeonPrimaryButton(
            text = stringResource(R.string.bip39_unlock),
            onClick = {
                if (locked || pin.length < 6) return@NeonPrimaryButton
                doUnlock(pin.toCharArray())
            },
            enabled = !locked && pin.length >= 6
        )

        if (canBio && !locked) {
            Spacer(Modifier.height(12.dp))
            NeonOutlinedButton(
                text = stringResource(R.string.bip39_biometric_button),
                onClick = {
                    val activity = context as? FragmentActivity
                    if (activity == null) {
                        biometricHint = "Biometric requires FragmentActivity host"
                        return@NeonOutlinedButton
                    }
                    BiometricHelper.authenticateWithCrypto(
                        activity = activity,
                        title = context.getString(R.string.bip39_biometric_title),
                        subtitle = context.getString(R.string.bip39_biometric_subtitle),
                        negativeButton = context.getString(R.string.bip39_back),
                        onSuccess = {
                            biometricHint = context.getString(R.string.bip39_biometric_enter_pin)
                        },
                        onError = { biometricHint = it },
                        onFailed = {
                            biometricHint = context.getString(R.string.bip39_wrong_pin)
                        }
                    )
                }
            )
        }
        } // end else !pendingMnemonic
    }
}
