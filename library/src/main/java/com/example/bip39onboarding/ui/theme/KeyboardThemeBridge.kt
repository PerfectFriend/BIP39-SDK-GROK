package com.example.bip39onboarding.ui.theme

import com.narde.keyboard.KeyboardColorScheme

/**
 * Maps onboarding theme preset ids → in-app keyboard schemes (docs/EVOLUTION_UNIFIED.md).
 */
object KeyboardThemeBridge {

    private val map = mapOf(
        "neon_night" to KeyboardColorScheme.Matrix,
        "starry_amber" to KeyboardColorScheme.Sunset,
        "gaudi_mosaic" to KeyboardColorScheme.Forest,
        "cyber_sakura" to KeyboardColorScheme.Lavender,
        "deep_ocean" to KeyboardColorScheme.Ocean,
        "violet_haze" to KeyboardColorScheme.Lavender,
        "ember_grid" to KeyboardColorScheme.Sunset,
        "mint_terminal" to KeyboardColorScheme.Matrix,
        "noir_gold" to KeyboardColorScheme.Amoled,
        "toxic_lime" to KeyboardColorScheme.Matrix,
        "royal_trencadis" to KeyboardColorScheme.Forest,
        "mono_cyan" to KeyboardColorScheme.Ocean,
        "classic_void" to KeyboardColorScheme.Dark,
    )

    @Volatile
    var currentPresetId: String = "neon_night"

    fun schemeForPreset(presetId: String): KeyboardColorScheme =
        map[presetId] ?: KeyboardColorScheme.Dark

    fun schemeForCurrentTheme(): KeyboardColorScheme =
        schemeForPreset(currentPresetId)
}
