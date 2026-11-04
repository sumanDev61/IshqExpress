package com.live.ishqexpress.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Night canvas: deep violet → ink, neon aurora blobs, slow light beams and twinkling stars. */
object Backdrop {
    val top = Color(0xFF1B0B33)
    val middle = Color(0xFF0E0620)
    val bottom = Color(0xFF080313)
    val blobPink = Color(0xFFFF2E8B)
    val blobViolet = Color(0xFF7A2BE8)
    val blobPeach = Color(0xFFFF7A59)
    val blobAqua = Color(0xFF26C6FF)

    val base = Brush.verticalGradient(0f to top, 0.5f to middle, 1f to bottom)

    /** Fixed star field (x, y, size, twinkle offset) so it never jumps between frames. */
    val stars: List<FloatArray> = List(70) { i ->
        val x = ((i * 0.618034f) % 1f)
        val y = ((i * 0.3819f + (i % 7) * 0.071f) % 1f)
        floatArrayOf(x, y, 0.6f + (i % 4) * 0.45f, (i * 0.137f) % 1f)
    }
}

/**
 * The app-wide background. Replaces flat colour on every screen.
 * The drift only redraws (no recomposition): the animated value is read inside drawBehind.
 */
@Composable
fun Modifier.appBackground(animated: Boolean = true): Modifier {
    val phase = if (animated) {
        rememberInfiniteTransition(label = "backdrop").animateFloat(
            0f, 1f, infiniteRepeatable(tween(26_000, easing = LinearEasing), RepeatMode.Restart), label = "phase"
        )
    } else null
    return this.drawBehind { drawBackdrop(phase?.value ?: 0.15f) }
}

fun DrawScope.drawBackdrop(phase: Float) {
    drawRect(Backdrop.base)
    val w = size.width
    val h = size.height
    val a = phase * 2f * PI.toFloat()
    fun blob(color: Color, alpha: Float, cx: Float, cy: Float, r: Float, dx: Float, dy: Float, k: Float) {
        val c = Offset(cx * w + dx * w * cos(a * k), cy * h + dy * h * sin(a * k))
        drawCircle(Brush.radialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)), c, r * w), r * w, c)
    }
    // Aurora
    blob(Backdrop.blobViolet, 0.55f, 0.05f, 0.04f, 0.95f, 0.07f, 0.03f, 1f)
    blob(Backdrop.blobPink, 0.38f, 1.00f, 0.22f, 0.80f, 0.06f, 0.05f, -1f)
    blob(Backdrop.blobAqua, 0.20f, 0.0f, 0.62f, 0.70f, 0.05f, 0.04f, 2f)
    blob(Backdrop.blobPink, 0.22f, 0.70f, 0.95f, 0.85f, 0.07f, 0.02f, 1f)
    blob(Backdrop.blobViolet, 0.30f, 0.30f, 1.05f, 0.75f, 0.05f, 0.02f, -2f)

    // Two soft light beams sweeping from the top corners
    rotate(-28f + 6f * sin(a), Offset(w * 0.15f, -h * 0.05f)) {
        drawRect(
            Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFFF6FD8).copy(alpha = 0.07f), Color.Transparent), startX = w * 0.05f, endX = w * 0.32f),
            topLeft = Offset(w * 0.05f, -h * 0.05f), size = Size(w * 0.27f, h * 1.3f)
        )
    }
    rotate(24f + 5f * cos(a), Offset(w * 0.9f, -h * 0.05f)) {
        drawRect(
            Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFF6FC8FF).copy(alpha = 0.06f), Color.Transparent), startX = w * 0.72f, endX = w * 0.95f),
            topLeft = Offset(w * 0.72f, -h * 0.05f), size = Size(w * 0.23f, h * 1.3f)
        )
    }

    // Stars
    val unit = w / 400f
    Backdrop.stars.forEach { s ->
        val tw = 0.5f + 0.5f * sin((phase * 6f + s[3]) * 2f * PI.toFloat())
        drawCircle(Color.White.copy(alpha = 0.12f + 0.5f * tw), s[2] * unit, Offset(s[0] * w, s[1] * h))
    }
    // Keep the bottom calm so content reads well
    drawRect(Brush.verticalGradient(0.7f to Color.Transparent, 1f to Backdrop.bottom.copy(alpha = 0.6f)))
}
