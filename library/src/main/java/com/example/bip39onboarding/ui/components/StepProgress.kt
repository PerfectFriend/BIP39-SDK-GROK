package com.example.bip39onboarding.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bip39onboarding.R
import com.example.bip39onboarding.data.OnboardingStep
import com.example.bip39onboarding.ui.theme.DefaultOnboardingColors
import com.example.bip39onboarding.ui.theme.LocalOnboardingColors

/**
 * Gaudí-inspired mosaic step indicator (replaces plain LinearProgressIndicator).
 */
@Composable
fun StepProgress(step: OnboardingStep) {
    val (current, total) = step.toProgress()
    if (total <= 0) return

    val colors = runCatching { LocalOnboardingColors.current }.getOrDefault(DefaultOnboardingColors)
    val mosaic = colors.mosaicPalette

    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(
            text = stringResource(R.string.bip39_step_progress, current, total),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(total) { index ->
                val filled = index < current
                val shard = mosaic[index % mosaic.size]
                Canvas(modifier = Modifier.weight(1f).height(8.dp)) {
                    drawRoundRect(
                        color = if (filled) shard else colors.panelElevated,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    if (filled) {
                        drawRoundRect(
                            color = colors.strokeNeon.copy(alpha = 0.35f),
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}

private fun OnboardingStep.toProgress(): Pair<Int, Int> = when (this) {
    OnboardingStep.SPLASH -> 0 to 0
    OnboardingStep.CHOOSE -> 1 to 9
    OnboardingStep.STRENGTH -> 2 to 9
    OnboardingStep.DISCLAIMER -> 3 to 9
    OnboardingStep.SHOW_MNEMONIC -> 4 to 9
    OnboardingStep.VERIFY -> 5 to 9
    OnboardingStep.SET_PASSPHRASE -> 6 to 9
    OnboardingStep.SET_PIN -> 7 to 9
    OnboardingStep.SET_PANIC_PIN -> 8 to 9
    OnboardingStep.RESTORE_CHOOSE,
    OnboardingStep.RESTORE_PASTE,
    OnboardingStep.RESTORE_QR,
    OnboardingStep.RESTORE_PDF -> 2 to 5
    OnboardingStep.DONE -> 9 to 9
}
