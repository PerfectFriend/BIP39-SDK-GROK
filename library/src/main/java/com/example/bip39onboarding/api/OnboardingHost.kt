package com.example.bip39onboarding.api

import com.example.bip39onboarding.data.SeedPayload

/**
 * Host integration contract (Phase B public API).
 *
 * Implement in the app module. The library never talks to analytics backends itself.
 * **Do not** put seed words, PIN, or passphrase into [onAnalyticsEvent] properties.
 */
interface OnboardingHost {

    /**
     * Called after successful create/restore onboarding when the vault is saved
     * and the seed is handed off once.
     *
     * Caller MUST [SeedPayload.clear] when finished deriving keys.
     */
    fun onOnboardingComplete(payload: SeedPayload)

    /**
     * Panic PIN or explicit wipe — storage already cleared by the library.
     * Host should reset navigation / process state.
     */
    fun onVaultWiped()

    /**
     * Optional funnel signals (screen names, button ids). No secrets.
     */
    fun onAnalyticsEvent(name: String, properties: Map<String, String> = emptyMap()) {}
}

/**
 * No-op host for previews and tests.
 */
object NoOpOnboardingHost : OnboardingHost {
    override fun onOnboardingComplete(payload: SeedPayload) {
        payload.clear()
    }

    override fun onVaultWiped() = Unit
}
