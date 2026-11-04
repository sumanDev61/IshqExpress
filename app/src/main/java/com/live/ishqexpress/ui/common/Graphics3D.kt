package com.live.ishqexpress.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/*
 * Hand-drawn 3D graphics (no image assets): glossy coins, coin stacks, a faceted diamond and
 * twinkling sparkles. Drawn on Canvas, so they stay sharp at any size and cost almost nothing.
 */

private val goldLight = Color(0xFFFFF0A8)
private val goldMid = Color(0xFFFFC53D)
private val goldDeep = Color(0xFFE08A00)
private val goldEdge = Color(0xFFB36200)

/** One glossy coin seen face-on with a thick rim, embossed ring and a specular highlight. */
fun DrawScope.coin(center: Offset, r: Float, tilt: Float = 0.0f) {
    // tilt squashes the coin vertically to show its edge (0 = face-on, 0.5 = strongly tilted)
    val ry = r * (1f - tilt)
    val edge = r * 0.16f * (0.35f + tilt * 1.6f)
    // Rim / side
    drawOval(Brush.verticalGradient(listOf(goldDeep, goldEdge), startY = center.y - ry, endY = center.y + ry + edge),
        topLeft = Offset(center.x - r, center.y - ry + edge), size = Size(r * 2, ry * 2))
    // Face
    drawOval(Brush.radialGradient(listOf(goldLight, goldMid, goldDeep), Offset(center.x - r * 0.35f, center.y - ry * 0.45f), r * 1.5f),
        topLeft = Offset(center.x - r, center.y - ry), size = Size(r * 2, ry * 2))
    // Embossed inner ring
    val ir = r * 0.74f; val iry = ry * 0.74f
    drawOval(goldEdge.copy(alpha = 0.55f), Offset(center.x - ir, center.y - iry), Size(ir * 2, iry * 2), style = Stroke(r * 0.07f))
    drawOval(goldLight.copy(alpha = 0.8f), Offset(center.x - ir, center.y - iry - r * 0.03f), Size(ir * 2, iry * 2), style = Stroke(r * 0.035f))
    // Heart emblem
    val hs = r * 0.36f
    val hc = Offset(center.x, center.y + ry * 0.04f)
    val heart = Path().apply {
        moveTo(hc.x, hc.y + hs * (1f - tilt))
        cubicTo(hc.x - hs * 1.9f, hc.y - hs * 0.3f * (1 - tilt), hc.x - hs * 0.8f, hc.y - hs * 1.5f * (1 - tilt), hc.x, hc.y - hs * 0.45f * (1 - tilt))
        cubicTo(hc.x + hs * 0.8f, hc.y - hs * 1.5f * (1 - tilt), hc.x + hs * 1.9f, hc.y - hs * 0.3f * (1 - tilt), hc.x, hc.y + hs * (1f - tilt))
        close()
    }
    translateDraw(0f, r * 0.04f) { drawPath(heart, goldEdge.copy(alpha = 0.6f)) }
    drawPath(heart, Brush.verticalGradient(listOf(Color(0xFFFFF7CC), goldMid), startY = hc.y - hs, endY = hc.y + hs))
    // Specular highlight
    drawOval(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0f)), Offset(center.x - r * 0.38f, center.y - ry * 0.5f), r * 0.55f),
        topLeft = Offset(center.x - r * 0.9f, center.y - ry * 0.95f), size = Size(r * 1.1f, ry * 0.9f))
}

private inline fun DrawScope.translateDraw(dx: Float, dy: Float, block: DrawScope.() -> Unit) {
    drawContext.transform.translate(dx, dy); block(); drawContext.transform.translate(-dx, -dy)
}

/** Faceted gem with crown and pavilion facets, edge light and a glint. */
fun DrawScope.diamond(c: Offset, w: Float, light: Color = Color(0xFFFFB3E0), mid: Color = Color(0xFFFF3EA5), deep: Color = Color(0xFF7A2BE8)) {
    val h = w * 0.86f
    val top = c.y - h * 0.42f
    val girdle = c.y - h * 0.12f
    val bottom = c.y + h * 0.5f
    val l = c.x - w / 2; val r = c.x + w / 2
    val tl = c.x - w * 0.28f; val tr = c.x + w * 0.28f
    fun poly(p: List<Offset>) = Path().apply { moveTo(p[0].x, p[0].y); p.drop(1).forEach { lineTo(it.x, it.y) }; close() }
    // Crown facets (top band)
    drawPath(poly(listOf(Offset(l, girdle), Offset(tl, top), Offset(c.x - w * 0.1f, girdle))), light)
    drawPath(poly(listOf(Offset(tl, top), Offset(tr, top), Offset(c.x + w * 0.1f, girdle), Offset(c.x - w * 0.1f, girdle))), Brush.verticalGradient(listOf(Color.White, light), startY = top, endY = girdle))
    drawPath(poly(listOf(Offset(tr, top), Offset(r, girdle), Offset(c.x + w * 0.1f, girdle))), mid)
    // Pavilion facets (bottom point)
    drawPath(poly(listOf(Offset(l, girdle), Offset(c.x - w * 0.1f, girdle), Offset(c.x, bottom))), Brush.linearGradient(listOf(mid, deep), Offset(l, girdle), Offset(c.x, bottom)))
    drawPath(poly(listOf(Offset(c.x - w * 0.1f, girdle), Offset(c.x + w * 0.1f, girdle), Offset(c.x, bottom))), Brush.verticalGradient(listOf(light, mid), startY = girdle, endY = bottom))
    drawPath(poly(listOf(Offset(c.x + w * 0.1f, girdle), Offset(r, girdle), Offset(c.x, bottom))), deep)
    // Edges
    val outline = poly(listOf(Offset(l, girdle), Offset(tl, top), Offset(tr, top), Offset(r, girdle), Offset(c.x, bottom)))
    drawPath(outline, Color.White.copy(alpha = 0.55f), style = Stroke(w * 0.025f))
    drawLine(Color.White.copy(alpha = 0.6f), Offset(l, girdle), Offset(r, girdle), w * 0.02f)
    sparkle(Offset(tl + w * 0.05f, top + h * 0.06f), w * 0.16f, 1f)
}

/** Four-point star glint. */
fun DrawScope.sparkle(c: Offset, r: Float, alpha: Float) {
    if (alpha <= 0.01f) return
    val p = Path().apply {
        moveTo(c.x, c.y - r)
        quadraticTo(c.x, c.y, c.x + r, c.y)
        quadraticTo(c.x, c.y, c.x, c.y + r)
        quadraticTo(c.x, c.y, c.x - r, c.y)
        quadraticTo(c.x, c.y, c.x, c.y - r)
        close()
    }
    drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.5f * alpha), Color.Transparent), c, r * 1.1f), r * 1.1f, c)
    drawPath(p, Color.White.copy(alpha = alpha))
}

/** Glossy 3D coin icon. */
@Composable
fun Coin3D(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) { coin(Offset(this.size.width / 2, this.size.height * 0.46f), this.size.minDimension * 0.46f, tilt = 0.06f) }
}

/** Faceted 3D diamond icon. */
@Composable
fun Diamond3D(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) { diamond(Offset(this.size.width / 2, this.size.height / 2), this.size.minDimension * 0.92f) }
}

/**
 * A pile of tilted coins that grows with [tier] (1–5), with twinkling glints. Tier 5 adds a gem.
 */
@Composable
fun CoinStack3D(tier: Int, modifier: Modifier = Modifier.size(64.dp)) {
    val t by rememberInfiniteTransition(label = "stack").animateFloat(0f, 1f, infiniteRepeatable(tween(2_400, easing = LinearEasing)), label = "t")
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val r = w * 0.2f
            // Glow
            drawCircle(Brush.radialGradient(listOf(goldMid.copy(alpha = 0.35f), Color.Transparent), Offset(w / 2, h * 0.6f), w * 0.55f), w * 0.55f, Offset(w / 2, h * 0.6f))
            val columns = when (tier.coerceIn(1, 5)) { 1 -> listOf(0f to 2); 2 -> listOf(0f to 4); 3 -> listOf(-0.22f to 3, 0.22f to 5); 4 -> listOf(-0.28f to 4, 0.28f to 3, 0f to 6); else -> listOf(-0.3f to 5, 0.3f to 4, 0f to 7) }
            columns.forEach { (dx, n) ->
                repeat(n) { i -> coin(Offset(w / 2 + dx * w, h * 0.86f - r * 0.5f - i * r * 0.34f), r, tilt = 0.55f) }
            }
            // A loose coin face-on in front
            coin(Offset(w * 0.27f, h * 0.8f), r * 0.75f, tilt = 0.1f)
            if (tier >= 5) diamond(Offset(w * 0.74f, h * 0.3f), w * 0.34f)
            // Glints
            listOf(0f, 0.33f, 0.66f).forEachIndexed { i, o ->
                val ph = (t + o) % 1f
                val a = 1f - abs(ph * 2f - 1f)
                val ang = (i * 2.1f + 0.6f) * PI.toFloat()
                sparkle(Offset(w / 2 + cos(ang) * w * 0.32f, h * 0.45f + sin(ang) * h * 0.25f), w * 0.09f * a, a)
            }
        }
    }
}

/** Twinkling glints over any area (hero cards, VIP banners). */
@Composable
fun Sparkles(modifier: Modifier = Modifier, count: Int = 6) {
    val t by rememberInfiniteTransition(label = "sparkles").animateFloat(0f, 1f, infiniteRepeatable(tween(3_000, easing = LinearEasing)), label = "t")
    Canvas(modifier.fillMaxSize()) {
        repeat(count) { i ->
            val ph = (t + i / count.toFloat()) % 1f
            val a = 1f - abs(ph * 2f - 1f)
            val x = size.width * ((i * 0.618f + 0.1f) % 1f)
            val y = size.height * ((i * 0.37f + 0.15f) % 1f)
            sparkle(Offset(x, y), size.minDimension * 0.06f * a, a * 0.9f)
        }
    }
}
