package com.example.bip39onboarding.api

/**
 * Optional capabilities for host apps.
 * See docs/EVOLUTION_UNIFIED.md and docs/STACK.md.
 */
object Bip39Features {
    @Volatile
    var enableQrRestore: Boolean = true

    @Volatile
    var enablePdfExport: Boolean = true

    @Volatile
    var wipeAfterFailedPinAttempts: Int = 10

    /**
     * Use in-app keyboard (com.narde.keyboard) for seed / passphrase / PIN.
     * When true, system IME is gated on sensitive Compose fields.
     * Default true — privacy baseline for crypto onboarding.
     */
    @Volatile
    var useInAppKeyboard: Boolean = true

    /** Shuffle digits on PIN/panic NumericKeypad (anti shoulder-surf). */
    @Volatile
    var shufflePinKeypad: Boolean = true
}
