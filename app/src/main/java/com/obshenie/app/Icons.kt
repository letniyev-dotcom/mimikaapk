package com.obshenie.app

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.cos
import kotlin.math.sin

private fun circle(cx: Float, cy: Float, r: Float): String =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

object Glyphs {
    const val MOON = "M20 14.5A8.5 8.5 0 0 1 9.5 4a8.5 8.5 0 1 0 10.5 10.5z"
    val DOTS = circle(12f, 5f, 1.9f) + circle(12f, 12f, 1.9f) + circle(12f, 19f, 1.9f)
    const val PENCIL = "M4 20h4L19.5 8.5a2.1 2.1 0 0 0-3-3L5 17z"
    const val CHAT =
        "M12 2.6c5.3 0 9.4 3.5 9.4 7.9s-4.1 7.9-9.4 7.9c-.8 0-1.5-.1-2.2-.2L5.2 21.4l.5-4.6C3.7 15.3 2.6 13 2.6 10.5c0-4.4 4.1-7.9 9.4-7.9z"
    val CONTACTS = circle(9f, 8.5f, 3.3f) +
        "M3 19.5c1.2-3.2 3.3-4.7 6-4.7s4.8 1.5 6 4.7" +
        circle(17f, 9.5f, 2.6f) +
        "M16.2 14.9c2.3.1 4 1.5 4.8 4.3"
    val PROFILE = circle(12f, 8.5f, 3.6f) + "M5 20c1.4-3.3 3.9-4.9 7-4.9s5.6 1.6 7 4.9"
    const val READ_ALL = "M2 13l4 4L15 8M10 16.5l1.5 1.5L21 8"
    const val CHECK = "M1 5.8l3.6 3.6L11.4 1.4M7 8.3l1.1 1.1L15 1.4"
    const val PIN =
        "M14 4v5c0 1.12.37 2.16 1 3H9c.65-.86 1-1.9 1-3V4h5m3-2H7c-.55 0-1 .45-1 1s.45 1 1 1h1v5c0 1.66-1.34 3-3 3v2h5.97v7l1 1 1-1v-7H19v-2c-1.66 0-3-1.34-3-3V4h1c.55 0 1-.45 1-1s-.45-1-1-1z"
}

/** Рисует векторный путь из SVG-координат. [box] — ширина области viewBox, [strokeWidth] = 0 — заливка. */
@Composable
fun PathIcon(
    d: String,
    color: Color,
    modifier: Modifier,
    box: Float = 24f,
    strokeWidth: Float = 0f,
) {
    val path = remember(d) { PathParser().parsePathString(d).toPath() }
    Canvas(modifier) {
        val s = size.width / box
        scale(s, pivot = Offset.Zero) {
            if (strokeWidth > 0f) {
                drawPath(
                    path,
                    color,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            } else {
                drawPath(path, color)
            }
        }
    }
}

/** Линейные иконки сетки 24×24: луна в светлой теме и солнце в тёмной (как в letnievpn). */
@Composable
fun ThemeIcon(dark: Boolean, color: Color, modifier: Modifier) {
    if (!dark) {
        PathIcon(Glyphs.MOON, color, modifier, strokeWidth = 1.8f)
    } else {
        Canvas(modifier) {
            val k = size.width / 24f
            drawCircle(color, radius = 4.5f * k, center = Offset(12f * k, 12f * k), style = Stroke(1.8f * k, cap = StrokeCap.Round))
            for (i in 0 until 8) {
                val a = Math.PI / 4 * i
                val c = cos(a).toFloat()
                val s = sin(a).toFloat()
                drawLine(
                    color,
                    Offset((12f + 8f * c) * k, (12f + 8f * s) * k),
                    Offset((12f + 10.5f * c) * k, (12f + 10.5f * s) * k),
                    strokeWidth = 1.8f * k,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
