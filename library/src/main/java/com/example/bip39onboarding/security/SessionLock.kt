package com.example.bip39onboarding.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

/**
 * In-memory session policy for hosts after successful unlock.
 *
 * Does not store secrets — only tracks last activity and signals [onLock]
 * so the host can clear [com.example.bip39onboarding.data.SeedPayload] and
 * navigate back to [com.example.bip39onboarding.ui.unlock.PinUnlockScreen].
 */
class SessionLock(
    /** Idle timeout while app is in foreground. */
    val foregroundTimeoutMs: Long = 5 * 60_000L,
    /** Lock when app stays in background longer than this. */
    val backgroundTimeoutMs: Long = 30_000L
) {
    @Volatile
    private var lastActivityMs: Long = System.currentTimeMillis()

    @Volatile
    private var backgroundedAtMs: Long = 0L

    fun touch() {
        lastActivityMs = System.currentTimeMillis()
    }

    fun onBackground() {
        backgroundedAtMs = System.currentTimeMillis()
    }

    fun onForeground(): Boolean {
        val bg = backgroundedAtMs
        backgroundedAtMs = 0L
        if (bg > 0L && System.currentTimeMillis() - bg >= backgroundTimeoutMs) {
            return true // should lock
        }
        return shouldLockForeground()
    }

    fun shouldLockForeground(): Boolean {
        return System.currentTimeMillis() - lastActivityMs >= foregroundTimeoutMs
    }
}

/**
 * Observes lifecycle + idle timeout. Calls [onLock] once when session expires.
 */
@Composable
fun RememberSessionLockEffect(
    session: SessionLock,
    enabled: Boolean = true,
    onLock: () -> Unit
) {
    if (!enabled) return

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, session) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> session.onBackground()
                Lifecycle.Event.ON_START -> {
                    if (session.onForeground()) {
                        onLock()
                    } else {
                        session.touch()
                    }
                }
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(session) {
        while (true) {
            delay(5_000L)
            if (session.shouldLockForeground()) {
                onLock()
                break
            }
        }
    }
}
