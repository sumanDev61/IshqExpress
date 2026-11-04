package com.live.ishqexpress

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.live.ishqexpress.ui.common.CameraFx
import com.live.ishqexpress.ui.common.VoiceDsp
import com.live.ishqexpress.ui.common.mapFace
import com.live.ishqexpress.ui.common.voiceEffects
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@org.junit.runner.RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
@org.robolectric.annotation.Config(sdk = [35])
class FxTest {
    private val sr = 22_050
    private fun tone(hz: Double, sec: Double = 1.0) = FloatArray((sr * sec).toInt()) { (0.5 * sin(2 * PI * hz * it / sr)).toFloat() }

    /** Zero crossings per second ≈ 2 × frequency. */
    private fun freq(x: FloatArray, from: Int = sr / 10): Double {
        var z = 0
        for (i in from + 1 until x.size) if ((x[i - 1] < 0f) != (x[i] < 0f)) z++
        return z / 2.0 / ((x.size - from).toDouble() / sr)
    }

    @Test fun everyVoiceEffectGivesCleanAudio() {
        val x = tone(220.0)
        voiceEffects.indices.forEach { e ->
            val y = VoiceDsp.process(x, e, sr)
            assertTrue("effect $e empty", y.isNotEmpty())
            assertTrue("effect $e has NaN/clipping", y.all { !it.isNaN() && abs(it) <= 0.91f })
        }
    }

    @Test fun girlVoiceRaisesPitchAndKeepsLength() {
        val x = tone(200.0)
        val y = VoiceDsp.pitchShift(x, 1.35f, sr)
        assertEquals(x.size, y.size)
        val f = freq(y)
        assertTrue("expected ~270 Hz, got $f", f in 240.0..300.0)
    }

    @Test fun monsterVoiceLowersPitch() {
        val f = freq(VoiceDsp.pitchShift(tone(300.0), 0.6f, sr))
        assertTrue("expected ~180 Hz, got $f", f in 150.0..215.0)
    }

    @Test fun echoAddsTail() {
        val x = tone(300.0, 0.3)
        assertTrue(VoiceDsp.echo(x, sr, 0.25f, 0.4f).size > x.size)
    }

    @Test fun filterAtZeroStrengthIsIdentity() {
        val id = android.graphics.ColorMatrix().array
        (0..8).forEach { assertArrayEquals(id, CameraFx.filterMatrix(it, 0f), 1e-5f) }
    }

    @Test fun faceMapsOntoMirroredFillCenterPreview() {
        // 480×640 frame shown in a 1080×1920 view: scale 3 (height), 0 horizontal crop... width 1440 → 180 px cropped each side.
        val r = mapFace(Rect(0.25f, 0.25f, 0.5f, 0.5f), Size(480f, 640f), 1080f, 1920f, mirrored = false)
        assertEquals(360f - 180f, r.left, 0.5f)
        assertEquals(480f, r.top, 0.5f)
        val m = mapFace(Rect(0.25f, 0.25f, 0.5f, 0.5f), Size(480f, 640f), 1080f, 1920f, mirrored = true)
        assertEquals(1080f - r.right, m.left, 0.5f)
        assertEquals(r.width, m.width, 0.5f)
    }
}
