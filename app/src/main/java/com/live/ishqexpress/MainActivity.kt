package com.live.ishqexpress

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.live.ishqexpress.data.Session
import com.live.ishqexpress.ui.nav.AppHost
import androidx.compose.runtime.LaunchedEffect
import com.live.ishqexpress.ui.onboarding.InterestsScreen
import com.live.ishqexpress.ui.onboarding.OtpScreen
import com.live.ishqexpress.ui.onboarding.PermissionsScreen
import com.live.ishqexpress.ui.onboarding.PhoneEntryScreen
import com.live.ishqexpress.ui.onboarding.ProfileDraft
import com.live.ishqexpress.ui.onboarding.ProfileSetupScreen
import com.live.ishqexpress.ui.onboarding.SplashScreen
import com.live.ishqexpress.ui.onboarding.WelcomeScreen
import com.live.ishqexpress.ui.theme.IshqExpressTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.init(applicationContext)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            IshqExpressTheme { IshqApp() }
        }
    }
}

/** Screen order follows the research doc's onboarding inventory (screens 1–6), then Home (7). */
enum class Route { Splash, Welcome, Phone, Otp, Profile, Interests, Permissions, Home }

@Composable
fun IshqApp() {
    // Signed-in users skip onboarding; signing out (Settings) drops back to Welcome.
    var route by rememberSaveable { mutableStateOf(if (Session.signedIn) Route.Home else Route.Splash) }
    LaunchedEffect(Session.signedIn) { if (!Session.signedIn && route == Route.Home) route = Route.Welcome }
    var phone by rememberSaveable { mutableStateOf("") }
    var profile by remember { mutableStateOf(ProfileDraft()) }
    var interests by rememberSaveable { mutableStateOf(setOf<String>()) }

    fun back(to: Route) { route = to }

    val previous = mapOf(
        Route.Phone to Route.Welcome,
        Route.Otp to Route.Phone,
        Route.Profile to Route.Otp,
        Route.Interests to Route.Profile,
        Route.Permissions to Route.Interests,
    )
    BackHandler(enabled = route in previous) { route = previous.getValue(route) }

    AnimatedContent(
        targetState = route,
        transitionSpec = {
            if (initialState == Route.Splash || targetState == Route.Home) {
                fadeIn(tween(400)) togetherWith fadeOut(tween(300))
            } else if (targetState.ordinal > initialState.ordinal) {
                (slideInHorizontally(tween(320)) { it / 3 } + fadeIn(tween(320))) togetherWith
                    (slideOutHorizontally(tween(320)) { -it / 4 } + fadeOut(tween(200)))
            } else {
                (slideInHorizontally(tween(320)) { -it / 3 } + fadeIn(tween(320))) togetherWith
                    (slideOutHorizontally(tween(320)) { it / 4 } + fadeOut(tween(200)))
            }
        },
        label = "nav"
    ) { r ->
        when (r) {
            Route.Splash -> SplashScreen(onFinished = { route = Route.Welcome })
            Route.Welcome -> WelcomeScreen(
                onPhone = { route = Route.Phone },
                onSocial = { route = Route.Profile } // social sign-in skips OTP
            )
            Route.Phone -> PhoneEntryScreen(
                phone = phone,
                onPhoneChange = { phone = it },
                onBack = { back(Route.Welcome) },
                onContinue = { route = Route.Otp }
            )
            Route.Otp -> OtpScreen(
                phone = phone,
                onBack = { back(Route.Phone) },
                onVerified = { com.live.ishqexpress.data.Session.setString("phone", "${com.live.ishqexpress.ui.onboarding.selectedDial()} $phone"); route = Route.Profile }
            )
            Route.Profile -> ProfileSetupScreen(
                draft = profile,
                onChange = { profile = it },
                onBack = { back(Route.Otp) },
                onContinue = { route = Route.Interests }
            )
            Route.Interests -> InterestsScreen(
                selected = interests,
                onToggle = { interests = if (it in interests) interests - it else interests + it },
                onBack = { back(Route.Profile) },
                onContinue = { route = Route.Permissions }
            )
            Route.Permissions -> PermissionsScreen(
                onBack = { back(Route.Interests) },
                onAllow = { Session.signIn(profile.name, profile.photo?.toString(), interests); route = Route.Home },
                onSkip = { Session.signIn(profile.name, profile.photo?.toString(), interests); route = Route.Home }
            )
            Route.Home -> AppHost()
        }
    }
}
