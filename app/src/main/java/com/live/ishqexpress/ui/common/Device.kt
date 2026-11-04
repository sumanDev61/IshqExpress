package com.live.ishqexpress.ui.common

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.live.ishqexpress.data.Session
import java.io.File

/* Phone features the UI needs: runtime permissions, photo picker, share sheet, clipboard, links, files. */

object Perms {
    val camera = Manifest.permission.CAMERA
    val mic = Manifest.permission.RECORD_AUDIO
    val notifications: String? = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null

    val video = listOf(camera, mic)
    val audio = listOf(mic)

    /** Bumped after every permission result so camera previews re-check and start. */
    var version by mutableIntStateOf(0)

    fun has(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun hasAll(context: Context, permissions: List<String>) = permissions.all { has(context, it) }
}

/** Ask for permissions only when needed; [onResult] gets true when all of them are granted. */
class PermissionGate internal constructor(
    private val context: Context,
    private val launch: (Array<String>) -> Unit,
    private val setCallback: ((Boolean) -> Unit) -> Unit,
) {
    fun request(permissions: List<String>, onResult: (Boolean) -> Unit) {
        val missing = permissions.filterNot { Perms.has(context, it) }
        if (missing.isEmpty()) { onResult(true); return }
        setCallback(onResult)
        launch(missing.toTypedArray())
    }
}

@Composable
fun rememberPermissionGate(): PermissionGate {
    val context = LocalContext.current
    val pending = remember { arrayOfNulls<(Boolean) -> Unit>(1) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        Perms.version++
        pending[0]?.invoke(result.values.all { it })
        pending[0] = null
    }
    return remember(context, launcher) { PermissionGate(context, { launcher.launch(it) }, { pending[0] = it }) }
}

/** Opens this app's system settings page, for when a permission was denied permanently. */
fun openAppSettings(context: Context) {
    runCatching {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/** Single image picker. The picked image is copied into app storage; you get a file:// uri. */
@Composable
fun rememberImagePicker(onPicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) callback(Session.importImage(context, uri))
    }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}

/**
 * Takes a photo with the system camera app and returns it as a file:// uri in app storage.
 * Asks for the camera permission first (required because the app declares it).
 */
@Composable
fun rememberCameraCapture(onTaken: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onTaken)
    val gate = rememberPermissionGate()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) runCatching {
            val dir = File(context.filesDir, "images").apply { mkdirs() }
            val out = File(dir, "cam_${System.currentTimeMillis()}.jpg")
            out.outputStream().use { bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
            callback(Uri.fromFile(out).toString())
        }
    }
    return { gate.request(listOf(Perms.camera)) { ok -> if (ok) runCatching { launcher.launch(null) } } }
}

/** Multiple photos and videos, up to [max]. */
@Composable
fun rememberMediaPicker(max: Int, onPicked: (List<String>) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(max.coerceAtLeast(2))) { uris ->
        if (uris.isNotEmpty()) callback(uris.take(max).map { Session.importImage(context, it) })
    }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) }
}

/** Public links. The domain is a placeholder until the real one (with App Links) is set up. */
object AppLinks {
    private const val base = "https://ishqexpress.app"
    fun profile(id: Int) = "$base/u/$id"
    fun live(id: Int) = "$base/live/$id"
    fun room(name: String) = "$base/room/${name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifEmpty { "me" }}"
    fun replay(id: Int) = "$base/replay/$id"
    fun post(id: Int) = "$base/p/$id"
    fun invite(code: String) = "$base/invite/$code"
    const val playStore = "https://play.google.com/store/apps/details?id=com.live.ishqexpress"
}

/** Android share sheet with plain text (usually a message plus a link). */
fun shareText(context: Context, text: String, title: String = "Share via") {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    runCatching { context.startActivity(Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

/** Share straight to one app (WhatsApp, Instagram…); falls back to the share sheet if it isn't installed. */
fun shareTo(context: Context, packageName: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text).setPackage(packageName)
    try { context.startActivity(send.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: ActivityNotFoundException) { shareText(context, text) }
}

fun copyText(context: Context, text: String, label: String = "ishqexpress") {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun openUrl(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

fun openPlayStore(context: Context) {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try { context.startActivity(market) } catch (_: ActivityNotFoundException) { openUrl(context, AppLinks.playStore) }
}

fun emailSupport(context: Context, subject: String) {
    val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@ishqexpress.app")).putExtra(Intent.EXTRA_SUBJECT, subject)
    try { context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: ActivityNotFoundException) { shareText(context, subject) }
}

/**
 * Writes a simple one-page PDF (title + rows of label/value) to Downloads.
 * Returns true on success.
 */
fun savePdfToDownloads(context: Context, fileName: String, title: String, subtitle: String, rows: List<Pair<String, String>>): Boolean = runCatching {
    val doc = PdfDocument()
    val pageW = 595; val pageH = 842 // A4 at 72 dpi
    val rowH = 22f
    val perPage = ((pageH - 160) / rowH).toInt()
    val titlePaint = Paint().apply { textSize = 20f; typeface = Typeface.DEFAULT_BOLD; color = 0xFF1B1030.toInt() }
    val subPaint = Paint().apply { textSize = 11f; color = 0xFF6B6480.toInt() }
    val textPaint = Paint().apply { textSize = 11f; color = 0xFF1B1030.toInt() }
    val linePaint = Paint().apply { color = 0xFFE8E2F0.toInt(); strokeWidth = 0.8f }
    rows.chunked(perPage.coerceAtLeast(1)).ifEmpty { listOf(emptyList()) }.forEachIndexed { pi, chunk ->
        val page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pi + 1).create())
        val c = page.canvas
        c.drawText(title, 40f, 60f, titlePaint)
        c.drawText(subtitle, 40f, 80f, subPaint)
        var y = 120f
        chunk.forEach { (l, v) ->
            c.drawText(l, 40f, y, textPaint)
            c.drawText(v, pageW - 40f - textPaint.measureText(v), y, textPaint)
            c.drawLine(40f, y + 7f, pageW - 40f, y + 7f, linePaint)
            y += rowH
        }
        c.drawText("Generated by ishqexpress · page ${pi + 1}", 40f, pageH - 30f, subPaint)
        doc.finishPage(page)
    }
    val name = if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf"
    if (Build.VERSION.SDK_INT >= 29) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
        resolver.openOutputStream(uri)!!.use { doc.writeTo(it) }
        values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    } else {
        @Suppress("DEPRECATION")
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).apply { mkdirs() }
        File(dir, name).outputStream().use { doc.writeTo(it) }
    }
    doc.close()
    true
}.getOrDefault(false)
