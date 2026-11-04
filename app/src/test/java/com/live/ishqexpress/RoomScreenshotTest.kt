package com.live.ishqexpress

import android.graphics.Color as AColor
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
import com.live.ishqexpress.ui.common.DemoWallet
import com.live.ishqexpress.ui.common.GiftTarget
import com.live.ishqexpress.ui.common.VideoBackdrop
import com.live.ishqexpress.ui.common.brandPair
import com.live.ishqexpress.ui.common.giftById
import com.live.ishqexpress.ui.gifts.GiftDrawer
import com.live.ishqexpress.ui.gifts.GiftEffectsLayer
import com.live.ishqexpress.ui.gifts.GiftEffectsState
import com.live.ishqexpress.ui.gifts.GiftEvent
import com.live.ishqexpress.ui.live.LiveViewerScreen
import com.live.ishqexpress.ui.nav.Dest
import com.live.ishqexpress.ui.nav.LocalNav
import com.live.ishqexpress.ui.nav.LocalToast
import com.live.ishqexpress.ui.nav.Navigator
import com.live.ishqexpress.ui.nav.Screen
import com.live.ishqexpress.ui.theme.IshqExpressTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the new rooms and gift effects on the JVM so they can be checked without a device.
 * Output: PNGs in app/build/outputs/roborazzi. Network photos are replaced by flat colours.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h860dp-xxhdpi")
class RoomScreenshotTest {

    @get:Rule val compose = createComposeRule()

    private val out = "build/outputs/roborazzi"

    @Before fun fakeImages() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val palette = intArrayOf(0xFFB98AA8.toInt(), 0xFF8D6E9E.toInt(), 0xFFD9A3B8.toInt(), 0xFF7A5C8F.toInt())
        Coil.setImageLoader(
            ImageLoader.Builder(ctx).components {
                add(coil.intercept.Interceptor { chain ->
                    val c = palette[(chain.request.data.hashCode() and 0x7fffffff) % palette.size]
                    SuccessResult(ColorDrawable(c), chain.request, DataSource.MEMORY)
                })
            }.build()
        )
        DemoWallet.coins = 1_240
    }

    private fun show(content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            IshqExpressTheme {
                CompositionLocalProvider(LocalNav provides Navigator(listOf(Dest(Screen.Main))), LocalToast provides {}) { content() }
            }
        }
    }

    private fun room(creator: Int, name: String, ms: Long = 9_000) {
        show { LiveViewerScreen(startCreator = creator, onClose = {}) }
        compose.mainClock.advanceTimeBy(ms)
        compose.onRoot().captureRoboImage("$out/$name.png")
    }

    @Test fun soloLive() = room(1, "01_solo_live")
    @Test fun pkBattle() = room(2, "02_pk_battle")
    @Test fun dualLive() = room(10, "03_dual_live")
    @Test fun videoParty() = room(3, "04_video_party")
    @Test fun chatParty() = room(5, "05_chat_party")

    private fun effect(name: String, ms: Long, post: GiftEffectsState.() -> Unit) {
        val state = GiftEffectsState()
        show {
            Box(Modifier.fillMaxSize()) {
                VideoBackdrop("bg", brandPair)
                GiftEffectsLayer(state)
            }
        }
        compose.mainClock.advanceTimeBy(100)
        compose.runOnUiThread { state.post() }
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        compose.mainClock.advanceTimeBy(ms)
        compose.onRoot().captureRoboImage("$out/$name.png")
    }

    private val host = GiftTarget(1, "Aanya Kapoor", "host")
    private fun ev(id: Int, n: Int = 1) = GiftEvent(giftById(id), n, "Amit K", "amit", 44, host)

    @Test fun motionCar() = effect("10_motion_car", 1_500) { post(ev(7)) }
    @Test fun motionCastle() = effect("11_motion_castle", 1_800) { post(ev(8)) }
    @Test fun motionRocket() = effect("12_motion_rocket", 1_400) { post(ev(9)) }
    @Test fun motionHeartsFallback() = effect("13_motion_love_storm_drawn", 1_500) { post(ev(22)) }
    @Test fun arCrown() = effect("15_ar_crown", 1_200) { post(ev(14)) }
    @Test fun comboBanners() = effect("16_combo_marquee", 900) {
        post(ev(1, 10)); post(ev(1, 66)); post(GiftEvent(giftById(4), 21, "Neha", "neha", 31, host))
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test fun giftDrawer() {
        show {
            Box(Modifier.fillMaxSize()) {
                VideoBackdrop("bg", brandPair)
                GiftDrawer(GiftEffectsState(), listOf(host, GiftTarget(9, "Priya Sharma", "p")), onDismiss = {}, onRecharge = {}, pkMode = true)
            }
        }
        compose.mainClock.advanceTimeBy(1_500)
        captureScreenRoboImage("$out/20_gift_drawer.png")
    }
}
