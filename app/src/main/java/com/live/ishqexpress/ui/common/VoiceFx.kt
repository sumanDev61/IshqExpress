package com.live.ishqexpress.ui.common

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.live.ishqexpress.data.Session
import com.live.ishqexpress.ui.nav.LocalToast
import com.live.ishqexpress.ui.theme.BrandPink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/*
 * Voice changer. The chosen voice is saved and shown in every room; the "hold to test" button
 * records your mic and plays it back through the effect, all on the phone.
 * When the streaming SDK is added, [VoiceDsp.process] (or the SDK's own voice changer with the
 * same preset names) runs on the outgoing mic audio instead.
 */

data class VoiceEffect(val name: String, val emoji: String, val subtitle: String)

val voiceEffects = listOf(
    VoiceEffect("Original", "🎙️", "Your real voice"),
    VoiceEffect("Girl", "👧", "Higher, softer"),
    VoiceEffect("Boy", "👦", "Deeper"),
    VoiceEffect("Child", "🧒", "Cute and high"),
    VoiceEffect("Monster", "👹", "Very deep"),
    VoiceEffect("Robot", "🤖", "Metallic"),
    VoiceEffect("Echo", "🏔️", "Mountain echo"),
    VoiceEffect("Hall", "🏛️", "Big concert hall"),
    VoiceEffect("Radio", "📻", "Old radio"),
    VoiceEffect("Chipmunk", "🐿️", "Fast and squeaky"),
)

object VoiceFx {
    var effect by mutableIntStateOf(Session.int("voice.effect", 0))
        private set
    val current get() = voiceEffects[effect]
    val active get() = effect != 0
    fun select(i: Int) { effect = i; Session.setInt("voice.effect", i) }
}

/** Pure audio processing on mono float samples in -1..1, testable without a device. */
object VoiceDsp {
    fun process(x: FloatArray, effect: Int, sr: Int): FloatArray {
        val y = when (effect) {
            1 -> pitchShift(x, 1.35f, sr)
            2 -> pitchShift(x, 0.82f, sr)
            3 -> pitchShift(x, 1.7f, sr)
            4 -> pitchShift(x, 0.6f, sr)
            5 -> robot(x, sr)
            6 -> echo(x, sr, 0.28f, 0.45f)
            7 -> hall(x, sr)
            8 -> radio(x, sr)
            9 -> resample(x, 1.8f)
            else -> x.copyOf()
        }
        return normalize(y)
    }

    /** Pitch shift keeping the length: two crossfaded read heads sweeping a short delay line. */
    fun pitchShift(x: FloatArray, ratio: Float, sr: Int): FloatArray {
        val w = (sr * 0.05f).coerceAtLeast(16f)
        val out = FloatArray(x.size)
        var d = 0f
        val step = 1f - ratio
        for (n in x.indices) {
            val d2 = (d + w / 2f) % w
            val g1 = 1f - abs(2f * d / w - 1f)
            val g2 = 1f - abs(2f * d2 / w - 1f)
            out[n] = at(x, n - d) * g1 + at(x, n - d2) * g2
            d += step
            if (d >= w) d -= w
            if (d < 0f) d += w
        }
        return out
    }

    fun resample(x: FloatArray, speed: Float): FloatArray = FloatArray((x.size / speed).toInt()) { at(x, it * speed) }

    fun echo(x: FloatArray, sr: Int, delaySec: Float, feedback: Float): FloatArray {
        val d = (sr * delaySec).toInt().coerceAtLeast(1)
        val y = FloatArray(x.size + d * 3)
        for (n in y.indices) y[n] = (if (n < x.size) x[n] else 0f) + if (n >= d) feedback * y[n - d] else 0f
        return y
    }

    fun hall(x: FloatArray, sr: Int): FloatArray {
        val delays = floatArrayOf(0.0297f, 0.0371f, 0.0411f, 0.0437f).map { (it * sr).toInt() }
        val tail = sr / 2
        val y = FloatArray(x.size + tail)
        delays.forEach { d ->
            val c = FloatArray(y.size)
            for (n in c.indices) { c[n] = (if (n < x.size) x[n] else 0f) + if (n >= d) 0.78f * c[n - d] else 0f }
            for (n in y.indices) y[n] += c[n] * 0.18f
        }
        for (n in x.indices) y[n] += x[n] * 0.7f
        return y
    }

    fun robot(x: FloatArray, sr: Int): FloatArray {
        val f = 2 * PI * 70.0 / sr
        val e = echo(x, sr, 0.012f, 0.5f)
        return FloatArray(e.size) { (e[it] * (0.6 + 0.4 * sin(f * it))).toFloat() }
    }

    fun radio(x: FloatArray, sr: Int): FloatArray {
        val hp = 1f - (2 * PI * 450 / sr).toFloat()
        val lp = (2 * PI * 3000 / sr).toFloat().coerceAtMost(1f)
        var prevX = 0f; var hpY = 0f; var lpY = 0f
        return FloatArray(x.size) { n ->
            hpY = hp * (hpY + x[n] - prevX); prevX = x[n]
            lpY += lp * (hpY - lpY)
            tanh(lpY * 3f) * 0.6f + ((n * 7919 % 97) / 97f - 0.5f) * 0.01f
        }
    }

    private fun at(x: FloatArray, pos: Float): Float {
        if (pos < 0f) return 0f
        val i = floor(pos).toInt()
        if (i + 1 >= x.size) return if (i < x.size) x[i] else 0f
        val t = pos - i
        return x[i] * (1 - t) + x[i + 1] * t
    }

    fun normalize(y: FloatArray): FloatArray {
        var peak = 0f
        for (v in y) peak = maxOf(peak, abs(v))
        if (peak < 1e-4f) return y
        val k = 0.9f / peak
        return FloatArray(y.size) { y[it] * k }
    }

    fun level(chunk: ShortArray, n: Int): Float {
        if (n <= 0) return 0f
        var s = 0.0
        for (i in 0 until n) s += chunk[i].toDouble() * chunk[i]
        return (sqrt(s / n) / 6000.0).toFloat().coerceIn(0f, 1f)
    }
}

/** Records up to [maxSec] seconds and plays it back with an effect. */
private class VoiceTester {
    val sr = 22_050
    var recording by mutableStateOf(false)
    var playing by mutableStateOf(false)
    var level by mutableFloatStateOf(0f)
    var clip: FloatArray? by mutableStateOf(null)
    private var track: AudioTrack? = null

    @SuppressLint("MissingPermission") // the caller asks for RECORD_AUDIO first
    suspend fun record(maxSec: Int = 6) = withContext(Dispatchers.IO) {
        val min = AudioRecord.getMinBufferSize(sr, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(2048)
        val rec = runCatching { AudioRecord(MediaRecorder.AudioSource.MIC, sr, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min * 2) }.getOrNull()
        if (rec == null || rec.state != AudioRecord.STATE_INITIALIZED) { rec?.release(); return@withContext }
        val all = ShortArray(sr * maxSec)
        val buf = ShortArray(min / 2)
        var total = 0
        rec.startRecording()
        try {
            while (isActive && recording && total < all.size) {
                val n = rec.read(buf, 0, minOf(buf.size, all.size - total))
                if (n > 0) { System.arraycopy(buf, 0, all, total, n); total += n; level = VoiceDsp.level(buf, n) }
            }
        } finally {
            runCatching { rec.stop() }; rec.release()
            level = 0f; recording = false
        }
        if (total > sr / 4) clip = FloatArray(total) { all[it] / 32768f }
    }

    suspend fun play(effect: Int) {
        val x = clip ?: return
        stop()
        playing = true
        val y = withContext(Dispatchers.Default) { VoiceDsp.process(x, effect, sr) }
        val pcm = ShortArray(y.size) { (y[it].coerceIn(-1f, 1f) * 32767).toInt().toShort() }
        withContext(Dispatchers.IO) {
            val t = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    .setAudioFormat(AudioFormat.Builder().setSampleRate(sr).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            }.getOrNull() ?: run { playing = false; return@withContext }
            track = t
            t.write(pcm, 0, pcm.size)
            t.play()
            val ms = pcm.size * 1000L / sr
            kotlinx.coroutines.delay(ms + 150)
            if (track === t) stop()
        }
    }

    fun stop() {
        track?.let { runCatching { it.stop() }; it.release() }
        track = null
        playing = false
    }
}

/** Voice changer sheet: pick a voice, hold the mic to record a test, hear it back. */
@Composable
fun VoiceChangerSheet(onDismiss: () -> Unit) {
    val toast = LocalToast.current
    val gate = rememberPermissionGate()
    val scope = rememberCoroutineScope()
    val tester = remember { VoiceTester() }
    var recJob by remember { mutableStateOf<Job?>(null) }
    DisposableEffect(Unit) { onDispose { tester.recording = false; tester.stop() } }

    fun pick(i: Int) {
        VoiceFx.select(i)
        if (tester.clip != null) scope.launch { tester.play(i) }
    }

    IshqSheet(onDismiss, title = "Voice changer", dark = true) {
        Text(
            if (VoiceFx.active) "Everyone in the room hears your ${VoiceFx.current.name.lowercase()} voice" else "Pick a voice — the room hears it while you talk",
            style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        voiceEffects.chunked(5).forEachIndexed { r, row ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEachIndexed { c, v ->
                    val i = r * 5 + c
                    val on = VoiceFx.effect == i
                    Column(
                        Modifier.width(64.dp).clip(RoundedCornerShape(14.dp)).clickable { pick(i) }.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.size(52.dp).clip(CircleShape)
                                .background(if (on) Brush.linearGradient(listOf(Color(0xFFFF4FB8), Color(0xFF8A5CFF))) else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.06f))))
                                .then(if (on) Modifier.border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) { Text(v.emoji, fontSize = 24.sp) }
                        Spacer(Modifier.height(4.dp))
                        Text(v.name, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.Normal), color = if (on) BrandPink else Color.White, maxLines = 1)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // Test area
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.07f)).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LevelBars(if (tester.recording) tester.level else if (tester.playing) 0.55f else 0f, tester.recording || tester.playing)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape)
                        .background(if (tester.recording) Brush.linearGradient(listOf(Color(0xFFFF3B5C), Color(0xFFFF7A59))) else Brush.linearGradient(listOf(Color(0xFFFF4FB8), Color(0xFF8A5CFF))))
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                var ok = false
                                gate.request(Perms.audio) { g -> ok = g; if (!g) toast("Allow the microphone to test your voice") }
                                if (!ok) return@detectTapGestures
                                tester.stop()
                                tester.recording = true
                                recJob = scope.launch { tester.record() }
                                tryAwaitRelease()
                                tester.recording = false
                                recJob?.join()
                                if (tester.clip == null) toast("Hold a little longer and say something")
                                else scope.launch { tester.play(VoiceFx.effect) }
                            })
                        },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Mic, "Hold to record", tint = Color.White, modifier = Modifier.size(30.dp)) }
                if (tester.clip != null) {
                    Spacer(Modifier.width(16.dp))
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.14f)).clickable {
                            if (tester.playing) tester.stop() else scope.launch { tester.play(VoiceFx.effect) }
                        },
                        contentAlignment = Alignment.Center
                    ) { Icon(if (tester.playing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow, if (tester.playing) "Stop" else "Play again", tint = Color.White) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    tester.recording -> "Recording… release to hear it"
                    tester.playing -> "Playing as ${VoiceFx.current.name} ${VoiceFx.current.emoji}"
                    tester.clip != null -> "Tap another voice to hear the same clip"
                    else -> "Hold the mic, say something, then let go"
                },
                style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f), textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LevelBars(level: Float, live: Boolean) {
    val t = rememberInfiniteTransition(label = "bars")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(700, easing = LinearEasing)), label = "p")
    Canvas(Modifier.fillMaxWidth().height(36.dp)) {
        val n = 28
        val gap = size.width / n
        repeat(n) { i ->
            val wave = if (live) (0.35f + 0.65f * abs(sin((i * 0.55f + p * 6.28f).toDouble())).toFloat()) else 0.12f
            val hgt = size.height * (0.12f + 0.88f * wave * (if (live) level.coerceAtLeast(0.15f) else 1f))
            val x = gap * i + gap / 2
            drawLine(
                Brush.verticalGradient(listOf(Color(0xFFFF4FB8), Color(0xFF39C5FF))),
                Offset(x, (size.height - hgt) / 2), Offset(x, (size.height + hgt) / 2),
                strokeWidth = gap * 0.5f, cap = StrokeCap.Round
            )
        }
    }
}

/** Small chip for room headers showing the active voice. */
@Composable
fun VoiceChip(modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (!VoiceFx.active) return
    GlassPill(modifier.clickable(onClick = onClick)) {
        Text("${VoiceFx.current.emoji} ${VoiceFx.current.name} voice", style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}
