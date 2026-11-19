package com.narde.keyboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Иконки рисуются на Canvas: не зависят от шрифтовых глифов и от material-icons. */
internal enum class KeyIcon {
    ShiftOff, ShiftOn, CapsLock, Backspace, Enter, Hide, Globe, Palette, Emoji, ChevronLeft, ChevronRight, Expand
}

@Composable
internal fun KeyIconView(icon: KeyIcon, color: Color, modifier: Modifier = Modifier, iconSize: Dp = 22.dp) {
    Canvas(modifier.size(iconSize)) {
        val u = this.size.minDimension / 24f
        val stroke = Stroke(width = 1.9f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            KeyIcon.ShiftOff -> shiftArrow(u, 0f, color, stroke)
            KeyIcon.ShiftOn -> shiftArrow(u, 0f, color, Fill)
            KeyIcon.CapsLock -> {
                shiftArrow(u, -1.5f, color, Fill)
                poly(u, floatArrayOf(9f, 20f, 15f, 20f, 15f, 21.6f, 9f, 21.6f), true, color, Fill)
            }
            KeyIcon.Backspace -> {
                poly(u, floatArrayOf(9f, 5f, 21f, 5f, 21f, 19f, 9f, 19f, 2.5f, 12f), true, color, stroke)
                line(u, 12.5f, 9.5f, 17.5f, 14.5f, color, stroke.width)
                line(u, 17.5f, 9.5f, 12.5f, 14.5f, color, stroke.width)
            }
            KeyIcon.Enter -> {
                poly(u, floatArrayOf(19f, 6f, 19f, 13f, 5f, 13f), false, color, stroke)
                poly(u, floatArrayOf(10f, 8f, 5f, 13f, 10f, 18f), false, color, stroke)
            }
            KeyIcon.Hide -> poly(u, floatArrayOf(6f, 9f, 12f, 15f, 18f, 9f), false, color, stroke)
            KeyIcon.ChevronLeft -> poly(u, floatArrayOf(15f, 5f, 8f, 12f, 15f, 19f), false, color, stroke)
            KeyIcon.ChevronRight -> poly(u, floatArrayOf(9f, 5f, 16f, 12f, 9f, 19f), false, color, stroke)
            KeyIcon.Expand -> {
                poly(u, floatArrayOf(4f, 12f, 20f, 12f), false, color, stroke)
                poly(u, floatArrayOf(8f, 8f, 4f, 12f, 8f, 16f), false, color, stroke)
                poly(u, floatArrayOf(16f, 8f, 20f, 12f, 16f, 16f), false, color, stroke)
            }
            KeyIcon.Globe -> {
                drawCircle(color = color, radius = 9f * u, center = Offset(12f * u, 12f * u), style = stroke)
                drawOval(color = color, topLeft = Offset(7.5f * u, 3f * u), size = Size(9f * u, 18f * u), style = stroke)
                line(u, 3f, 12f, 21f, 12f, color, stroke.width)
            }
            KeyIcon.Palette -> {
                drawCircle(color = color, radius = 9f * u, center = Offset(12f * u, 12f * u), style = stroke)
                drawCircle(color = color, radius = 1.4f * u, center = Offset(8f * u, 10f * u))
                drawCircle(color = color, radius = 1.4f * u, center = Offset(12f * u, 7.5f * u))
                drawCircle(color = color, radius = 1.4f * u, center = Offset(16f * u, 10f * u))
                drawCircle(color = color, radius = 1.4f * u, center = Offset(9f * u, 15f * u))
            }
            KeyIcon.Emoji -> {
                drawCircle(color = color, radius = 9f * u, center = Offset(12f * u, 12f * u), style = stroke)
                drawCircle(color = color, radius = 1.3f * u, center = Offset(9f * u, 10f * u))
                drawCircle(color = color, radius = 1.3f * u, center = Offset(15f * u, 10f * u))
                drawArc(
                    color = color, startAngle = 25f, sweepAngle = 130f, useCenter = false,
                    topLeft = Offset(7f * u, 7f * u), size = Size(10f * u, 10f * u), style = stroke,
                )
            }
        }
    }
}

private fun DrawScope.shiftArrow(u: Float, dy: Float, color: Color, style: DrawStyle) {
    poly(
        u,
        floatArrayOf(12f, 4f + dy, 20f, 12f + dy, 15f, 12f + dy, 15f, 18f + dy, 9f, 18f + dy, 9f, 12f + dy, 4f, 12f + dy),
        true, color, style,
    )
}

private fun DrawScope.poly(u: Float, pts: FloatArray, close: Boolean, color: Color, style: DrawStyle) {
    val path = Path()
    path.moveTo(pts[0] * u, pts[1] * u)
    var i = 2
    while (i < pts.size) {
        path.lineTo(pts[i] * u, pts[i + 1] * u)
        i += 2
    }
    if (close) path.close()
    drawPath(path = path, color = color, style = style)
}

private fun DrawScope.line(u: Float, x1: Float, y1: Float, x2: Float, y2: Float, color: Color, width: Float) {
    drawLine(
        color = color, start = Offset(x1 * u, y1 * u), end = Offset(x2 * u, y2 * u),
        strokeWidth = width, cap = StrokeCap.Round,
    )
}
