package com.live.ishqexpress.ui.common

import android.graphics.Bitmap
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage

/** Front/back choice shared by Go Live setup, the live itself and calls. */
object CameraState { var front by androidx.compose.runtime.mutableStateOf(true) }

/**
 * Handle to grab the current preview frame, e.g. for a KYC selfie or a live cover.
 * In production the streaming SDK (Agora/ZEGO/LiveKit…) owns the camera instead; this preview is
 * what the user sees before and while the SDK publishes.
 */
class CameraHandle { internal var view: PreviewView? = null; fun snapshot(): Bitmap? = view?.bitmap }

/** Shared ML Kit face detector (fast mode, biggest face only) used to pin masks to your face. */
private val faceDetector by lazy {
    FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setMinFaceSize(0.2f)
            .build()
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun faceAnalysis(context: android.content.Context): ImageAnalysis {
    val analysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setResolutionStrategy(ResolutionStrategy(android.util.Size(480, 640), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER))
                .build()
        )
        .build()
    analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { proxy ->
        val media = proxy.image
        if (media == null || !CameraFx.wantsFace) { proxy.close(); return@setAnalyzer }
        val rot = proxy.imageInfo.rotationDegrees
        val fw = (if (rot % 180 == 0) proxy.width else proxy.height).toFloat()
        val fh = (if (rot % 180 == 0) proxy.height else proxy.width).toFloat()
        faceDetector.process(InputImage.fromMediaImage(media, rot))
            .addOnSuccessListener { faces ->
                val f = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }?.boundingBox
                CameraFx.frame = Size(fw, fh)
                CameraFx.face = f?.let { Rect(it.left / fw, it.top / fh, it.right / fw, it.bottom / fh) }
            }
            .addOnCompleteListener { proxy.close() }
    }
    return analysis
}

/** Still-image colour filter for Android < 12 (on 12+ the [cameraFx] render effect already does it). */
private fun stillFilter(fx: Boolean) = if (!fx || Build.VERSION.SDK_INT >= 31) null else fxColorFilter()

/**
 * Live camera preview with the beauty/filter/mask/scene effects from [CameraFx] on top.
 * Falls back to [fallback] when the camera permission isn't granted, in previews/tests, or if the
 * device has no matching camera. Pass [fx] = false for KYC captures, which must be unedited.
 */
@Composable
fun CameraPreview(front: Boolean, modifier: Modifier = Modifier, handle: CameraHandle? = null, fx: Boolean = true, fallback: @Composable () -> Unit) {
    val context = LocalContext.current
    val inspection = LocalInspectionMode.current
    val granted = remember(Perms.version) { !inspection && Perms.has(context, Perms.camera) }
    if (!granted) {
        Box(modifier) {
            Box(Modifier.fillMaxSize().cameraFx(fx)) { fallback() }
            if (fx) FxOverlay(tracked = false)
        }
        return
    }

    val owner = LocalLifecycleOwner.current
    val view = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE // TextureView: clips to rounded tiles
        }
    }
    LaunchedEffect(handle) { handle?.view = view }
    DisposableEffect(front, owner, fx) {
        val future = ProcessCameraProvider.getInstance(context)
        val preview = Preview.Builder().build()
        val analysis = if (fx) faceAnalysis(context) else null
        var provider: ProcessCameraProvider? = null
        future.addListener({
            runCatching {
                val p = future.get()
                provider = p
                preview.setSurfaceProvider(view.surfaceProvider)
                val wanted = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                val selector = if (p.hasCamera(wanted)) wanted else CameraSelector.DEFAULT_BACK_CAMERA
                p.unbind(preview)
                runCatching { if (analysis != null) p.bindToLifecycle(owner, selector, preview, analysis) else p.bindToLifecycle(owner, selector, preview) }
                    .onFailure { p.bindToLifecycle(owner, selector, preview) } // device can't run both: skip face tracking
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            runCatching { provider?.unbind(preview); analysis?.let { provider?.unbind(it); it.clearAnalyzer() } }
            CameraFx.face = null
        }
    }
    val filter = stillFilter(fx)
    Box(modifier) {
        AndroidView(
            { view }, Modifier.fillMaxSize().cameraFx(fx),
            update = { v ->
                // Android < 12: colour effects through a hardware layer paint.
                if (Build.VERSION.SDK_INT < 31) {
                    if (filter != null) v.setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply { colorFilter = ColorMatrixColorFilter(CameraFx.colorMatrix()) })
                    else v.setLayerType(View.LAYER_TYPE_NONE, null)
                }
            }
        )
        if (fx) FxOverlay(tracked = true, mirrored = front)
    }
}

/** Full-screen self camera with the same top/bottom scrim as [VideoBackdrop]. */
@Composable
fun CameraBackdrop(front: Boolean, tint: List<Color>, fallbackUrl: String, modifier: Modifier = Modifier, scrim: Boolean = true, handle: CameraHandle? = null, fx: Boolean = true) {
    Box(modifier.fillMaxSize().background(Brush.linearGradient(tint))) {
        CameraPreview(front, Modifier.fillMaxSize(), handle, fx) {
            AsyncImage(fallbackUrl, null, contentScale = ContentScale.Crop, colorFilter = stillFilter(fx), modifier = Modifier.fillMaxSize())
        }
        if (scrim) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.45f), 0.2f to Color.Transparent, 0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.7f))
                )
            )
        }
    }
}

/** Your own small video tile (PiP, guest seat, split screen). */
@Composable
fun SelfCameraTile(fallbackUrl: String, modifier: Modifier = Modifier, front: Boolean = true) {
    CameraPreview(front, modifier.fillMaxSize()) {
        AsyncImage(fallbackUrl, null, contentScale = ContentScale.Crop, colorFilter = stillFilter(true), modifier = Modifier.fillMaxSize())
    }
}

/** Asks for [permissions] once when the screen opens (calls, KYC, hosting). */
@Composable
fun RequestOnEnter(permissions: List<String>, onDenied: () -> Unit = {}) {
    if (LocalInspectionMode.current) return
    val gate = rememberPermissionGate()
    LaunchedEffect(Unit) { gate.request(permissions) { ok -> if (!ok) onDenied() } }
}
