package com.live.ishqexpress.ui.gifts

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.common.*
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.home.LiveCreator
import com.live.ishqexpress.ui.home.compact
import com.live.ishqexpress.ui.home.sampleCreators
import com.live.ishqexpress.ui.nav.*
import com.live.ishqexpress.ui.theme.*
import com.live.ishqexpress.ui.home.LiveMode
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws every gift effect for a room on top of the video. Put it above the stage and below the
 * controls; it never takes touches.
 *
 * @param faceAnchor where the host's head is, as a fraction of this layer (AR gifts sit there).
 *   A face-tracking SDK would update this every frame; for now each stage passes its best guess.
 */
@Composable
fun GiftEffectsLayer(
    state: GiftEffectsState,
    modifier: Modifier = Modifier,
    faceAnchor: Offset = Offset(0.5f, 0.28f),
    bannerTop: Dp = 330.dp,
    marqueeTop: Dp = 108.dp,
) {
    Box(modifier.fillMaxSize()) {
        // Entrance (VIP / high level joins)
        state.entrance?.let { e -> key(e.id) { EntranceBanner(e, Modifier.align(Alignment.TopStart).padding(top = bannerTop - 70.dp)) { state.entranceFinished(e.id) } } }

        // Combo banners, newest at the bottom lane
        Column(Modifier.align(Alignment.TopStart).padding(top = bannerTop, start = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.banners.forEach { b -> key(b.key) { ComboBannerView(b) { state.bannerExpired(b.key, b.version) } } }
        }

        // AR on the host's face
        state.ar?.let { e -> key(e.id) { ArEffect(e, faceAnchor) { state.arFinished(e.id) } } }

        // Full-screen motion gift
        state.motion?.let { e -> key(e.id) { MotionGift(e) { state.motionFinished(e.id) } } }

        // Platform-wide marquee for big gifts
        state.marquee?.let { m -> key(m) { Marquee(m, Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = marqueeTop)) { state.marqueeFinished() } } }
    }
}

/* ============================== Combo banner ============================== */

@Composable
private fun ComboBannerView(b: ComboBanner, onExpired: () -> Unit) {
    val e = b.event
    LaunchedEffect(b.version) { delay(3_000); onExpired() }
    val enter = remember { Animatable(-1f) }
    LaunchedEffect(Unit) { enter.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = 300f)) }
    val pop = remember { Animatable(1f) }
    LaunchedEffect(b.version) { pop.snapTo(1.8f); pop.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 400f)) }
    val colors = when {
        e.totalCoins >= 999 -> listOf(Color(0xFFFFB020), Color(0xFFFF2E8B), Color.Transparent)
        else -> listOf(BrandPink.copy(alpha = 0.92f), BrandOrchid.copy(alpha = 0.6f), Color.Transparent)
    }
    Row(
        Modifier
            .graphicsLayer { translationX = enter.value * size.width }
            .height(48.dp)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(colors))
            .padding(start = 4.dp, end = 36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(e.senderAvatar, 40.dp)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.widthIn(max = 120.dp)) {
            Text(e.senderName, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("to ${e.receiver.name.substringBefore(' ')}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
        }
        Spacer(Modifier.width(6.dp))
        Text(e.gift.emoji, fontSize = 34.sp, modifier = Modifier.graphicsLayer { rotationZ = (pop.value - 1f) * 20f })
        Spacer(Modifier.width(6.dp))
        Text(
            "×${b.combo}",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
            color = Gold,
            modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value; transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f) }
        )
    }
}

/* ============================== Motion gifts ============================== */

@Composable
private fun MotionGift(e: GiftEvent, onDone: () -> Unit) {
    val context = LocalContext.current
    val hasVideo = remember(e.gift.video) {
        val path = e.gift.video ?: return@remember false
        runCatching { context.assets.list(path.substringBeforeLast('/'))?.contains(path.substringAfterLast('/')) == true }.getOrDefault(false)
    }
    var videoFailed by remember { mutableStateOf(false) }

    // Scrim between the room and the gift: chat/seats recede so the gift reads as on top of
    // everything (the video itself is transparent around the subject).
    val scrim = remember { Animatable(0f) }
    var ending by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { scrim.animateTo(1f, tween(350)) }
    LaunchedEffect(ending) { if (ending) { scrim.animateTo(0f, tween(250)); onDone() } }

    Box(Modifier.fillMaxSize()) {
        if (hasVideo && !videoFailed) {
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer { alpha = scrim.value }
                    .background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.15f), 0.45f to Color.Black.copy(alpha = 0.45f), 1f to Color.Black.copy(alpha = 0.6f)))
            )
            AndroidView(
                factory = { ctx ->
                    AlphaVideoView(ctx).apply {
                        onFinished = { ok -> if (ok) ending = true else videoFailed = true }
                        play(e.gift.video!!)
                    }
                },
                onRelease = { it.release() },
                modifier = Modifier.fillMaxSize()
            )
            // Safety net if the decoder never reports completion.
            LaunchedEffect(Unit) { delay(12_000); ending = true }
        } else {
            DrawnMotion(e, onDone)
        }
        // Personalised caption (VAP can bake this into the video; an overlay works for any asset).
        MotionCaption(e, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 210.dp))
    }
}

@Composable
private fun MotionCaption(e: GiftEvent, modifier: Modifier) {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { delay(300); a.animateTo(1f, tween(400)) }
    Row(
        modifier
            .graphicsLayer { alpha = a.value; translationY = (1f - a.value) * 40f }
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.55f), BrandViolet.copy(alpha = 0.65f), Color.Black.copy(alpha = 0.55f))))
            .border(1.dp, Gold.copy(alpha = 0.7f), CircleShape)
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(e.senderAvatar, 32.dp)
        Text("  ${e.senderName}", style = MaterialTheme.typography.titleSmall, color = Color.White)
        Text("  sent ${e.gift.name} to  ", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        Text(e.receiver.name.substringBefore(' ') + "  ", style = MaterialTheme.typography.titleSmall, color = Gold)
        Avatar(e.receiver.avatarUrl, 32.dp)
    }
}

/** Drawn motion effects — used until a designer's video exists for the gift. */
@Composable
private fun DrawnMotion(e: GiftEvent, onDone: () -> Unit) {
    val duration = 3_600
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(duration, easing = LinearEasing)); onDone() }
    val v = t.value
    val fade = when { v < 0.08f -> v / 0.08f; v > 0.88f -> (1f - v) / 0.12f; else -> 1f }
    val style = e.gift.motion ?: MotionStyle.Fireworks
    val seed = e.id.toInt()

    Box(Modifier.fillMaxSize().graphicsLayer { alpha = fade }) {
        // Dim + glow behind the effect
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(styleGlow(style).copy(alpha = 0.45f), Color.Black.copy(alpha = 0.35f)))))
        Canvas(Modifier.fillMaxSize()) {
            when (style) {
                MotionStyle.Car -> { speedLines(v, seed); sparkles(v, seed, Gold) }
                MotionStyle.Castle -> { rays(v, Gold); bursts(v, seed, listOf(Gold, BrandPink, Color.White)) }
                MotionStyle.Rocket -> { stars(v, seed); flameTrail(v, seed) }
                MotionStyle.Dragon -> { stars(v, seed); embers(v, seed) }
                MotionStyle.Fireworks -> bursts(v, seed, listOf(BrandPink, Gold, Color(0xFF6EC6FF), Color(0xFF8CF2A8), Color.White))
                MotionStyle.Hearts -> { rings(v, BrandPink); hearts(v, seed) }
                MotionStyle.Portal -> { portal(v); stars(v, seed) }
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = maxWidth; val h = maxHeight
            when (style) {
                MotionStyle.Car -> {
                    // Drive in from the right, brake in the middle, drive off left.
                    val x = when { v < 0.3f -> 1f - ease(v / 0.3f) * 0.5f; v < 0.7f -> 0.5f; else -> 0.5f - ease((v - 0.7f) / 0.3f) * 0.9f }
                    val bounce = if (v in 0.3f..0.7f) sin((v - 0.3f) * 40f) * 4f * (0.7f - v) else 0f
                    Hero(e.gift.emoji, 150.sp, Modifier.offset(x = w * x - 90.dp, y = h * 0.42f + bounce.dp))
                }
                MotionStyle.Castle -> {
                    val rise = ease((v / 0.45f).coerceAtMost(1f))
                    Hero(e.gift.emoji, 190.sp, Modifier.offset(x = w * 0.5f - 115.dp, y = h * (0.9f - rise * 0.5f)))
                }
                MotionStyle.Rocket -> {
                    val y = 1.05f - ease(v) * 1.4f
                    val wob = sin(v * 30f) * 3f
                    Hero(e.gift.emoji, 140.sp, Modifier.offset(x = w * 0.5f - 85.dp + wob.dp, y = h * y), rotate = -45f)
                }
                MotionStyle.Dragon -> {
                    val x = 1.1f - v * 1.5f
                    val y = 0.35f + sin(v * 2 * PI.toFloat() * 1.5f) * 0.08f
                    Hero(e.gift.emoji, 160.sp, Modifier.offset(x = w * x, y = h * y))
                }
                MotionStyle.Fireworks -> Hero(e.gift.emoji, (90 + 40 * sin(v * 12f)).sp, Modifier.offset(x = w * 0.5f - 60.dp, y = h * 0.38f))
                MotionStyle.Hearts -> {
                    val s = 1f + 0.12f * sin(v * 28f)
                    Hero(e.gift.emoji, 170.sp, Modifier.offset(x = w * 0.5f - 105.dp, y = h * 0.36f).graphicsLayer { scaleX = s; scaleY = s })
                }
                MotionStyle.Portal -> {
                    val s = ease((v / 0.4f).coerceAtMost(1f))
                    Hero(e.gift.emoji, 150.sp, Modifier.offset(x = w * 0.5f - 90.dp, y = h * 0.38f).graphicsLayer { scaleX = s; scaleY = s; rotationZ = v * 360f })
                }
            }
            // Title card
            val titleIn = ease(((v - 0.15f) / 0.2f).coerceIn(0f, 1f))
            Text(
                e.gift.name.uppercase(),
                fontSize = 30.sp, fontWeight = FontWeight.Black, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                style = androidx.compose.ui.text.TextStyle(brush = Brush.horizontalGradient(listOf(Gold, Color.White, Gold)), letterSpacing = 2.sp),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = h * 0.22f).graphicsLayer { alpha = titleIn; scaleX = 0.6f + 0.4f * titleIn; scaleY = 0.6f + 0.4f * titleIn }
            )
        }
    }
}

@Composable
private fun Hero(emoji: String, size: TextUnit, modifier: Modifier, mirror: Boolean = false, rotate: Float = 0f) {
    Text(emoji, fontSize = size, modifier = modifier.graphicsLayer { if (mirror) scaleX = -1f; rotationZ = rotate })
}

private fun ease(x: Float) = 1f - (1f - x) * (1f - x) * (1f - x)

private fun styleGlow(s: MotionStyle) = when (s) {
    MotionStyle.Car -> Color(0xFFFF6A00)
    MotionStyle.Castle -> Gold
    MotionStyle.Rocket -> Color(0xFF3B6FE0)
    MotionStyle.Dragon -> Color(0xFFE0245E)
    MotionStyle.Fireworks -> BrandOrchid
    MotionStyle.Hearts -> BrandPink
    MotionStyle.Portal -> BrandViolet
}

/** Deterministic pseudo-random in [0,1) so particles don't jump between frames. */
private fun rnd(seed: Int, i: Int, salt: Int = 0): Float {
    var x = seed * 374761393 + i * 668265263 + salt * 2147483647
    x = (x xor (x ushr 13)) * 1274126177
    return ((x xor (x ushr 16)) and 0xFFFFFF) / 16777216f
}

private fun DrawScope.speedLines(v: Float, seed: Int) {
    repeat(28) { i ->
        val y = size.height * (0.3f + rnd(seed, i) * 0.4f)
        val len = size.width * (0.15f + rnd(seed, i, 1) * 0.3f)
        val x = size.width * (1.2f - ((v * 2.2f + rnd(seed, i, 2)) % 1.4f))
        drawLine(Color.White.copy(alpha = 0.5f * rnd(seed, i, 3) + 0.2f), Offset(x, y), Offset(x + len, y), strokeWidth = 3f + rnd(seed, i, 4) * 5f)
    }
}

private fun DrawScope.sparkles(v: Float, seed: Int, color: Color) {
    repeat(30) { i ->
        val life = (v * 3f + rnd(seed, i)) % 1f
        val c = Offset(size.width * rnd(seed, i, 5), size.height * (0.25f + rnd(seed, i, 6) * 0.5f))
        drawCircle(color.copy(alpha = (1f - life) * 0.9f), radius = 3f + 8f * (1f - life), center = c)
    }
}

private fun DrawScope.rays(v: Float, color: Color) {
    val c = Offset(size.width / 2, size.height * 0.45f)
    rotate(v * 90f, c) {
        repeat(16) { i ->
            val a = i * (2 * PI / 16).toFloat()
            drawLine(
                Brush.linearGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), c, Offset(c.x + cos(a) * size.width, c.y + sin(a) * size.width)),
                c, Offset(c.x + cos(a) * size.width, c.y + sin(a) * size.width), strokeWidth = 40f
            )
        }
    }
}

private fun DrawScope.bursts(v: Float, seed: Int, colors: List<Color>) {
    repeat(6) { b ->
        val start = b * 0.13f
        val p = ((v - start) / 0.45f)
        if (p !in 0f..1f) return@repeat
        val c = Offset(size.width * (0.18f + rnd(seed, b, 7) * 0.64f), size.height * (0.15f + rnd(seed, b, 8) * 0.45f))
        val col = colors[b % colors.size]
        repeat(22) { i ->
            val a = i * (2 * PI / 22).toFloat()
            val r = ease(p) * size.width * 0.22f
            val pos = Offset(c.x + cos(a) * r, c.y + sin(a) * r + p * p * 60f)
            drawCircle(col.copy(alpha = (1f - p)), radius = 6f * (1f - p) + 2f, center = pos)
        }
        drawCircle(Color.White.copy(alpha = (1f - p) * 0.6f), radius = 30f * (1f - p), center = c)
    }
}

private fun DrawScope.stars(v: Float, seed: Int) {
    repeat(60) { i ->
        val tw = 0.4f + 0.6f * ((sin(v * 20f + i) + 1f) / 2f)
        drawCircle(Color.White.copy(alpha = 0.7f * tw), radius = 1.5f + rnd(seed, i, 9) * 2.5f, center = Offset(size.width * rnd(seed, i, 10), size.height * rnd(seed, i, 11)))
    }
}

private fun DrawScope.flameTrail(v: Float, seed: Int) {
    val y0 = size.height * (1.05f - ease(v) * 1.4f) + 330f
    repeat(40) { i ->
        val life = rnd(seed, i)
        val pos = Offset(size.width / 2 - 40f + (rnd(seed, i, 12) - 0.5f) * 60f * (1 + life * 2), y0 + life * 520f)
        drawCircle(lerpColor(Color(0xFFFFE08A), Color(0xFFFF4D00), life).copy(alpha = (1f - life) * 0.85f), radius = 22f * (1f - life) + 4f, center = pos)
    }
}

private fun DrawScope.embers(v: Float, seed: Int) {
    repeat(40) { i ->
        val life = (v * 2f + rnd(seed, i)) % 1f
        val pos = Offset(size.width * rnd(seed, i, 13), size.height * (0.2f + life * 0.6f))
        drawCircle(Color(0xFFFF6A00).copy(alpha = (1f - life) * 0.8f), radius = 3f + 5f * (1f - life), center = pos)
    }
}

private fun DrawScope.rings(v: Float, color: Color) {
    val c = Offset(size.width / 2, size.height * 0.45f)
    repeat(4) { i ->
        val p = (v * 2f + i * 0.25f) % 1f
        drawCircle(color.copy(alpha = (1f - p) * 0.8f), radius = p * size.width * 0.7f, center = c, style = Stroke(width = 8f * (1f - p) + 2f))
    }
}

private fun DrawScope.hearts(v: Float, seed: Int) {
    repeat(26) { i ->
        val life = (v * 1.6f + rnd(seed, i)) % 1f
        val x = size.width * rnd(seed, i, 14) + sin(life * 8f + i) * 30f
        val y = size.height * (1.05f - life * 1.1f)
        heart(Offset(x, y), 16f + rnd(seed, i, 15) * 26f, lerpColor(BrandPink, Color(0xFFFF7AB6), rnd(seed, i, 16)).copy(alpha = 0.85f * (1f - life * 0.6f)))
    }
}

private fun DrawScope.heart(c: Offset, s: Float, color: Color) {
    drawCircle(color, s * 0.5f, Offset(c.x - s * 0.35f, c.y))
    drawCircle(color, s * 0.5f, Offset(c.x + s * 0.35f, c.y))
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(c.x - s * 0.82f, c.y + s * 0.12f); lineTo(c.x + s * 0.82f, c.y + s * 0.12f); lineTo(c.x, c.y + s * 1.05f); close()
    }
    drawPath(p, color)
}

private fun DrawScope.portal(v: Float) {
    val c = Offset(size.width / 2, size.height * 0.45f)
    repeat(7) { i ->
        rotate(v * 360f * (if (i % 2 == 0) 1 else -1) + i * 25f, c) {
            drawArc(
                Brush.sweepGradient(listOf(Color.Transparent, BrandOrchid, Color(0xFF6EC6FF), Color.Transparent), c),
                0f, 260f, false,
                topLeft = Offset(c.x - (60f + i * 45f), c.y - (60f + i * 45f)),
                size = androidx.compose.ui.geometry.Size((60f + i * 45f) * 2, (60f + i * 45f) * 2),
                style = Stroke(width = 10f - i)
            )
        }
    }
}

private fun lerpColor(a: Color, b: Color, f: Float) = androidx.compose.ui.graphics.lerp(a, b, f.coerceIn(0f, 1f))

/* ============================== AR ============================== */

@Composable
private fun ArEffect(e: GiftEvent, anchor: Offset, onDone: () -> Unit) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(6_000, easing = LinearEasing)); onDone() }
    val v = t.value
    val appear = ease((v / 0.1f).coerceAtMost(1f))
    val fade = if (v > 0.9f) (1f - v) / 0.1f else 1f
    BoxWithConstraints(Modifier.fillMaxSize().graphicsLayer { alpha = fade }) {
        val cx = maxWidth * anchor.x
        val cy = maxHeight * anchor.y
        when (e.gift.id) {
            13 -> repeat(5) { i -> // butterflies orbiting the head
                val a = v * 2 * PI.toFloat() * 1.5f + i * (2 * PI.toFloat() / 5)
                Text("🦋", fontSize = 30.sp, modifier = Modifier.offset(x = cx + (cos(a) * 90).dp - 18.dp, y = cy + (sin(a) * 40).dp - 18.dp).graphicsLayer { scaleX = appear; scaleY = appear })
            }
            24 -> Text("🐰", fontSize = 80.sp, modifier = Modifier.offset(x = cx - 48.dp, y = cy - 140.dp).graphicsLayer { scaleX = appear; scaleY = appear; rotationZ = sin(v * 30f) * 4f })
            25 -> Text("🕶️", fontSize = 76.sp, modifier = Modifier.offset(x = cx - 46.dp, y = cy - 20.dp + ((1 - appear) * -200).dp))
            else -> Text(e.gift.emoji, fontSize = 84.sp, modifier = Modifier.offset(x = cx - 50.dp, y = cy - 150.dp + (sin(v * 12f) * 4).dp).graphicsLayer { scaleX = appear; scaleY = appear })
        }
        Text(
            "AR · ${e.gift.name} from ${e.senderName}", style = MaterialTheme.typography.labelSmall, color = Color.White,
            modifier = Modifier.offset(x = cx - 80.dp, y = cy + 70.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)).padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/* ============================== Marquee ============================== */

@Composable
private fun Marquee(text: String, modifier: Modifier, onDone: () -> Unit) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(7_000, easing = LinearEasing)); onDone() }
    BoxWithConstraints(modifier.fillMaxWidth().height(34.dp).clipToBounds()) {
        val w = maxWidth
        Row(
            Modifier
                .offset(x = w - (w * 2.2f) * t.value)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Color(0xFFFFB020), Color(0xFFFF2E8B), BrandViolet)))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📣 ", fontSize = 13.sp)
            Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = Color.White, maxLines = 1, softWrap = false)
            Text("  · Tap to watch", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), softWrap = false)
        }
    }
}

/* ============================== Entrance ============================== */

@Composable
private fun EntranceBanner(e: Entrance, modifier: Modifier, onDone: () -> Unit) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(3_200, easing = LinearEasing)); onDone() }
    val v = t.value
    val x = when { v < 0.18f -> 1f - ease(v / 0.18f); v < 0.82f -> 0f; else -> -ease((v - 0.82f) / 0.18f) }
    val tier = vipTiers[(e.vip - 1).coerceIn(0, vipTiers.lastIndex)]
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val w = maxWidth
        Row(
            Modifier
                .offset(x = w * x + 8.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp, topStart = 6.dp, bottomStart = 6.dp))
                .background(Brush.horizontalGradient(listOf(tier.color, tier.color.copy(alpha = 0.6f), Color.Transparent)))
                .padding(start = 4.dp, end = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(e.avatar, 32.dp)
            Spacer(Modifier.width(6.dp))
            LevelBadge(e.level)
            Spacer(Modifier.width(4.dp))
            if (e.vip > 0) { VipBadge(e.vip); Spacer(Modifier.width(4.dp)) }
            Text(e.name, style = MaterialTheme.typography.labelLarge, color = Color.White)
            Text(" joined ", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
            Text(e.ride, fontSize = 24.sp, modifier = Modifier.graphicsLayer { scaleX = -1f })
        }
    }
}
