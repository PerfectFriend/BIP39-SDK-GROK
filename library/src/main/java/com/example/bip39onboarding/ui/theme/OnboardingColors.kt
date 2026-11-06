package com.example.bip39onboarding.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Design tokens: Cyberpunk × Van Gogh × Gaudí
 *
 * - Void / neon chrome → cyberpunk
 * - Swirl night blues & amber → Van Gogh
 * - Mosaic fragment hues → Gaudí trencadís
 */
@Immutable
data class OnboardingColors(
    val void: Color = Color(0xFF0A0618),
    val panel: Color = Color(0xFF12102A),
    val panelElevated: Color = Color(0xFF1A1738),
    val strokeNeon: Color = Color(0xFF00F0FF),
    val strokeMagenta: Color = Color(0xFFFF2D95),
    val glowAmber: Color = Color(0xFFF4C15D),
    val textPrimary: Color = Color(0xFFF2EFE9),
    val textMuted: Color = Color(0xFF9A94B8),
    val textOnNeon: Color = Color(0xFF0A0618),
    /** Gaudí mosaic shards */
    val mosaicTeal: Color = Color(0xFF2EC4B6),
    val mosaicCoral: Color = Color(0xFFFF6B6B),
    val mosaicGold: Color = Color(0xFFFFD166),
    val mosaicIndigo: Color = Color(0xFF5C6BC0),
    val mosaicCream: Color = Color(0xFFFFF1D6),
    /** Van Gogh night field */
    val starWhite: Color = Color(0xFFE8F1FF),
    val swirlDeep: Color = Color(0xFF1B2744),
    val swirlMid: Color = Color(0xFF3D5A80),
    val success: Color = Color(0xFF3DDC97),
    val danger: Color = Color(0xFFFF4D6D),
) {
    val mosaicPalette: List<Color>
        get() = listOf(mosaicTeal, mosaicCoral, mosaicGold, mosaicIndigo, mosaicCream, strokeNeon, strokeMagenta)
}

val LocalOnboardingColors = staticCompositionLocalOf { OnboardingColors() }

/** Default instance for call sites outside CompositionLocal. */
val DefaultOnboardingColors = OnboardingColors()
