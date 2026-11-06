package com.example.bip39onboarding.ui.theme

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Host-facing theme overrides for the onboarding engine.
 *
 * @param colors palette tokens (cyberpunk × Van Gogh × Gaudí by default)
 * @param reduceMotion when true, Welcome Aboard and similar skip motion
 * @param logo optional composable brand mark above the Welcome Aboard title
 * @param showStepProgress when false, mosaic step bar is hidden
 */
@Immutable
data class OnboardingThemeConfig(
    val colors: OnboardingColors = DefaultOnboardingColors,
    val reduceMotion: Boolean = false,
    val logo: (@Composable () -> Unit)? = null,
    val showStepProgress: Boolean = true
)

val LocalOnboardingThemeConfig = staticCompositionLocalOf { OnboardingThemeConfig() }

@Composable
fun OnboardingTheme(
    config: OnboardingThemeConfig = OnboardingThemeConfig(),
    content: @Composable () -> Unit
) {
    val colors = config.colors
    val scheme = remember(colors) {
        darkColorScheme(
            primary = colors.strokeNeon,
            onPrimary = colors.textOnNeon,
            secondary = colors.strokeMagenta,
            onSecondary = colors.textPrimary,
            tertiary = colors.glowAmber,
            background = colors.void,
            onBackground = colors.textPrimary,
            surface = colors.panel,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.panelElevated,
            onSurfaceVariant = colors.textMuted,
            outline = colors.strokeNeon.copy(alpha = 0.45f),
            error = colors.danger,
            onError = colors.textPrimary
        )
    }
    CompositionLocalProvider(
        LocalOnboardingColors provides colors,
        LocalOnboardingThemeConfig provides config
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

@Composable
fun OnboardingTheme(
    colors: OnboardingColors,
    content: @Composable () -> Unit
) {
    OnboardingTheme(config = OnboardingThemeConfig(colors = colors), content = content)
}

@Composable
fun onboardingColors(): OnboardingColors = LocalOnboardingColors.current

@Composable
fun onboardingThemeConfig(): OnboardingThemeConfig = LocalOnboardingThemeConfig.current

fun Modifier.onboardingLogoSize(): Modifier = this.size(56.dp)
