package com.example.sample

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.api.OnboardingHost
import com.example.bip39onboarding.data.SeedPayload
import com.example.bip39onboarding.security.RememberSessionLockEffect
import com.example.bip39onboarding.security.SeedStorage
import com.example.bip39onboarding.security.SessionLock
import com.example.bip39onboarding.security.TinkSeedStorage
import com.example.bip39onboarding.security.TinkSeedStorageAdapter
import com.example.bip39onboarding.ui.onboarding.OnboardingNavHost
import com.example.bip39onboarding.ui.theme.OnboardingTheme
import com.example.bip39onboarding.ui.theme.OnboardingThemePresets
import com.example.bip39onboarding.ui.unlock.PinUnlockScreen
import com.example.bip39onboarding.engine.MnemonicEngines

/**
 * Sample host: Phase B theme config + OnboardingHost + session lock.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tink = TinkSeedStorage(applicationContext).also { it.migrateFromLegacyIfNeeded() }
        val storage: SeedStorage = TinkSeedStorageAdapter(tink)
        val rustOn = MnemonicEngines.preferRust()
        android.util.Log.i("Bip39Sample", "MnemonicEngine backend=${MnemonicEngines.backend} rust=$rustOn")

        setContent {
            var unlocked by remember { mutableStateOf<SeedPayload?>(null) }
            val session = remember { SessionLock() }

            val host = remember {
                object : OnboardingHost {
                    override fun onOnboardingComplete(payload: SeedPayload) {
                        // Sample keeps payload for demo HomeScreen; production should
                        // derive keys then payload.clear() immediately.
                    }

                    override fun onVaultWiped() {
                        unlocked?.clear()
                        unlocked = null
                    }

                    override fun onAnalyticsEvent(name: String, properties: Map<String, String>) {
                        // Debug only — never log secrets
                        android.util.Log.d("Bip39Sample", "event=$name $properties")
                    }
                }
            }

            OnboardingTheme(config = OnboardingThemePresets.config("neon_night")) {
                RememberSessionLockEffect(
                    session = session,
                    enabled = unlocked != null,
                    onLock = {
                        unlocked?.clear()
                        unlocked = null
                    }
                )

                when {
                    !storage.isSetup() -> {
                        OnboardingNavHost(
                            storage = storage,
                            onComplete = {
                                session.touch()
                                unlocked = it
                            },
                            onWipe = { restartApp() },
                            host = host
                        )
                    }
                    unlocked == null -> {
                        PinUnlockScreen(
                            storage = storage,
                            onSuccess = {
                                session.touch()
                                unlocked = it
                            },
                            onPanic = {
                                host.onVaultWiped()
                                restartApp()
                            },
                            enableBiometric = true,
                            showIntegrityWarning = true
                        )
                    }
                    else -> {
                        HomeScreen(
                            fingerprint = unlocked!!.fingerprint,
                            hasPassphrase = unlocked!!.hasPassphrase,
                            onUserAction = { session.touch() },
                            onLock = {
                                unlocked?.clear()
                                unlocked = null
                            }
                        )
                    }
                }
            }
        }
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}

@Composable
private fun HomeScreen(
    fingerprint: String,
    hasPassphrase: Boolean,
    onUserAction: () -> Unit,
    onLock: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Unlocked", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Fingerprint: $fingerprint", style = MaterialTheme.typography.bodyMedium)
        if (hasPassphrase) {
            Text("Passphrase: required at unlock", style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            onUserAction()
            onLock()
        }) { Text("Lock") }
    }
}
