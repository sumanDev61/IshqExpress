package com.live.ishqexpress

import android.graphics.drawable.ColorDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.SuccessResult
import com.github.takahirom.roborazzi.captureRoboImage
import com.live.ishqexpress.ui.common.DemoWallet
import com.live.ishqexpress.ui.nav.Dest
import com.live.ishqexpress.ui.nav.LocalNav
import com.live.ishqexpress.ui.nav.LocalToast
import com.live.ishqexpress.ui.nav.MainShell
import com.live.ishqexpress.ui.nav.Navigator
import com.live.ishqexpress.ui.nav.Render
import com.live.ishqexpress.ui.nav.Screen
import com.live.ishqexpress.ui.onboarding.InterestsScreen
import com.live.ishqexpress.ui.onboarding.OtpScreen
import com.live.ishqexpress.ui.onboarding.PermissionsScreen
import com.live.ishqexpress.ui.onboarding.PhoneEntryScreen
import com.live.ishqexpress.ui.onboarding.ProfileDraft
import com.live.ishqexpress.ui.onboarding.ProfileSetupScreen
import com.live.ishqexpress.ui.onboarding.WelcomeScreen
import com.live.ishqexpress.ui.theme.IshqExpressTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders every screen of the app so layout can be checked on several phone sizes without a device.
 * Output: app/build/outputs/roborazzi/screens/<size>/<screen>.png
 */
abstract class AppScreensBase(private val size: String, private val fontScale: Float = 1f) {

    @get:Rule val compose = createComposeRule()

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

    private sealed interface Shot { val name: String }
    private data class AppScreen(val dest: Dest, override val name: String) : Shot
    private data class Custom(override val name: String, val content: @Composable () -> Unit) : Shot

    private val shots: List<Shot> = buildList {
        add(Custom("00a_welcome") { WelcomeScreen(onPhone = {}, onSocial = {}) })
        add(Custom("00b_phone") { PhoneEntryScreen(phone = "98765", onPhoneChange = {}, onBack = {}, onContinue = {}) })
        add(Custom("00c_otp") { OtpScreen(phone = "9876543210", onBack = {}, onVerified = {}) })
        add(Custom("00d_profile") { ProfileSetupScreen(draft = ProfileDraft(), onChange = {}, onBack = {}, onContinue = {}) })
        add(Custom("00e_interests") { InterestsScreen(selected = setOf(), onToggle = {}, onBack = {}, onContinue = {}) })
        add(Custom("00f_permissions") { PermissionsScreen(onBack = {}, onAllow = {}, onSkip = {}) })
        add(Custom("04b_GoLive_chat_party") { com.live.ishqexpress.ui.golive.GoLiveSetupScreen(onBack = {}, initialType = 1) })
        add(Custom("04c_GoLive_video_party") { com.live.ishqexpress.ui.golive.GoLiveSetupScreen(onBack = {}, initialType = 2) })
        listOf("home", "shorts", "explore", "chats").forEachIndexed { i, n -> add(Custom("01_tab_$n") { MainShell(initialTab = i) }) }
        Screen.entries.filter { it != Screen.Main }.forEachIndexed { i, s ->
            val arg = when (s) { Screen.Follows -> 0; Screen.AudioRoom -> 0; Screen.Broadcaster -> 0; else -> 1 }
            add(AppScreen(Dest(s, arg), "%02d_%s".format(i + 2, s.name)))
        }
    }

    @Test fun allScreens() {
        var current by mutableStateOf(shots.first())
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val d = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density, fontScale)) {
                IshqExpressTheme {
                    key(current.name) {
                        val nav = Navigator(listOf(Dest(Screen.Main)) + ((current as? AppScreen)?.let { listOf(it.dest) } ?: emptyList()))
                        CompositionLocalProvider(LocalNav provides nav, LocalToast provides {}) {
                            when (val s = current) {
                                is AppScreen -> Render(s.dest)
                                is Custom -> s.content()
                            }
                        }
                    }
                }
            }
        }
        val failures = mutableListOf<String>()
        for (s in shots) {
            if (only != null && only.none { s.name.contains(it, ignoreCase = true) }) continue
            try {
                compose.runOnUiThread { current = s }
                androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
                compose.mainClock.advanceTimeByFrame()
                compose.waitForIdle()
                compose.mainClock.advanceTimeBy(1_600)
                compose.onRoot().captureRoboImage("build/outputs/roborazzi/screens/$size/${s.name}.png")
            } catch (t: Throwable) {
                failures += "${s.name}: ${t.javaClass.simpleName} ${t.message?.take(200)}"
            }
        }
        if (failures.isNotEmpty()) println("SCREEN FAILURES:\n" + failures.joinToString("\n"))
    }

    /** -Dscreens=home,Wallet to render a subset. */
    private val only: List<String>? = System.getProperty("screens")?.split(',')?.filter { it.isNotBlank() }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w320dp-h640dp-xhdpi")
class ScreensSmallPhone : AppScreensBase("small_320")

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h860dp-xxhdpi")
class ScreensNormalPhone : AppScreensBase("normal_400")

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h760dp-xxhdpi")
class ScreensLargeFont : AppScreensBase("font130_360", fontScale = 1.3f)
