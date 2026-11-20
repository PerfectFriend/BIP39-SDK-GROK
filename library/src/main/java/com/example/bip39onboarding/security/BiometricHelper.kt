package com.example.bip39onboarding.security

import android.content.Context
import androidx.fragment.app.FragmentActivity

/**
 * **Deprecated / disabled.** Product policy: biometric gate is not used.
 * PIN-only unlock. Kept as no-op API for binary compatibility with older hosts.
 */
@Deprecated("Biometric unlock is not supported; PIN-only policy")
object BiometricHelper {

    fun canAuthenticate(context: Context): Boolean = false

    fun availabilityMessage(context: Context): String =
        "Biometric unlock is disabled (PIN-only policy)"

    fun authenticateWithCrypto(
        activity: FragmentActivity,
        title: String = "Biometric",
        subtitle: String = "",
        negative: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        onError("biometric_disabled")
    }

    fun clear(context: Context) {
        try {
            context.getSharedPreferences("bip39_bio_gate", Context.MODE_PRIVATE)
                .edit().clear().apply()
        } catch (_: Exception) { /* ignore */ }
    }
}
