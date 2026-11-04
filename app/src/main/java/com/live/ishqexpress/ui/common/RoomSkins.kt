package com.live.ishqexpress.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

/**
 * Party room backgrounds. In production the catalogue (name, price, preview image, full-size
 * image/video) comes from the server and the art is downloaded; these are drawn stand-ins.
 */
enum class SkinArt { Neon, Beach, Moon, Stage, Snow, Galaxy }

data class RoomSkin(val id: Int, val name: String, val coinsPerWeek: Int, val art: SkinArt) {
    val free get() = coinsPerWeek == 0
}

val roomSkins = listOf(
    RoomSkin(0, "Neon arena", 0, SkinArt.Neon),
    RoomSkin(1, "Golden beach", 9_999, SkinArt.Beach),
    RoomSkin(2, "Moon night", 9_999, SkinArt.Moon),
    RoomSkin(3, "Spotlight stage", 17_777, SkinArt.Stage),
    RoomSkin(4, "Snow park", 17_777, SkinArt.Snow),
    RoomSkin(5, "Galaxy", 24_999, SkinArt.Galaxy),
)

/** The host's chosen skin and the paid skins they own (demo state; server-side in production). */
object RoomSkinState {
    var selected by mutableIntStateOf(0)
    val owned = mutableStateListOf(0)
}

/** Full-bleed room background for a skin. [animated] drifts lights/stars slowly. */
@Composable
fun SkinBackdrop(skin: RoomSkin, modifier: Modifier = Modifier, animated: Boolean = true) {
    val phase = if (animated) rememberInfiniteTransition(label = "skin").animateFloat(
        0f, 1f, infiniteRepeatable(tween(9_000, easing = LinearEasing)), label = "p"
    ) else null
    Canvas(modifier) { drawSkin(skin.art, phase?.value ?: 0.3f) }
}

fun DrawScope.drawSkin(art: SkinArt, p: Float) {
    val w = size.width
    val h = size.height
    when (art) {
        SkinArt.Neon -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF2A0B5C), Color(0xFF6B1A8C), Color(0xFF241063), Color(0xFF120A3A))))
            // Glowing arches
            val c = Offset(w / 2, h * 0.55f)
            listOf(0.95f to Color(0xFF39D5FF), 0.78f to Color(0xFFFF3DB8), 0.62f to Color(0xFF8A5CFF)).forEachIndexed { i, (r, col) ->
                val rr = w * r * (1f + 0.02f * sin((p * 6.28f) + i))
                drawArc(col.copy(alpha = 0.18f), 180f, 180f, false, Offset(c.x - rr, c.y - rr), Size(rr * 2, rr * 2), style = Stroke(w * 0.06f))
                drawArc(col.copy(alpha = 0.75f), 180f, 180f, false, Offset(c.x - rr, c.y - rr), Size(rr * 2, rr * 2), style = Stroke(w * 0.008f))
            }
            // Perspective floor grid
            val top = h * 0.62f
            drawRect(Brush.verticalGradient(listOf(Color(0x00120A3A), Color(0xFF1A0F55)), startY = top, endY = h), Offset(0f, top), Size(w, h - top))
            val grid = Color(0xFF7FA8FF).copy(alpha = 0.45f)
            for (i in -8..8) drawLine(grid, Offset(w / 2 + i * w * 0.04f, top), Offset(w / 2 + i * w * 0.32f, h), 1.5f)
            var y = top
            var step = h * 0.012f
            val shift = (p * step * 4) % (step * 4)
            while (y < h) { drawLine(grid, Offset(0f, y + shift), Offset(w, y + shift), 1.5f); y += step; step *= 1.28f }
        }
        SkinArt.Beach -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF3B1E1A), Color(0xFFE9893A), Color(0xFFFFC56B), Color(0xFFB86A2A), Color(0xFF6B3A1A))))
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF4C2), Color(0x00FFC56B)), Offset(w * 0.5f, h * 0.42f), w * 0.5f), w * 0.5f, Offset(w * 0.5f, h * 0.42f))
            drawCircle(Color(0xFFFFF1C9), w * 0.11f, Offset(w * 0.5f, h * 0.42f))
            for (i in 0 until 14) {
                val yy = h * (0.52f + i * 0.03f)
                val ww = w * (0.15f + i * 0.04f)
                val dx = sin(p * 6.28f + i) * w * 0.03f
                drawLine(Color(0xFFFFE3A3).copy(alpha = 0.55f - i * 0.03f), Offset(w / 2 - ww / 2 + dx, yy), Offset(w / 2 + ww / 2 + dx, yy), 3f)
            }
        }
        SkinArt.Moon -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF05060F), Color(0xFF0E1433), Color(0xFF1C1440))))
            stars(w, h, p, 60, Color.White)
            drawCircle(Brush.radialGradient(listOf(Color(0x55DDE6FF), Color(0x00DDE6FF)), Offset(w * 0.62f, h * 0.2f), w * 0.4f), w * 0.4f, Offset(w * 0.62f, h * 0.2f))
            drawCircle(Brush.linearGradient(listOf(Color(0xFFF4F1E6), Color(0xFFBDB8A8)), Offset(w * 0.5f, h * 0.1f), Offset(w * 0.75f, h * 0.3f)), w * 0.17f, Offset(w * 0.62f, h * 0.2f))
            // Hanging crescents
            listOf(0.2f, 0.4f).forEach { x ->
                drawLine(Color.White.copy(alpha = 0.5f), Offset(w * x, 0f), Offset(w * x, h * 0.3f), 2f)
                drawCircle(Color(0xFFF7F2DC), w * 0.05f, Offset(w * x, h * 0.34f))
                drawCircle(Color(0xFF0E1433), w * 0.045f, Offset(w * x + w * 0.02f, h * 0.33f))
            }
        }
        SkinArt.Stage -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF0A0604), Color(0xFF2A1A08), Color(0xFF120A04))))
            for (i in 0 until 5) {
                val x = w * (0.1f + i * 0.2f)
                val sway = sin(p * 6.28f + i * 1.3f) * w * 0.08f
                val path = Path().apply {
                    moveTo(x, 0f); lineTo(x + sway - w * 0.16f, h * 0.85f); lineTo(x + sway + w * 0.16f, h * 0.85f); close()
                }
                drawPath(path, Brush.verticalGradient(listOf(Color(0xCCFFE2A0), Color(0x00FFC060)), 0f, h * 0.85f))
                drawCircle(Color(0xFFFFF1C4), w * 0.022f, Offset(x, h * 0.01f))
            }
            drawOval(Brush.radialGradient(listOf(Color(0x88FFC060), Color(0x00FFC060)), Offset(w / 2, h * 0.86f), w * 0.6f), Offset(-w * 0.1f, h * 0.78f), Size(w * 1.2f, h * 0.16f))
        }
        SkinArt.Snow -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF0B1A3A), Color(0xFF1D3F7A), Color(0xFF2B5DA8), Color(0xFFCFE3FF))))
            for (i in 0 until 4) {
                val x = w * (0.15f + i * 0.25f)
                drawLine(Color(0xFF0B1A3A), Offset(x, h * 0.45f), Offset(x, h * 0.8f), w * 0.012f)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE9A8), Color(0x00FFE9A8)), Offset(x, h * 0.45f), w * 0.1f), w * 0.1f, Offset(x, h * 0.45f))
            }
            for (i in 0 until 80) {
                val sx = ((i * 73) % 100) / 100f
                val sy = ((i * 37 + p * 100) % 100) / 100f
                drawCircle(Color.White.copy(alpha = 0.8f), 1.5f + (i % 3), Offset(w * sx, h * sy))
            }
        }
        SkinArt.Galaxy -> {
            drawRect(Brush.linearGradient(listOf(Color(0xFF0A0420), Color(0xFF3A0E6B), Color(0xFF0B2A5C))))
            drawCircle(Brush.radialGradient(listOf(Color(0x88FF4FD8), Color(0x00FF4FD8)), Offset(w * 0.3f, h * 0.35f), w * 0.6f), w * 0.6f, Offset(w * 0.3f, h * 0.35f))
            drawCircle(Brush.radialGradient(listOf(Color(0x7739D5FF), Color(0x0039D5FF)), Offset(w * 0.8f, h * 0.65f), w * 0.55f), w * 0.55f, Offset(w * 0.8f, h * 0.65f))
            stars(w, h, p, 90, Color.White)
        }
    }
}

private fun DrawScope.stars(w: Float, h: Float, p: Float, n: Int, c: Color) {
    for (i in 0 until n) {
        val sx = ((i * 7919) % 1000) / 1000f
        val sy = ((i * 104729) % 1000) / 1000f
        val tw = 0.35f + 0.65f * ((sin(p * 6.28f * 2 + i) + 1) / 2)
        drawCircle(c.copy(alpha = tw * 0.9f), 1f + (i % 3) * 0.8f, Offset(w * sx, h * sy))
    }
}
