package com.example.bip39onboarding.api

/**
 * Public API surface of the onboarding engine (v1.3+ / Phase B freeze intent).
 *
 * ## Stable for host apps
 * - [OnboardingHost]
 * - [com.example.bip39onboarding.ui.theme.OnboardingThemeConfig]
 * - [com.example.bip39onboarding.ui.theme.OnboardingColors]
 * - [com.example.bip39onboarding.ui.theme.OnboardingTheme]
 * - [com.example.bip39onboarding.ui.onboarding.OnboardingNavHost]
 * - [com.example.bip39onboarding.ui.unlock.PinUnlockScreen]
 * - [com.example.bip39onboarding.ui.welcome.WelcomeAboardScreen]
 * - [com.example.bip39onboarding.data.SeedPayload]
 * - [com.example.bip39onboarding.security.SeedStorage]
 * - [com.example.bip39onboarding.security.TinkSeedStorage] (+ adapter)
 * - [com.example.bip39onboarding.security.SessionLock]
 * - [com.example.bip39onboarding.security.Bip39Manager] (generate / validate / toSeed / clear)
 *
 * ## Internal — do not depend on from app code
 * - PinUnlock, CryptoUtils details, BiometricHelper internals
 * - OnboardingViewModel, step enums may evolve
 * - Most of ui.components except documented helpers
 *
 * Breaking changes to stable types require a version bump and CHANGELOG entry.
 */
object PublicApi {
    const val API_LEVEL = 2
}
