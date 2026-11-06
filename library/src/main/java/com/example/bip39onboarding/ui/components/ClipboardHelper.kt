package com.example.bip39onboarding.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.bip39onboarding.R

/**
 * Copies sensitive text and schedules clipboard clear.
 */
object ClipboardHelper {

    private const val CLEAR_DELAY_MS = 60_000L

    fun copySensitive(context: Context, text: String, label: String = "seed") {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            cm.setPrimaryClip(
                ClipData.newPlainText(label, text).apply {
                    // API 33+ sensitive flag where available via description
                }
            )
        } else {
            cm.setPrimaryClip(ClipData.newPlainText(label, text))
        }
        Toast.makeText(context, R.string.bip39_copied_toast, Toast.LENGTH_LONG).show()
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    cm.clearPrimaryClip()
                } else {
                    cm.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            } catch (_: Exception) { /* ignore */ }
        }, CLEAR_DELAY_MS)
    }
}
