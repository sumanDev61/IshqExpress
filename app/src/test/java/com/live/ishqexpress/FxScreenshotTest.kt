package com.live.ishqexpress

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.SuccessResult
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.live.ishqexpress.ui.common.BeautyPanel
import com.live.ishqexpress.ui.common.CameraBackdrop
import com.live.ishqexpress.ui.common.CameraFx
import com.live.ishqexpress.ui.common.VoiceChangerSheet
import com.live.ishqexpress.ui.common.brandPair
import com.live.ishqexpress.ui.nav.Dest
import com.live.ishqexpress.ui.nav.LocalNav
import com.live.ishqexpress.ui.nav.LocalToast
import com.live.ishqexpress.ui.nav.Navigator
import com.live.ishqexpress.ui.nav.Screen
import com.live.ishqexpress.ui.theme.IshqExpressTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Camera effects and voice changer rendered on the JVM (no camera permission → sample photo fallback). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h860dp-xxhdpi")
class FxScreenshotTest {
    @get:Rule val compose = createComposeRule()
    private val out = "build/outputs/roborazzi"

    @Before fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        Coil.setImageLoader(ImageLoader.Builder(ctx).components {
            add(coil.intercept.Interceptor { chain -> SuccessResult(ColorDrawable(0xFFC99A86.toInt()), chain.request, DataSource.MEMORY) })
        }.build())
        CameraFx.reset()
    }

    @After fun tearDown() = CameraFx.reset()

    private fun show(content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            IshqExpressTheme {
                CompositionLocalProvider(LocalNav provides Navigator(listOf(Dest(Screen.Main))), LocalToast provides {}) { content() }
            }
        }
        compose.mainClock.advanceTimeBy(1_200)
    }

    private fun camera(name: String, setup: () -> Unit) {
        setup()
        show { CameraBackdrop(true, brandPair, "me", scrim = false) }
        compose.onRoot().captureRoboImage("$out/fx_$name.png")
    }

    @Test fun bunnyNeonVivid() = camera("bunny_neon_vivid") { CameraFx.mask = 1; CameraFx.scene = 4; CameraFx.filter = 7 }
    @Test fun catSpaceBw() = camera("cat_space_bw") { CameraFx.mask = 2; CameraFx.scene = 6; CameraFx.filter = 5 }
    @Test fun crownHeartsWarm() = camera("crown_hearts_warm") { CameraFx.mask = 3; CameraFx.scene = 5; CameraFx.filter = 1 }
    @Test fun devilBeachGlam() = camera("devil_beach_glam") { CameraFx.mask = 6; CameraFx.scene = 3; CameraFx.apply(com.live.ishqexpress.ui.common.beautyPresets[3]) }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test fun beautyPanel() {
        CameraFx.mask = 1
        show { Box(Modifier.fillMaxSize()) { CameraBackdrop(true, brandPair, "me"); BeautyPanel(0) {} } }
        captureScreenRoboImage("$out/fx_panel_beauty.png")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test fun voiceChanger() {
        show { Box(Modifier.fillMaxSize()) { CameraBackdrop(true, brandPair, "me"); VoiceChangerSheet {} } }
        captureScreenRoboImage("$out/fx_panel_voice.png")
    }
}
