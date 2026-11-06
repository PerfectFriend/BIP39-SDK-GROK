package com.example.bip39onboarding.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bip39onboarding.data.OnboardingStep
import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.security.Bip39Manager
import com.example.bip39onboarding.security.EspSeedStorageAdapter
import com.example.bip39onboarding.security.SecureSeedStorage
import com.example.bip39onboarding.security.SeedStorage
import com.example.bip39onboarding.ui.components.PassphraseScreen
import com.example.bip39onboarding.api.OnboardingHost
import com.example.bip39onboarding.engine.MnemonicEngines
import com.example.bip39onboarding.api.NoOpOnboardingHost
import com.example.bip39onboarding.ui.theme.LocalOnboardingThemeConfig
import com.example.bip39onboarding.ui.theme.OnboardingTheme
import com.example.bip39onboarding.ui.theme.OnboardingThemeConfig
import com.example.bip39onboarding.ui.welcome.WelcomeAboardScreen
import com.example.bip39onboarding.ui.components.StepProgress
import com.example.bip39onboarding.ui.restore.RestoreChooseScreen
import com.example.bip39onboarding.ui.restore.RestorePasteScreen
import com.example.bip39onboarding.ui.restore.RestorePdfScreen
import com.example.bip39onboarding.ui.restore.RestoreQrScreen

@Composable
fun OnboardingNavHost(
    storage: SecureSeedStorage,
    onComplete: (SeedPayload) -> Unit,
    onWipe: () -> Unit = {},
    host: OnboardingHost = NoOpOnboardingHost,
    themeConfig: OnboardingThemeConfig = OnboardingThemeConfig()
) {
    OnboardingNavHost(
        storage = EspSeedStorageAdapter(storage),
        onComplete = onComplete,
        onWipe = onWipe,
        host = host,
        themeConfig = themeConfig
    )
}

@Composable
fun OnboardingNavHost(
    storage: SeedStorage,
    onComplete: (SeedPayload) -> Unit,
    onWipe: () -> Unit = {},
    host: OnboardingHost = NoOpOnboardingHost,
    themeConfig: OnboardingThemeConfig = OnboardingThemeConfig()
) {
    val context = LocalContext.current
    MnemonicEngines.ensureInitialized()
    val deliverComplete: (SeedPayload) -> Unit = { payload ->
        host.onAnalyticsEvent("onboarding_complete")
        host.onOnboardingComplete(payload)
        onComplete(payload)
    }
    val deliverWipe: () -> Unit = {
        host.onAnalyticsEvent("vault_wiped")
        host.onVaultWiped()
        onWipe()
    }
    val vm: OnboardingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return OnboardingViewModel(storage) as T
            }
        }
    )

    var tempPin by remember { mutableStateOf<CharArray?>(null) }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.goTo(OnboardingStep.RESTORE_QR)
    }

    OnboardingTheme(config = themeConfig) {
    Column(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp)) {
        val showProgress = LocalOnboardingThemeConfig.current.showStepProgress
        if (showProgress && vm.step != OnboardingStep.SPLASH) {
            StepProgress(vm.step)
        }

        when (vm.step) {
            OnboardingStep.SPLASH -> WelcomeAboardScreen(
                onCreate = vm::chooseCreate,
                onRestore = vm::chooseRestore
            )

            OnboardingStep.CHOOSE -> ChooseScreen(
                onCreate = vm::chooseCreate,
                onRestore = vm::chooseRestore
            )

            OnboardingStep.STRENGTH -> StrengthScreen(onSelect = vm::setStrength)

            OnboardingStep.DISCLAIMER -> DisclaimerScreen(
                accepted = vm.acceptedDisclaimer,
                onAcceptedChange = { vm.acceptedDisclaimer = it },
                onContinue = vm::acceptDisclaimerAndShow
            )

            OnboardingStep.SHOW_MNEMONIC -> {
                val m = vm.mnemonic
                if (m != null) {
                    ShowMnemonicScreen(
                        mnemonic = m,
                        fingerprint = Bip39Manager.fingerprint(m),
                        onContinue = vm::prepareVerification
                    )
                }
            }

            OnboardingStep.VERIFY -> VerifyMnemonicScreen(
                positions = vm.verifyPositions,
                options = vm.verifyOptions,
                error = vm.errorMessage,
                onCheck = { selected -> vm.checkVerification(selected) }
            )

            OnboardingStep.SET_PASSPHRASE -> PassphraseScreen(
                onSkip = { vm.skipPassphrase() },
                onConfirm = { pp -> vm.setPassphrase(pp) }
            )

            OnboardingStep.SET_PIN -> SetPinScreen(
                titleRes = true,
                isPanic = false,
                errorMessage = vm.errorMessage,
                onComplete = { pin ->
                    tempPin = pin
                    vm.goTo(OnboardingStep.SET_PANIC_PIN)
                }
            )

            OnboardingStep.SET_PANIC_PIN -> SetPinScreen(
                titleRes = false,
                isPanic = true,
                errorMessage = vm.errorMessage,
                onComplete = { panic ->
                    val pin = tempPin
                    if (pin != null) {
                        val payload = vm.completeSetup(pin, panic)
                        Bip39Manager.clear(tempPin)
                        tempPin = null
                        if (payload != null) deliverComplete(payload)
                    }
                }
            )

            OnboardingStep.RESTORE_CHOOSE -> RestoreChooseScreen(
                onPaste = { vm.goTo(OnboardingStep.RESTORE_PASTE) },
                onQr = {
                    val ok = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (ok) vm.goTo(OnboardingStep.RESTORE_QR)
                    else cameraPermission.launch(Manifest.permission.CAMERA)
                },
                onPdf = { vm.goTo(OnboardingStep.RESTORE_PDF) },
                onBack = { vm.goTo(OnboardingStep.CHOOSE) }
            )

            OnboardingStep.RESTORE_PASTE -> RestorePasteScreen(
                onValidMnemonic = { chars -> vm.startRestoreFromMnemonic(chars) },
                onBack = { vm.goTo(OnboardingStep.RESTORE_CHOOSE) }
            )

            OnboardingStep.RESTORE_QR -> RestoreQrScreen(
                onValidMnemonic = { chars -> vm.startRestoreFromMnemonic(chars) },
                onBack = { vm.goTo(OnboardingStep.RESTORE_CHOOSE) }
            )

            OnboardingStep.RESTORE_PDF -> RestorePdfScreen(
                onValidMnemonic = { chars -> vm.startRestoreFromMnemonic(chars) },
                onBack = { vm.goTo(OnboardingStep.RESTORE_CHOOSE) }
            )

            OnboardingStep.DONE -> { }
        }
    }
    } // OnboardingTheme
}
