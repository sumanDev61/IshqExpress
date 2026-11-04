package com.live.ishqexpress.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/*
 * Rich-surface helpers for the dark theme: glass cards with neon edges, a glow under raised
 * elements, a light sweep across featured items and a press-to-tilt 3D effect.
 * All of them only change drawing (graphicsLayer / draw modifiers), so they're cheap.
 */

/** Frosted glass panel on the night canvas: translucent body, neon hairline edge, top sheen, coloured glow. */
fun Modifier.glass(shape: Shape, glow: Color = NeonViolet, elevation: Dp = 10.dp, edge: Brush? = null): Modifier =
    this
        .shadow(elevation, shape, ambientColor = glow.copy(alpha = 0.35f), spotColor = glow.copy(alpha = 0.55f))
        .clip(shape)
        .background(BrandBrush.glass)
        .border(
            1.dp,
            edge ?: Brush.linearGradient(listOf(Color.White.copy(alpha = 0.22f), NeonPink.copy(alpha = 0.18f), NeonCyan.copy(alpha = 0.10f), Color.White.copy(alpha = 0.04f))),
            shape
        )
        .drawWithContent {
            drawContent()
            drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = 0.07f), 0.3f to Color.Transparent))
        }

/** Rotating pink → violet → cyan border, for highlighted / selected items. */
fun Modifier.neonBorder(shape: Shape, width: Dp = 1.5.dp, animated: Boolean = true): Modifier = composed {
    val angle = if (animated) rememberInfiniteTransition(label = "neon").animateFloat(
        0f, 1f, infiniteRepeatable(tween(4_000, easing = LinearEasing)), label = "a"
    ).value else 0f
    // Shift every colour stop toward the next one, so the gradient appears to travel round the edge.
    val c = listOf(NeonPink, NeonViolet, NeonCyan)
    val stops = (0..c.size).map { k ->
        val pos = k + angle * c.size
        val i = pos.toInt()
        androidx.compose.ui.graphics.lerp(c[i % c.size], c[(i + 1) % c.size], pos - i)
    }
    this.border(width, Brush.sweepGradient(stops), shape)
}

/** A slanted band of light that sweeps across the content every few seconds. */
fun Modifier.shine(periodMs: Int = 3_600, alpha: Float = 0.22f): Modifier = composed {
    val p = rememberInfiniteTransition(label = "shine").animateFloat(
        -0.6f, 1.6f, infiniteRepeatable(tween(periodMs, easing = LinearEasing), RepeatMode.Restart), label = "p"
    )
    this.drawWithContent {
        drawContent()
        val x = p.value * size.width
        val band = size.width * 0.35f
        drawRect(
            Brush.linearGradient(
                0f to Color.Transparent, 0.5f to Color.White.copy(alpha = alpha), 1f to Color.Transparent,
                start = Offset(x - band, 0f), end = Offset(x + band * 0.4f, size.height)
            )
        )
    }
}

/**
 * Tilts toward the finger while pressed (real 3D rotation with perspective) and springs back.
 * Doesn't consume touches, so clicks underneath still work.
 */
fun Modifier.tilt3D(maxDeg: Float = 9f, pressScale: Float = 0.97f): Modifier = composed {
    val rx = remember { Animatable(0f) }
    val ry = remember { Animatable(0f) }
    val sc = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val springy = spring<Float>(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)
    this
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                val nx = (down.position.x / size.width - 0.5f) * 2f
                val ny = (down.position.y / size.height - 0.5f) * 2f
                scope.launch { rx.animateTo(-ny * maxDeg, springy) }
                scope.launch { ry.animateTo(nx * maxDeg, springy) }
                scope.launch { sc.animateTo(pressScale, springy) }
                waitForUpOrCancellation(PointerEventPass.Initial)
                scope.launch { rx.animateTo(0f, springy) }
                scope.launch { ry.animateTo(0f, springy) }
                scope.launch { sc.animateTo(1f, springy) }
            }
        }
        .graphicsLayer {
            rotationX = rx.value
            rotationY = ry.value
            scaleX = sc.value
            scaleY = sc.value
            cameraDistance = 14f * density
        }
}

/** Gentle up-down float, for hero icons and badges. */
fun Modifier.floating(amplitude: Dp = 4.dp, periodMs: Int = 2_400): Modifier = composed {
    val t = rememberInfiniteTransition(label = "float").animateFloat(
        -1f, 1f, infiniteRepeatable(tween(periodMs), RepeatMode.Reverse), label = "y"
    )
    this.graphicsLayer { translationY = t.value * amplitude.toPx() }
}

/** Neon gradient text (pink → violet → cyan by default). */
@Composable
fun GradientText(text: String, style: TextStyle, modifier: Modifier = Modifier, brush: Brush = BrandBrush.neon) {
    Text(text, modifier, style = style.copy(brush = brush))
}
