package com.example.bip39onboarding.vault

import android.content.Context
import androidx.fragment.app.FragmentActivity

/**
 * **Deprecated / disabled.** Product policy (2026-11-20): biometric unlock is not used.
 * PIN-only unlock for all sovereign / ParanoidX deployments.
 *
 * Methods are no-ops so existing host call sites compile; they never seal or unseal a PIN.
 */
@Deprecated("Biometric unlock is not supported; PIN-only policy")
object BiometricPinCache {

    fun isSealed(context: Context): Boolean = false

    fun canUse(context: Context): Boolean = false

    fun seal(
        activity: FragmentActivity,
        pin: CharArray,
        title: String = "Enable biometric unlock",
        subtitle: String = "Confirm biometrics to store a device-bound unlock factor",
        negative: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        pin.fill('\u0000')
        onError("biometric_disabled")
    }

    fun unseal(
        activity: FragmentActivity,
        title: String = "Unlock",
        subtitle: String = "Use biometrics",
        negative: String = "Use PIN",
        onPin: (CharArray) -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        onError("biometric_disabled")
    }

    fun clear(context: Context) {
        try {
            context.getSharedPreferences("bip39_bio_pin_cache", Context.MODE_PRIVATE)
                .edit().clear().apply()
        } catch (_: Exception) { /* ignore */ }
    }
}
