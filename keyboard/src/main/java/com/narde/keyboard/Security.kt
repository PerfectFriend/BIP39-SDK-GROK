package com.narde.keyboard

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import kotlinx.coroutines.awaitCancellation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun SystemKeyboardGate(
    blockSystemKeyboard: Boolean = true,
    content: @Composable () -> Unit,
) {
    val block by rememberUpdatedState(blockSystemKeyboard)
    InterceptPlatformTextInput(
        interceptor = PlatformTextInputInterceptor { request, nextHandler ->
            if (block) awaitCancellation() else nextHandler.startInputMethod(request)
        },
        content = content,
    )
}

@Composable
public fun rememberHardwareKeyboardPresent(): Boolean {
    val c = LocalConfiguration.current
    return c.keyboard != Configuration.KEYBOARD_NOKEYS &&
        c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
}

@Composable
public fun SecureWindowEffect(enabled: Boolean = true) {
    val view = LocalView.current
    DisposableEffect(enabled, view) {
        val window = view.context.findActivity()?.window
        val added = enabled && window != null &&
            (window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE) == 0
        if (added) window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            if (added) window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@Composable
internal fun TouchFilterEffect(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled, view) {
        val previous = view.filterTouchesWhenObscured
        if (enabled) view.filterTouchesWhenObscured = true
        onDispose { view.filterTouchesWhenObscured = previous }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
