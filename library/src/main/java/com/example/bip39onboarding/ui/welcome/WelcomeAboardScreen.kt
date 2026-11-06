package com.example.bip39onboarding.ui.welcome

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import android.provider.Settings
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bip39onboarding.R
import com.example.bip39onboarding.ui.theme.DefaultOnboardingColors
import com.example.bip39onboarding.ui.theme.LocalOnboardingThemeConfig
import com.example.bip39onboarding.ui.theme.OnboardingColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Universal entry screen: Welcome Aboard.
 * Visual system: Cyberpunk chrome + Van Gogh swirl + Gaudí mosaic.
 *
 * CTAs skip the intermediate "choose" step and go straight to create / restore.
 */
@Composable
fun WelcomeAboardScreen(
    onCreate: () -> Unit,
    onRestore: () -> Unit,
    reduceMotion: Boolean? = null,
    colors: OnboardingColors = DefaultOnboardingColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeConfig = LocalOnboardingThemeConfig.current
    val systemReduceMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val effectiveReduceMotion = reduceMotion ?: themeConfig.reduceMotion || systemReduceMotion
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.void)
    ) {
        WelcomeAboardBackdrop(colors = colors, reduceMotion = effectiveReduceMotion)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                themeConfig.logo?.invoke()
                if (themeConfig.logo != null) {
                    Spacer(Modifier.height(16.dp))
                }
                // Gaudí mosaic chip row
                MosaicChips(colors = colors)

                Spacer(Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.bip39_welcome_aboard_title),
                    color = colors.textPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.semantics {
                        contentDescription = "Welcome Aboard"
                    }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.bip39_welcome_aboard_subtitle),
                    color = colors.textMuted,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.bip39_welcome_aboard_tagline),
                    color = colors.strokeNeon.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.2.sp
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onCreate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.strokeNeon,
                        contentColor = colors.textOnNeon
                    )
                ) {
                    Text(
                        stringResource(R.string.bip39_create_account),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.textPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.strokeMagenta.copy(alpha = 0.85f))
                ) {
                    Text(
                        stringResource(R.string.bip39_restore_account),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.bip39_welcome_aboard_footer),
                    color = colors.textMuted.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun MosaicChips(colors: OnboardingColors) {
    val shards = colors.mosaicPalette
    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        shards.take(7).forEachIndexed { i, c ->
            Canvas(
                modifier = Modifier
                    .size(width = 12.dp, height = 12.dp)
            ) {
                val chipW = size.width
                val chipH = size.height
                drawRoundRect(
                    color = c.copy(alpha = 0.9f),
                    topLeft = Offset(0f, if (i % 2 == 0) 0f else chipH * 0.15f),
                    size = Size(chipW, chipH * 0.85f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
            }
        }
    }
}

@Composable
private fun WelcomeAboardBackdrop(
    colors: OnboardingColors,
    reduceMotion: Boolean
) {
    val infinite = rememberInfiniteTransition(label = "welcome_swirl")
    val animatedPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val animatedPulse by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val phase = if (reduceMotion) 0.15f else animatedPhase
    val pulse = if (reduceMotion) 0.6f else animatedPulse

    val stars = remember {
        List(48) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                r = 0.6f + Random.nextFloat() * 2.2f,
                a = 0.25f + Random.nextFloat() * 0.75f
            )
        }
    }
    val mosaic = remember {
        List(18) {
            MosaicTile(
                x = Random.nextFloat(),
                y = 0.55f + Random.nextFloat() * 0.42f,
                s = 0.02f + Random.nextFloat() * 0.04f,
                colorIndex = it % 7
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.38f

        // Van Gogh night gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.void,
                    colors.swirlDeep,
                    colors.swirlMid.copy(alpha = 0.55f),
                    colors.void
                )
            )
        )

        // Soft swirl bands
        for (i in 0 until 5) {
            val radius = h * (0.18f + i * 0.07f)
            val rot = Math.toDegrees((phase + i * 0.35f).toDouble()).toFloat()
            rotate(rot, pivot = Offset(cx, cy)) {
                drawArc(
                    color = colors.glowAmber.copy(alpha = 0.04f + i * 0.012f),
                    startAngle = 20f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius * 0.7f),
                    size = Size(radius * 2f, radius * 1.4f),
                    style = Stroke(width = 18f - i * 2f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = colors.strokeNeon.copy(alpha = 0.03f + i * 0.01f),
                    startAngle = 200f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(cx - radius * 0.9f, cy - radius * 0.5f),
                    size = Size(radius * 1.8f, radius * 1.2f),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }
        }

        // Stars
        stars.forEach { s ->
            val twinkle = (0.5f + 0.5f * sin(phase + s.x * 8f)).coerceIn(0.2f, 1f)
            drawCircle(
                color = colors.starWhite.copy(alpha = s.a * twinkle * 0.9f),
                radius = s.r,
                center = Offset(s.x * w, s.y * h * 0.7f)
            )
        }

        // Gaudí mosaic tiles near bottom
        val palette = colors.mosaicPalette
        mosaic.forEach { t ->
            val col = palette[t.colorIndex % palette.size]
            val left = t.x * w
            val top = t.y * h
            val side = t.s * w
            drawRoundRect(
                color = col.copy(alpha = 0.35f + 0.25f * pulse),
                topLeft = Offset(left, top),
                size = Size(side, side * 0.85f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }

        // Cyberpunk neon arch (Gaudí-like curve)
        val archPath = Path().apply {
            val left = w * 0.12f
            val right = w * 0.88f
            val baseY = h * 0.52f
            val peakY = h * 0.22f
            moveTo(left, baseY)
            cubicTo(
                w * 0.25f, peakY,
                w * 0.75f, peakY,
                right, baseY
            )
        }
        drawPath(
            path = archPath,
            color = colors.strokeNeon.copy(alpha = 0.25f + 0.35f * pulse),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
        drawPath(
            path = archPath,
            color = colors.strokeMagenta.copy(alpha = 0.12f + 0.15f * pulse),
            style = Stroke(width = 10f, cap = StrokeCap.Round)
        )
    }
}

private data class Star(val x: Float, val y: Float, val r: Float, val a: Float)
private data class MosaicTile(val x: Float, val y: Float, val s: Float, val colorIndex: Int)
