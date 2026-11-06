package com.example.bip39onboarding.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BtnShape = RoundedCornerShape(16.dp)

@Composable
fun OnboardingScreenScaffold(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalOnboardingColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.void)
            .padding(24.dp),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun OnboardingTitle(text: String, modifier: Modifier = Modifier) {
    val colors = LocalOnboardingColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
fun OnboardingBody(text: String, modifier: Modifier = Modifier) {
    val colors = LocalOnboardingColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textMuted,
        modifier = modifier
    )
}

@Composable
fun NeonPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalOnboardingColors.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = BtnShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.strokeNeon,
            contentColor = colors.textOnNeon,
            disabledContainerColor = colors.panelElevated,
            disabledContentColor = colors.textMuted
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun NeonOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color? = null
) {
    val colors = LocalOnboardingColors.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = BtnShape,
        border = BorderStroke(1.5.dp, borderColor ?: colors.strokeMagenta.copy(alpha = 0.85f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun NeonTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalOnboardingColors.current
    TextButton(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Text(text, color = colors.strokeNeon)
    }
}
