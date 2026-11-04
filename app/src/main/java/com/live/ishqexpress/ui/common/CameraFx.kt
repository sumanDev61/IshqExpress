package com.live.ishqexpress.ui.common

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.live.ishqexpress.data.Session
import com.live.ishqexpress.ui.theme.BrandPink
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Camera effects that run fully on the phone: colour filters, beauty, face masks and scenes.
 *
 * - Filters, brighten, rosy and smooth skin are a colour matrix (+ a soft-focus blur on Android 12+)
 *   applied to the camera view itself, so what you see is what viewers would get.
 * - Masks follow your face using ML Kit face detection on the camera frames (see CameraPreview).
 * - Slim face / big eyes are approximated with a gentle frame warp; true face-mesh reshaping needs
 *   a beauty SDK (Banuba, DeepAR, the streaming SDK's beauty module) when the backend is wired up.
 *
 * Every choice is saved on the device so Go Live setup, the live itself and calls share it.
 */

data class FxItem(val name: String, val emoji: String)

val cameraFilters = listOf(
    FxItem("None", "⭕"), FxItem("Warm", "🌅"), FxItem("Cool", "❄️"), FxItem("Pink", "🌸"),
    FxItem("Vintage", "📷"), FxItem("B&W", "⚫"), FxItem("Glow", "✨"), FxItem("Vivid", "🌈"), FxItem("Film", "🎞️"),
)
val cameraMasks = listOf(
    FxItem("None", "⭕"), FxItem("Bunny", "🐰"), FxItem("Cat", "🐱"), FxItem("Crown", "👑"), FxItem("Hearts", "💕"),
    FxItem("Glasses", "🕶️"), FxItem("Devil", "😈"), FxItem("Flowers", "🌸"), FxItem("Blush", "😊"),
)
val cameraScenes = listOf(
    FxItem("None", "⭕"), FxItem("Blur", "🌫️"), FxItem("Studio", "🎙️"), FxItem("Beach", "🏖️"), FxItem("Neon", "🌃"),
    FxItem("Hearts", "💗"), FxItem("Space", "🌌"), FxItem("Snow", "❄️"),
)

data class BeautyPreset(val name: String, val smooth: Float, val brighten: Float, val rosy: Float, val slim: Float, val eyes: Float)

val beautyPresets = listOf(
    BeautyPreset("Off", 0f, 0f, 0f, 0f, 0f),
    BeautyPreset("Natural", 0.35f, 0.2f, 0.15f, 0.1f, 0.05f),
    BeautyPreset("Soft", 0.7f, 0.35f, 0.25f, 0.15f, 0.1f),
    BeautyPreset("Glam", 0.6f, 0.45f, 0.45f, 0.4f, 0.35f),
)

object CameraFx {
    private fun f(key: String, d: Float) = Session.int("fx.$key", (d * 100).roundToInt()) / 100f

    var smooth by mutableFloatStateOf(f("smooth", 0.35f))
    var brighten by mutableFloatStateOf(f("brighten", 0.2f))
    var rosy by mutableFloatStateOf(f("rosy", 0.15f))
    var slim by mutableFloatStateOf(f("slim", 0.1f))
    var eyes by mutableFloatStateOf(f("eyes", 0.05f))
    var filter by mutableIntStateOf(Session.int("fx.filter", 0))
    var filterStrength by mutableFloatStateOf(f("filterStrength", 0.8f))
    var mask by mutableIntStateOf(Session.int("fx.mask", 0))
    var scene by mutableIntStateOf(Session.int("fx.scene", 0))

    /** True while "hold to compare" is pressed: shows the raw camera. */
    var bypass by mutableStateOf(false)

    /** Latest face box, normalised to the upright camera frame (0..1), or null when no face is seen. */
    var face by mutableStateOf<Rect?>(null)
    /** Upright analysis frame size, used to map [face] onto a FILL_CENTER preview. */
    var frame by mutableStateOf(Size(480f, 640f))

    val anyBeauty get() = smooth + brighten + rosy + slim + eyes > 0.01f
    val active get() = !bypass && (anyBeauty || filter != 0)
    val wantsFace get() = mask != 0

    fun save() {
        listOf("smooth" to smooth, "brighten" to brighten, "rosy" to rosy, "slim" to slim, "eyes" to eyes, "filterStrength" to filterStrength)
            .forEach { (k, v) -> Session.setInt("fx.$k", (v * 100).roundToInt()) }
        Session.setInt("fx.filter", filter); Session.setInt("fx.mask", mask); Session.setInt("fx.scene", scene)
    }

    fun apply(p: BeautyPreset) { smooth = p.smooth; brighten = p.brighten; rosy = p.rosy; slim = p.slim; eyes = p.eyes; save() }

    fun reset() { apply(beautyPresets[0]); filter = 0; mask = 0; scene = 0; filterStrength = 0.8f; save() }

    /** Colour part of the effect (filter + brighten + rosy + smooth's softer contrast). */
    fun colorMatrix(filterIndex: Int = filter, strength: Float = filterStrength, beauty: Boolean = true): ColorMatrix {
        val m = ColorMatrix(filterMatrix(filterIndex, strength))
        if (beauty) {
            if (brighten > 0f) m.postConcat(brightness(brighten))
            if (rosy > 0f) m.postConcat(ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, 14f * rosy,
                0f, 1f - 0.04f * rosy, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 8f * rosy,
                0f, 0f, 0f, 1f, 0f,
            )))
            if (smooth > 0f) m.postConcat(contrast(1f - 0.12f * smooth))
        }
        return m
    }

    /** Compose graphics layer for the camera surface. Null when nothing needs drawing. */
    fun renderEffect(): androidx.compose.ui.graphics.RenderEffect? {
        if (Build.VERSION.SDK_INT < 31 || !active) return null
        val color = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(colorMatrix()))
        if (smooth <= 0.01f) return color.asComposeRenderEffect()
        // Soft-focus skin: a blurred copy laid over the sharp image at partial opacity.
        val r = 4f + 10f * smooth
        val blur = RenderEffect.createBlurEffect(r, r, color, Shader.TileMode.CLAMP)
        val faded = RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(ColorMatrix().apply { setScale(1f, 1f, 1f, 0.55f * smooth) }), blur)
        return RenderEffect.createBlendModeEffect(color, faded, android.graphics.BlendMode.SRC_OVER).asComposeRenderEffect()
    }

    private fun brightness(v: Float): ColorMatrix {
        val k = 1f + 0.22f * v
        val o = 10f * v
        return ColorMatrix(floatArrayOf(k, 0f, 0f, 0f, o, 0f, k, 0f, 0f, o, 0f, 0f, k, 0f, o, 0f, 0f, 0f, 1f, 0f))
    }

    private fun contrast(c: Float): ColorMatrix {
        val t = (1f - c) * 128f
        return ColorMatrix(floatArrayOf(c, 0f, 0f, 0f, t, 0f, c, 0f, 0f, t, 0f, 0f, c, 0f, t, 0f, 0f, 0f, 1f, 0f))
    }

    private fun saturation(s: Float) = ColorMatrix().apply { setSaturation(s) }

    private fun scale(r: Float, g: Float, b: Float, or: Float = 0f, og: Float = 0f, ob: Float = 0f) =
        ColorMatrix(floatArrayOf(r, 0f, 0f, 0f, or, 0f, g, 0f, 0f, og, 0f, 0f, b, 0f, ob, 0f, 0f, 0f, 1f, 0f))

    fun filterMatrix(i: Int, strength: Float): FloatArray {
        val full = when (i) {
            1 -> scale(1.12f, 1.02f, 0.86f, 8f, 4f, 0f)                                   // Warm
            2 -> scale(0.9f, 1.0f, 1.15f, 0f, 4f, 10f)                                   // Cool
            3 -> scale(1.1f, 0.95f, 1.05f, 18f, 0f, 12f)                                 // Pink
            4 -> ColorMatrix(floatArrayOf(                                                // Vintage (sepia-ish)
                0.5f, 0.6f, 0.2f, 0f, 14f,
                0.35f, 0.6f, 0.15f, 0f, 8f,
                0.25f, 0.45f, 0.2f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f))
            5 -> saturation(0f).apply { postConcat(contrast(1.15f)) }                    // B&W
            6 -> brightness(0.6f).apply { postConcat(saturation(1.15f)) }                // Glow
            7 -> saturation(1.6f).apply { postConcat(contrast(1.1f)) }                   // Vivid
            8 -> saturation(0.75f).apply { postConcat(scale(1.04f, 1f, 0.92f, 10f, 6f, 14f)); postConcat(contrast(0.9f)) } // Film
            else -> ColorMatrix()
        }.array
        val id = ColorMatrix().array
        return FloatArray(20) { id[it] + (full[it] - id[it]) * strength.coerceIn(0f, 1f) }
    }
}

/** Gentle frame warp standing in for face reshaping (slim = narrower, big eyes = slight zoom). */
fun Modifier.cameraFx(enabled: Boolean = true): Modifier = if (!enabled) this else graphicsLayer {
    val on = !CameraFx.bypass
    val zoom = if (on) 1f + 0.06f * CameraFx.eyes else 1f
    scaleX = zoom * (if (on) 1f - 0.07f * CameraFx.slim else 1f)
    scaleY = zoom
    renderEffect = CameraFx.renderEffect()
    clip = true
}

/** Colour filter for still images (fallback photo, thumbnails) on every Android version. */
fun fxColorFilter(filterIndex: Int = CameraFx.filter, strength: Float = CameraFx.filterStrength, beauty: Boolean = true): androidx.compose.ui.graphics.ColorFilter? {
    if (CameraFx.bypass) return null
    if (filterIndex == 0 && (!beauty || !CameraFx.anyBeauty)) return null
    return androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(CameraFx.colorMatrix(filterIndex, strength, beauty).array))
}

/* ------------------------------ Masks & scenes overlay ------------------------------ */

/**
 * Draws the chosen mask on the face and the chosen scene around the edges.
 * [tracked] uses the live face box from ML Kit; otherwise the mask sits where a selfie face usually is.
 */
@Composable
fun FxOverlay(modifier: Modifier = Modifier, tracked: Boolean = false, mirrored: Boolean = true, small: Boolean = false) {
    if (CameraFx.bypass) return
    val scene = CameraFx.scene
    val mask = CameraFx.mask
    if (scene == 0 && mask == 0) return
    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        if (scene != 0) SceneLayer(scene, small)
        if (mask != 0) {
            val face = if (tracked) CameraFx.face?.let { mapFace(it, CameraFx.frame, w, h, mirrored) } else null
            val box = face ?: Rect(w * 0.27f, h * 0.2f, w * 0.73f, h * 0.2f + w * 0.56f)
            val animated = animateRect(box)
            if (!tracked || face != null) MaskLayer(mask, animated)
        }
    }
}

@Composable
private fun animateRect(target: Rect): Rect {
    val l by animateFloatAsState(target.left, tween(90), label = "l")
    val t by animateFloatAsState(target.top, tween(90), label = "t")
    val r by animateFloatAsState(target.right, tween(90), label = "r")
    val b by animateFloatAsState(target.bottom, tween(90), label = "b")
    return Rect(l, t, r, b)
}

/** Maps a normalised face box from the camera frame onto a FILL_CENTER view of size [w]×[h]. */
fun mapFace(face: Rect, frame: Size, w: Float, h: Float, mirrored: Boolean): Rect {
    val s = maxOf(w / frame.width, h / frame.height)
    val dx = (w - frame.width * s) / 2f
    val dy = (h - frame.height * s) / 2f
    var l = face.left * frame.width * s + dx
    var r = face.right * frame.width * s + dx
    if (mirrored) { val nl = w - r; r = w - l; l = nl }
    return Rect(l, face.top * frame.height * s + dy, r, face.bottom * frame.height * s + dy)
}

@Composable
private fun MaskLayer(mask: Int, f: Rect) {
    val density = LocalDensity.current
    val fw = f.width
    val t = rememberInfiniteTransition(label = "mask")
    val spin by t.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "spin")
    val bob by t.animateFloat(-1f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "bob")

    @Composable
    fun Emoji(e: String, cx: Float, cy: Float, sizePx: Float, rotation: Float = 0f) {
        val sp = with(density) { (sizePx * 0.8f).toSp() }
        Text(
            e, fontSize = sp, textAlign = TextAlign.Center, softWrap = false,
            modifier = Modifier
                .offset { IntOffset((cx - sizePx / 2f).roundToInt(), (cy - sizePx / 2f).roundToInt()) }
                .size(with(density) { sizePx.toDp() })
                .graphicsLayer { rotationZ = rotation }
                .wrapContentSize(unbounded = true)
        )
    }

    when (mask) {
        1, 2, 6 -> Canvas(Modifier.fillMaxSize()) {
            val top = f.top
            val cx = f.center.x
            when (mask) {
                1 -> { // Bunny ears + nose
                    val earW = fw * 0.2f; val earH = fw * 0.62f
                    listOf(-1f, 1f).forEach { side ->
                        val ex = cx + side * fw * 0.2f
                        rotateAround(side * 12f, Offset(ex, top)) {
                            drawOval(Color.White, Offset(ex - earW / 2, top - earH), Size(earW, earH))
                            drawOval(Color(0xFFFFA8C8), Offset(ex - earW * 0.28f, top - earH * 0.88f), Size(earW * 0.56f, earH * 0.78f))
                        }
                    }
                    drawOval(Color(0xFFFF7EB0), Offset(cx - fw * 0.07f, f.top + f.height * 0.56f), Size(fw * 0.14f, fw * 0.1f))
                }
                2 -> { // Cat ears + whiskers
                    listOf(-1f, 1f).forEach { side ->
                        val bx = cx + side * fw * 0.3f
                        val p = Path().apply {
                            moveTo(bx - fw * 0.17f, top + fw * 0.06f); lineTo(bx + side * fw * 0.05f, top - fw * 0.34f); lineTo(bx + fw * 0.17f, top + fw * 0.06f); close()
                        }
                        drawPath(p, Color(0xFF3A2A28))
                        val q = Path().apply {
                            moveTo(bx - fw * 0.09f, top + fw * 0.02f); lineTo(bx + side * fw * 0.04f, top - fw * 0.22f); lineTo(bx + fw * 0.09f, top + fw * 0.02f); close()
                        }
                        drawPath(q, Color(0xFFFFA8C8))
                        val my = f.top + f.height * 0.66f
                        repeat(3) { k ->
                            val y = my + (k - 1) * fw * 0.05f
                            drawLine(Color.White.copy(alpha = 0.9f), Offset(cx + side * fw * 0.12f, y), Offset(cx + side * fw * 0.5f, y + (k - 1) * fw * 0.05f), strokeWidth = fw * 0.012f)
                        }
                    }
                    drawCircle(Color(0xFF3A2A28), fw * 0.045f, Offset(cx, f.top + f.height * 0.6f))
                }
                6 -> { // Devil horns
                    listOf(-1f, 1f).forEach { side ->
                        val bx = cx + side * fw * 0.26f
                        val p = Path().apply {
                            moveTo(bx - fw * 0.09f, top + fw * 0.04f)
                            quadraticTo(bx + side * fw * 0.02f, top - fw * 0.12f, bx + side * fw * 0.12f, top - fw * 0.3f)
                            quadraticTo(bx + side * fw * 0.02f, top - fw * 0.05f, bx + fw * 0.09f, top + fw * 0.04f)
                            close()
                        }
                        drawPath(p, Brush.verticalGradient(listOf(Color(0xFFFF2D55), Color(0xFF9B0022)), startY = top - fw * 0.3f, endY = top + fw * 0.04f))
                    }
                }
            }
        }
        3 -> Emoji("👑", f.center.x, f.top - fw * 0.22f + bob * fw * 0.015f, fw * 0.75f)
        4 -> repeat(3) { i ->
            val a = 2 * PI * (spin + i / 3f)
            Emoji("💕", f.center.x + (cos(a) * fw * 0.62f).toFloat(), f.top + f.height * 0.15f + (sin(a) * fw * 0.18f).toFloat(), fw * 0.28f)
        }
        5 -> Emoji("🕶️", f.center.x, f.top + f.height * 0.4f, fw * 0.95f)
        7 -> listOf("🌸", "🌼", "🌺", "🌷", "🌸").forEachIndexed { i, e ->
            val a = PI + PI * (i + 0.5) / 5
            Emoji(e, f.center.x + (cos(a) * fw * 0.5f).toFloat(), f.top + fw * 0.12f + (sin(a) * fw * 0.3f).toFloat(), fw * 0.26f, rotation = (i - 2) * 15f)
        }
        8 -> Canvas(Modifier.fillMaxSize()) {
            listOf(-1f, 1f).forEach { side ->
                val c = Offset(f.center.x + side * fw * 0.28f, f.top + f.height * 0.62f)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFF6FA5).copy(alpha = 0.55f), Color.Transparent), c, fw * 0.16f), fw * 0.16f, c)
            }
        }
    }
}

private inline fun androidx.compose.ui.graphics.drawscope.DrawScope.rotateAround(deg: Float, pivot: Offset, crossinline block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    rotate(deg, pivot) { block() }
}

@Composable
private fun SceneLayer(scene: Int, small: Boolean) {
    val t = rememberInfiniteTransition(label = "scene")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "p")
    val pulse by t.animateFloat(0.55f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "pulse")
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val center = Offset(w / 2f, h * 0.38f)
        val radius = maxOf(w, h) * 0.75f
        when (scene) {
            1 -> drawRect(Brush.radialGradient(0.35f to Color.Transparent, 1f to Color(0xFFE8E4F0).copy(alpha = 0.78f), center = center, radius = radius))
            2 -> {
                drawRect(Brush.radialGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.75f), center = center, radius = radius))
                drawRect(Brush.verticalGradient(0f to Color(0xFFFFF4D6).copy(alpha = 0.35f), 0.3f to Color.Transparent))
            }
            3 -> {
                drawRect(Brush.verticalGradient(0f to Color(0xFFFF9A5A).copy(alpha = 0.55f), 0.25f to Color.Transparent, 0.75f to Color.Transparent, 1f to Color(0xFF29B6F6).copy(alpha = 0.6f)))
                drawCircle(Color(0xFFFFD54F).copy(alpha = 0.8f), w * 0.09f, Offset(w * 0.82f, h * 0.1f))
            }
            4 -> {
                val stroke = (if (small) 4f else 10f) * pulse
                drawRect(Brush.sweepGradient(listOf(Color(0xFFFF2DAA), Color(0xFF2DE2FF), Color(0xFFB45CFF), Color(0xFFFF2DAA)), center), style = Stroke(stroke))
                drawRect(Brush.radialGradient(0.5f to Color.Transparent, 1f to Color(0xFF6A00FF).copy(alpha = 0.35f), center = center, radius = radius))
            }
            5 -> repeat(10) { i ->
                val x = w * ((i * 0.618f + 0.07f) % 1f)
                val y = h * (1f - ((p + i * 0.1f) % 1f))
                val r = w * (0.025f + (i % 3) * 0.01f)
                heart(Offset(x, y), r, Color(0xFFFF4F8B).copy(alpha = 0.75f))
            }
            6 -> {
                drawRect(Brush.radialGradient(0.4f to Color.Transparent, 1f to Color(0xFF1A0B4A).copy(alpha = 0.85f), center = center, radius = radius))
                repeat(36) { i ->
                    val x = w * ((i * 0.618f) % 1f); val y = h * ((i * 0.377f) % 1f)
                    val d = Offset(x, y) - center
                    if (d.getDistance() > w * 0.38f) drawCircle(Color.White.copy(alpha = (0.4f + 0.6f * ((pulse + i * 0.13f) % 1f))), w * 0.006f, Offset(x, y))
                }
            }
            7 -> repeat(40) { i ->
                val sway = sin((p * 6.28f + i).toDouble()).toFloat() * 0.02f
                val x = w * ((i * 0.618f + sway + 1f) % 1f)
                val y = h * ((p + i * 0.025f) % 1f)
                drawCircle(Color.White.copy(alpha = 0.85f), w * (0.006f + (i % 3) * 0.003f), Offset(x, y))
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.heart(c: Offset, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(c.x, c.y + r)
        cubicTo(c.x - r * 2f, c.y - r * 0.4f, c.x - r * 0.9f, c.y - r * 1.6f, c.x, c.y - r * 0.5f)
        cubicTo(c.x + r * 0.9f, c.y - r * 1.6f, c.x + r * 2f, c.y - r * 0.4f, c.x, c.y + r)
        close()
    }
    drawPath(p, color)
}

/* ------------------------------ Beauty / filter / mask / scene panel ------------------------------ */

/** Bottom sheet with live preview behind it. Changes apply instantly and are saved. */
@Composable
fun BeautyPanel(initialTab: Int = 0, onDismiss: () -> Unit) {
    var tab by remember { mutableIntStateOf(initialTab) }
    DisposableEffect(Unit) { onDispose { CameraFx.bypass = false; CameraFx.save() } }
    IshqSheet(onDismiss, dark = true) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            UnderlineTabs(listOf("Beauty", "Filters", "Masks", "Scenes"), tab, { tab = it }, Modifier.weight(1f), dark = true)
            // Hold to see the camera without effects.
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(if (CameraFx.bypass) BrandPink else Color.White.copy(alpha = 0.12f))
                    .pointerInput(Unit) { detectTapGestures(onPress = { CameraFx.bypass = true; tryAwaitRelease(); CameraFx.bypass = false }) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.Compare, "Hold to compare", tint = Color.White, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)).clickable {
                    when (tab) { 0 -> CameraFx.apply(beautyPresets[0]); 1 -> CameraFx.filter = 0; 2 -> CameraFx.mask = 0; else -> CameraFx.scene = 0 }
                    CameraFx.save()
                },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.RestartAlt, "Reset", tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
        Spacer(Modifier.height(10.dp))
        when (tab) {
            0 -> Column(Modifier.padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    beautyPresets.forEach { p ->
                        val on = CameraFx.smooth == p.smooth && CameraFx.brighten == p.brighten && CameraFx.rosy == p.rosy && CameraFx.slim == p.slim && CameraFx.eyes == p.eyes
                        Text(
                            p.name, style = MaterialTheme.typography.labelLarge, color = Color.White,
                            modifier = Modifier.clip(CircleShape).background(if (on) BrandPink else Color.White.copy(alpha = 0.12f)).clickable { CameraFx.apply(p) }.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                FxSlider("Smooth", CameraFx.smooth) { CameraFx.smooth = it }
                FxSlider("Brighten", CameraFx.brighten) { CameraFx.brighten = it }
                FxSlider("Rosy", CameraFx.rosy) { CameraFx.rosy = it }
                FxSlider("Slim face", CameraFx.slim) { CameraFx.slim = it }
                FxSlider("Big eyes", CameraFx.eyes) { CameraFx.eyes = it }
            }
            1 -> Column {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(cameraFilters) { i, it ->
                        FxTile(it, CameraFx.filter == i, { CameraFx.filter = i; CameraFx.save() }) {
                            AsyncImage(photo(77, 160, 160), null, contentScale = ContentScale.Crop, colorFilter = fxColorFilter(i, 1f, beauty = false), modifier = Modifier.fillMaxSize())
                        }
                    }
                }
                if (CameraFx.filter != 0) Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) { FxSlider("Strength", CameraFx.filterStrength) { CameraFx.filterStrength = it } }
            }
            2 -> FxRow(cameraMasks, CameraFx.mask) { CameraFx.mask = it; CameraFx.save() }
            else -> FxRow(cameraScenes, CameraFx.scene) { CameraFx.scene = it; CameraFx.save() }
        }
        if (tab == 2) Text(
            if (CameraFx.face != null) "Face found · the mask follows you" else "Look at the camera — the mask sticks to your face",
            style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun FxSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.width(84.dp))
        Slider(
            value, onChange, Modifier.weight(1f), onValueChangeFinished = { CameraFx.save() },
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = BrandPink, inactiveTrackColor = Color.White.copy(alpha = 0.2f))
        )
        Text("${(value * 100).roundToInt()}", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun FxRow(items: List<FxItem>, selected: Int, onPick: (Int) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(items) { i, it ->
            FxTile(it, selected == i, { onPick(i) }) {
                Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) { Text(it.emoji, fontSize = 28.sp) }
            }
        }
    }
}

@Composable
private fun FxTile(item: FxItem, selected: Boolean, onClick: () -> Unit, thumb: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp).clickable(onClick = onClick)) {
        Box(
            Modifier.size(62.dp).clip(RoundedCornerShape(16.dp))
                .then(if (selected) Modifier.border(2.dp, BrandPink, RoundedCornerShape(16.dp)) else Modifier)
        ) { thumb() }
        Spacer(Modifier.height(4.dp))
        Text(item.name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal), color = if (selected) BrandPink else Color.White, maxLines = 1)
    }
}

/** Quick mask strip for calls: tap a mask without opening the full panel. */
@Composable
fun QuickMaskRow(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(0, 1, 3, 5, 4).forEach { i ->
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f))
                    .then(if (CameraFx.mask == i) Modifier.border(2.dp, BrandPink, CircleShape) else Modifier)
                    .clickable { CameraFx.mask = i; CameraFx.save() },
                contentAlignment = Alignment.Center
            ) { Text(cameraMasks[i].emoji, fontSize = 20.sp) }
        }
    }
}
