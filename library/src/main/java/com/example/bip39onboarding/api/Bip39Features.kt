package com.example.bip39onboarding.api

/**
 * Optional capabilities. Host apps can disable QR/PDF to avoid CAMERA permission
 * and large transitive deps (audit P1-8 / P1-9).
 */
object Bip39Features {
    @Volatile
    var enableQrRestore: Boolean = true

    @Volatile
    var enablePdfExport: Boolean = true

    @Volatile
    var wipeAfterFailedPinAttempts: Int = 10
}
