package com.example.bip39onboarding.api

/**
 * Optional capabilities for host apps.
 * See docs/EVOLUTION_UNIFIED.md and docs/STACK.md.
 *
 * Product policy (2026-11-20): **PIN-only unlock**. Biometric unlock is not supported
 * and must not be enabled for ParanoidX / sovereign products.
 */
object Bip39Features {

    /**
     * QR restore path. Default **false** — optional module risk (camera permission, mnemonic on screen).
     */
    @Volatile
    var enableQrRestore: Boolean = false

    /**
     * PDF export of seed. Default **false** — optional high-risk path.
     */
    @Volatile
    var enablePdfExport: Boolean = false

    /**
     * Failed PIN attempts before vault wipe (panic-equivalent). 0 = no wipe, only lockout.
     */
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

    /**
     * Biometric unlock / PIN seal under Keystore user-auth key.
     *
     * **Always treated as false.** Product decision: biometrics is not an unlock factor
     * in this SDK for sovereign / ParanoidX deployments. The property exists only so
     * older host call sites compile; setters are ignored.
     */
    @Deprecated("Biometric unlock is not supported; PIN-only policy", level = DeprecationLevel.WARNING)
    @Volatile
    var enableBiometricUnlock: Boolean = false
        set(@Suppress("UNUSED_PARAMETER") value) {
            field = false
        }
}
