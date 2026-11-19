package com.narde.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Полный набор цветов клавиатуры. Можно собрать свой и передать в `StandardKeyboard(colors = ...)`. */
@Immutable
public data class KeyboardColors(
    public val background: Color,
    public val key: Color,
    public val keyPressed: Color,
    public val keyContent: Color,
    /** Служебные клавиши: ⇧ ⌫ ?123 RU/EN 🙂 */
    public val special: Color,
    public val specialPressed: Color,
    public val specialContent: Color,
    /** Акцент: ⏎, включённый ⇧, выбранный пункт. */
    public val accent: Color,
    public val accentPressed: Color,
    public val accentContent: Color,
    /** Мелкие подсказки (цифры на буквах, надпись на пробеле). */
    public val hint: Color,
    public val popup: Color,
    public val popupContent: Color,
    public val popupSelected: Color,
    public val popupSelectedContent: Color,
    public val divider: Color,
)

/**
 * 8 готовых цветовых схем. «Авто» (следовать системной светлой/тёмной теме) — это `scheme = null`
 * в [KeyboardState.scheme]: тогда берётся [Light] или [Dark] по `isSystemInDarkTheme()`.
 */
public enum class KeyboardColorScheme(public val displayName: String) {
    Light("Light"),
    Dark("Dark"),
    Amoled("AMOLED"),
    Ocean("Ocean"),
    Forest("Forest"),
    Sunset("Sunset"),
    Lavender("Lavender"),
    Matrix("Matrix");

    public val colors: KeyboardColors
        get() = when (this) {
            Light -> Palettes.light
            Dark -> Palettes.dark
            Amoled -> Palettes.amoled
            Ocean -> Palettes.ocean
            Forest -> Palettes.forest
            Sunset -> Palettes.sunset
            Lavender -> Palettes.lavender
            Matrix -> Palettes.matrix
        }

    /** Тёмная ли схема (по фону). */
    public val isDark: Boolean
        get() = when (this) {
            Light, Sunset, Lavender -> false
            else -> true
        }
}

/**
 * Палитры. Все пары «контент/фон» проверены на контраст ≥ 4.5:1 (WCAG AA),
 * это же проверяет KeyboardColorsTest.
 */
internal object Palettes {
    private fun c(rgb: Int): Color = Color(0xFF000000L or rgb.toLong())

    // Подряд: background, key, keyPressed, keyContent, special, specialPressed, specialContent,
    // accent, accentPressed, accentContent, hint, popup, popupContent, popupSelected, popupSelectedContent, divider
    private fun p(
        bg: Int, key: Int, keyP: Int, keyC: Int, sp: Int, spP: Int, spC: Int,
        ac: Int, acP: Int, acC: Int, hint: Int, pop: Int, popC: Int, popS: Int, popSC: Int, div: Int,
    ) = KeyboardColors(
        c(bg), c(key), c(keyP), c(keyC), c(sp), c(spP), c(spC),
        c(ac), c(acP), c(acC), c(hint), c(pop), c(popC), c(popS), c(popSC), c(div),
    )

    val light = p(0xF1F3F4, 0xFFFFFF, 0xDADCE0, 0x202124, 0xDADCE0, 0xC4C7CC, 0x202124,
        0x1A73E8, 0x1765CC, 0xFFFFFF, 0x5F6368, 0xFFFFFF, 0x202124, 0x1A73E8, 0xFFFFFF, 0xDADCE0)

    val dark = p(0x202124, 0x3C4043, 0x5F6368, 0xE8EAED, 0x2D2F31, 0x45484B, 0xE8EAED,
        0x8AB4F8, 0xAECBFA, 0x202124, 0xB0B5BA, 0x3C4043, 0xE8EAED, 0x8AB4F8, 0x202124, 0x3C4043)

    val amoled = p(0x000000, 0x141414, 0x2E2E2E, 0xF2F2F2, 0x1F1F1F, 0x333333, 0xF2F2F2,
        0x00BFA5, 0x1DE9B6, 0x000000, 0xA8A8A8, 0x1F1F1F, 0xF2F2F2, 0x00BFA5, 0x000000, 0x262626)

    val ocean = p(0x0A1F33, 0x143A5C, 0x1E5483, 0xE6F2FF, 0x0F2C48, 0x1A4468, 0xE6F2FF,
        0x4FC3F7, 0x81D4FA, 0x04263D, 0xA9CDEA, 0x143A5C, 0xE6F2FF, 0x4FC3F7, 0x04263D, 0x1E4A73)

    val forest = p(0x0F1F16, 0x1E3A2A, 0x2C5640, 0xE5F5E9, 0x162B1E, 0x244733, 0xE5F5E9,
        0x81C784, 0xA5D6A7, 0x0A1F10, 0xAAD6B2, 0x1E3A2A, 0xE5F5E9, 0x81C784, 0x0A1F10, 0x2A4A38)

    val sunset = p(0xFFF1E0, 0xFFFFFF, 0xFFD9B0, 0x3E2316, 0xFFDDB8, 0xFFC78A, 0x3E2316,
        0xBF360C, 0x9F2D09, 0xFFFFFF, 0x8A5636, 0xFFFFFF, 0x3E2316, 0xBF360C, 0xFFFFFF, 0xFFCC99)

    val lavender = p(0xEDE7F6, 0xFFFFFF, 0xD6CBEB, 0x2A1B4A, 0xD9CEF0, 0xC4B5E3, 0x2A1B4A,
        0x5E35B1, 0x4A2A91, 0xFFFFFF, 0x6B5B8F, 0xFFFFFF, 0x2A1B4A, 0x5E35B1, 0xFFFFFF, 0xD1C4E9)

    val matrix = p(0x000000, 0x07140A, 0x0F3318, 0x00FF41, 0x0B1F10, 0x123D1C, 0x00E63B,
        0x00FF41, 0x00C832, 0x000000, 0x00B02C, 0x07140A, 0x00FF41, 0x00FF41, 0x000000, 0x0F3318)
}

/**
 * Ряд круглых «свотчей» для выбора схемы: первый — «Авто» (половина светлая, половина тёмная),
 * далее все 8 схем. Выбранный обведён акцентным цветом.
 */
@Composable
public fun KeyboardSchemePicker(
    selected: KeyboardColorScheme?,
    onSelect: (KeyboardColorScheme?) -> Unit,
    modifier: Modifier = Modifier,
    swatchSize: Dp = 36.dp,
    autoLabel: String = "Auto",
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        val autoSelected = selected == null
        Box(
            Modifier
                .size(swatchSize)
                .clip(CircleShape)
                .border(
                    if (autoSelected) 3.dp else 1.dp,
                    if (autoSelected) Palettes.light.accent else Palettes.light.divider,
                    CircleShape,
                )
                .clickable(role = Role.RadioButton) { onSelect(null) }
                .semantics { contentDescription = autoLabel; this.selected = autoSelected },
        ) {
            Row(Modifier.size(swatchSize)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(Palettes.light.background))
                Box(Modifier.weight(1f).fillMaxHeight().background(Palettes.dark.background))
            }
        }
        KeyboardColorScheme.entries.forEach { scheme ->
            val colors = scheme.colors
            val isSelected = scheme == selected
            Box(
                Modifier
                    .size(swatchSize)
                    .clip(CircleShape)
                    .background(colors.background)
                    .border(
                        if (isSelected) 3.dp else 1.dp,
                        if (isSelected) colors.accent else colors.divider,
                        CircleShape,
                    )
                    .clickable(role = Role.RadioButton) { onSelect(scheme) }
                    .semantics { contentDescription = scheme.displayName; this.selected = isSelected },
            ) {
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = swatchSize / 6, top = swatchSize / 6)
                        .size(swatchSize / 3)
                        .clip(CircleShape)
                        .background(colors.key),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = swatchSize / 6, bottom = swatchSize / 6)
                        .size(swatchSize / 3)
                        .clip(CircleShape)
                        .background(colors.accent),
                )
            }
        }
    }
}
