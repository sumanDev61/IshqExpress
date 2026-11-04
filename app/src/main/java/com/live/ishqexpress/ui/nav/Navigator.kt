package com.live.ishqexpress.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Every screen after onboarding, numbered against the research doc's mobile inventory (8–68)
 * plus the extra level/agency screens the doc's "Levels and tiers" section asks for.
 * Bottom sheets (gift drawer, share, report, …) live inside their host screen, not here.
 */
enum class Screen {
    Main,            // tabs: Home(7) · Explore(8) · Chats(45) · Me(52)
    Search,          // 9
    LiveViewer,      // 10–20 (sheets inside)
    GoLiveSetup,     // 21–22
    Broadcaster,     // 23–25, 30, 31, 33 (sheets inside)
    VideoParty,      // video party room you host (viewers reach parties via LiveViewer)
    Battle,          // 26–28
    DualBroadcast,   // 29
    LiveSummary,     // 32
    AudioRooms,      // 34, 36
    AudioRoom,       // 35 chat party (arg 0 = your own room)
    Match,           // 37–38
    Feed,            // 39, 42
    CreatePost,      // 40
    PostDetail,      // 41
    News,            // 43
    Notifications,   // 44
    ChatThread,      // 46
    MediaPacks,      // 47
    VideoCall,       // 48
    GroupCall,       // 49
    IncomingCall,    // 50
    OtherProfile,    // 53, 56
    EditProfile,     // 54
    Follows,         // 55
    Subscriptions,   // 57
    CoinStore,       // 58–59
    Transactions,    // 60
    Vip,             // 61
    DailyRewards,    // 62
    Earnings,        // 63
    Payout,          // 64
    PayoutHistory,   // 65
    Leaderboards,    // 66
    Settings,        // 67
    Help,            // 68
    UserLevel,       // extra
    Crowns,          // extra
    CreatorLevel,    // extra
    Agency,          // extra
    PhoneNumber,     // settings → phone
    LinkedAccounts,  // settings → linked accounts
    Legal,           // terms / privacy / community (arg = tab)
    About,
    SafetyCentre,
    PaymentMethods,  // payout methods (UPI, bank)
    KycCapture,      // arg 0 = document, 1 = selfie
    RankingRules,
    Replay,          // arg = creator id
    AgencyStatement, // arg = month index
    OfficialTalent,
    Shorts,          // full-screen short videos (arg = start index)
    Me,              // your profile (opened from the avatar on Home)
}

/** [arg] carries a single id (creator, post, room, conversation) or a tab index. */
data class Dest(val screen: Screen, val arg: Int = 0)

@Stable
class Navigator(initial: List<Dest>) {
    val stack = mutableStateListOf<Dest>().apply { addAll(initial) }
    val current: Dest get() = stack.last()
    val canPop: Boolean get() = stack.size > 1

    fun push(screen: Screen, arg: Int = 0) { stack.add(Dest(screen, arg)) }

    /** Swap the top screen, e.g. incoming call → call, broadcaster → summary. */
    fun replace(screen: Screen, arg: Int = 0) { stack[stack.lastIndex] = Dest(screen, arg) }

    fun pop() { if (canPop) stack.removeAt(stack.lastIndex) }

    fun popToRoot() { while (canPop) stack.removeAt(stack.lastIndex) }

    companion object {
        val saver: Saver<Navigator, List<String>> = Saver(
            save = { nav -> nav.stack.map { "${it.screen.name}:${it.arg}" } },
            restore = { saved ->
                Navigator(saved.map { s -> s.split(':').let { Dest(Screen.valueOf(it[0]), it[1].toInt()) } })
            }
        )
    }
}

@Composable
fun rememberNavigator(): Navigator = rememberSaveable(saver = Navigator.saver) { Navigator(listOf(Dest(Screen.Main))) }

val LocalNav = staticCompositionLocalOf<Navigator> { error("Navigator not provided") }

/** Shows a short snackbar message from anywhere (placeholder actions, confirmations). */
val LocalToast = staticCompositionLocalOf<(String) -> Unit> { {} }
