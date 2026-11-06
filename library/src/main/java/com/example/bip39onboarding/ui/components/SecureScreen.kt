package com.example.bip39onboarding.ui.components

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.Executor

/**
 * Enables FLAG_SECURE for the composable lifetime (blocks screenshots / recents / many recorders).
 * On API 34+ also listens for [Activity.ScreenCaptureCallback] and exposes [onCaptureAttempted].
 */
@Composable
fun SecureScreen(
    onCaptureAttempted: (() -> Unit)? = null,
    content: @Composable (captureWarning: Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var captureWarning by remember { mutableStateOf(false) }

    DisposableEffect(activity, onCaptureAttempted) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        var callback: Activity.ScreenCaptureCallback? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && activity != null) {
            val executor = Executor { it.run() }
            callback = Activity.ScreenCaptureCallback {
                captureWarning = true
                onCaptureAttempted?.invoke()
            }
            try {
                activity.registerScreenCaptureCallback(executor, callback)
            } catch (_: Exception) {
                callback = null
            }
        }

        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                activity != null && callback != null
            ) {
                try {
                    activity.unregisterScreenCaptureCallback(callback)
                } catch (_: Exception) { }
            }
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    content(captureWarning)
}

/** Backward-compatible overload without capture lambda. */
@Composable
fun SecureScreen(content: @Composable () -> Unit) {
    SecureScreen(onCaptureAttempted = null) { content() }
}
