package com.live.ishqexpress.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Two-stop colour ramp used by the 3D icons: light top, deep bottom. */
@Immutable
data class Ramp(val light: Color, val deep: Color) {
    val brush get() = Brush.verticalGradient(listOf(light, deep))
    val diagonal get() = Brush.linearGradient(listOf(light, deep))

    companion object {
        /** A ramp from one accent colour: brightened at the top, slightly darker at the bottom. */
        fun of(c: Color) = Ramp(lerp(c, Color.White, 0.28f), lerp(c, Color.Black, 0.12f))
    }
}

object Ramps {
    val home = Ramp(Color(0xFFFF7AB2), Color(0xFFFF2E6E))
    val explore = Ramp(Color(0xFF5FD6FF), Color(0xFF7A2BE8))
    val chats = Ramp(Color(0xFFC77DFF), Color(0xFFFF2E8B))
    val me = Ramp(Color(0xFFFFB35C), Color(0xFFFF3D7F))
    val shorts = Ramp(Color(0xFF6FE7FF), Color(0xFFB45CFF))
    val live = Ramp(Color(0xFFFF4F9A), Color(0xFF7A2BE8))
    val muted = Ramp(Color(0xFFC9C2DA), Color(0xFF8C84A6))
}

/**
 * A vector icon rendered as a glossy 3D glyph: gradient body, specular highlight on the top half,
 * a darker rim at the bottom, and a soft drop shadow underneath.
 */
@Composable
fun GlossyIcon(icon: ImageVector, ramp: Ramp, size: Dp, modifier: Modifier = Modifier, contentDescription: String? = null, shadow: Boolean = true) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (shadow) {
            Icon(
                icon, null, tint = ramp.deep.copy(alpha = 0.28f),
                modifier = Modifier.fillMaxSize().offset(y = size * 0.07f).graphicsLayer { scaleX = 0.96f; scaleY = 0.96f }
            )
        }
        Icon(
            icon, contentDescription, tint = Color.White,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(ramp.brush, blendMode = BlendMode.SrcIn)
                    drawRect(
                        Brush.verticalGradient(0f to Color.White.copy(alpha = 0.55f), 0.42f to Color.White.copy(alpha = 0.08f), 0.5f to Color.Transparent),
                        blendMode = BlendMode.SrcAtop
                    )
                    drawRect(
                        Brush.verticalGradient(0.62f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.16f)),
                        blendMode = BlendMode.SrcAtop
                    )
                }
        )
    }
}

/**
 * A raised, glossy tile (rounded square or circle) with a coloured glow, a glass highlight across
 * the top and a soft inner shade at the bottom. Content is centred on top.
 */
@Composable
fun Bubble3D(
    ramp: Ramp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    elevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .shadow(elevation, shape, ambientColor = ramp.deep.copy(alpha = 0.5f), spotColor = ramp.deep)
            .clip(shape)
            .background(ramp.diagonal)
            .drawWithContent {
                // Bottom inner shade, then content, then the glass highlight over everything.
                drawRect(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.16f)))
                drawContent()
                drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = 0.50f), 0.46f to Color.White.copy(alpha = 0.10f), 0.5f to Color.Transparent))
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}

/** White glyph with a small drop shadow, for use on a [Bubble3D]. */
@Composable
fun BubbleGlyph(icon: ImageVector, size: Dp, contentDescription: String? = null) {
    Box(Modifier.size(size)) {
        Icon(icon, null, tint = Color.Black.copy(alpha = 0.18f), modifier = Modifier.fillMaxSize().offset(y = 1.dp))
        Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.fillMaxSize())
    }
}

/** A 3D icon tile in one call: accent colour → glossy tile with a white glyph. */
@Composable
fun IconTile3D(icon: ImageVector, color: Color, modifier: Modifier = Modifier, size: Dp = 40.dp, glyph: Dp = size * 0.5f, corner: Dp = size * 0.32f) {
    Bubble3D(Ramp.of(color), modifier.size(size), RoundedCornerShape(corner), elevation = size * 0.16f) { BubbleGlyph(icon, glyph) }
}

/**
 * Single-line text that shrinks (down to [minSize]) instead of wrapping or clipping on narrow
 * screens and large font settings.
 */
@Composable
fun FitText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    minSize: TextUnit = 9.sp,
    textAlign: TextAlign? = null,
) {
    val s = style.merge(TextStyle(color = color, textAlign = textAlign ?: TextAlign.Unspecified))
    BasicText(
        text, modifier, style = s, maxLines = 1, overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(minFontSize = minSize, maxFontSize = if (style.fontSize.isSp) style.fontSize else 16.sp, stepSize = 0.5.sp)
    )
}
