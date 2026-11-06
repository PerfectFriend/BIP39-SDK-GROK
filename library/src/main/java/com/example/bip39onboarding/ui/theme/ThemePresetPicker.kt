package com.example.bip39onboarding.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * One-shot UI to pick among [OnboardingThemePresets] (20 looks).
 * Host persists [OnboardingThemePresets.Preset.id] and applies [OnboardingThemePresets.config].
 */
@Composable
fun ThemePresetPicker(
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Choose a look"
) {
    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFF0A0618))
            .padding(20.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Color(0xFFF2EFE9))
        Spacer(Modifier.height(8.dp))
        Text(
            "Cyberpunk × Van Gogh × Gaudí — 20 presets",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9A94B8)
        )
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(OnboardingThemePresets.all, key = { it.id }) { preset ->
                val selected = preset.id == selectedId
                val shape = RoundedCornerShape(14.dp)
                Column(
                    Modifier
                        .clip(shape)
                        .background(preset.colors.panel)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) preset.colors.strokeNeon else preset.colors.strokeNeon.copy(alpha = 0.25f),
                            shape = shape
                        )
                        .clickable { onSelect(preset.id) }
                        .padding(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        preset.colors.mosaicPalette.take(5).forEach { c ->
                            Box(
                                Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(c)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(preset.nameEn, color = preset.colors.textPrimary, style = MaterialTheme.typography.labelLarge)
                    Text(preset.id, color = preset.colors.textMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
