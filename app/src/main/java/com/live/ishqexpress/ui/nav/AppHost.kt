package com.live.ishqexpress.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.live.ishqexpress.ui.audio.AudioRoomScreen
import com.live.ishqexpress.ui.audio.AudioRoomsScreen
import com.live.ishqexpress.ui.chat.ChatThreadScreen
import com.live.ishqexpress.ui.chat.ChatsScreen
import com.live.ishqexpress.ui.chat.GroupCallScreen
import com.live.ishqexpress.ui.chat.IncomingCallScreen
import com.live.ishqexpress.ui.chat.MediaPacksScreen
import com.live.ishqexpress.ui.chat.PrivateShowScreen
import com.live.ishqexpress.ui.chat.VideoCallScreen
import com.live.ishqexpress.ui.explore.ExploreScreen
import com.live.ishqexpress.ui.explore.SearchScreen
import com.live.ishqexpress.ui.golive.BattleScreen
import com.live.ishqexpress.ui.golive.BroadcasterScreen
import com.live.ishqexpress.ui.golive.DualBroadcastScreen
import com.live.ishqexpress.ui.golive.GoLiveSetupScreen
import com.live.ishqexpress.ui.golive.LiveSummaryScreen
import com.live.ishqexpress.ui.home.HomeScreen
import com.live.ishqexpress.ui.live.LiveViewerScreen
import com.live.ishqexpress.ui.profile.CreatorLevelScreen
import com.live.ishqexpress.ui.profile.CrownsScreen
import com.live.ishqexpress.ui.profile.EditProfileScreen
import com.live.ishqexpress.ui.profile.FollowsScreen
import com.live.ishqexpress.ui.profile.MyProfileScreen
import com.live.ishqexpress.ui.profile.OtherProfileScreen
import com.live.ishqexpress.ui.profile.SubscriptionsScreen
import com.live.ishqexpress.ui.profile.UserLevelScreen
import com.live.ishqexpress.ui.settings.HelpScreen
import com.live.ishqexpress.ui.settings.SettingsScreen
import com.live.ishqexpress.ui.social.CreatePostScreen
import com.live.ishqexpress.ui.social.FeedScreen
import com.live.ishqexpress.ui.social.MatchScreen
import com.live.ishqexpress.ui.social.NewsScreen
import com.live.ishqexpress.ui.social.NotificationsScreen
import com.live.ishqexpress.ui.social.PostDetailScreen
import com.live.ishqexpress.ui.theme.BrandBrush
import com.live.ishqexpress.ui.theme.BrandOrchid
import com.live.ishqexpress.ui.theme.BrandPink
import com.live.ishqexpress.ui.theme.Canvas
import com.live.ishqexpress.ui.theme.Hairline
import com.live.ishqexpress.ui.theme.InkTertiary
import com.live.ishqexpress.ui.theme.LiveRed
import com.live.ishqexpress.ui.wallet.AgencyScreen
import com.live.ishqexpress.ui.wallet.CoinStoreScreen
import com.live.ishqexpress.ui.wallet.DailyRewardsScreen
import com.live.ishqexpress.ui.wallet.EarningsScreen
import com.live.ishqexpress.ui.wallet.LeaderboardsScreen
import com.live.ishqexpress.ui.wallet.PayoutHistoryScreen
import com.live.ishqexpress.ui.wallet.PayoutScreen
import com.live.ishqexpress.ui.wallet.TransactionsScreen
import com.live.ishqexpress.ui.wallet.VipScreen
import kotlinx.coroutines.launch
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.live.ishqexpress.ui.common.Bubble3D
import com.live.ishqexpress.ui.common.BubbleGlyph
import com.live.ishqexpress.ui.common.FitText
import com.live.ishqexpress.ui.common.GlossyIcon
import com.live.ishqexpress.ui.common.Ramp
import com.live.ishqexpress.ui.common.Ramps
import com.live.ishqexpress.ui.theme.BrandViolet
import com.live.ishqexpress.ui.theme.appBackground

/** Screens that open from the bottom (full-screen modal feel) rather than sliding in from the side. */
private val modalScreens = setOf(
    Screen.LiveViewer, Screen.GoLiveSetup, Screen.IncomingCall, Screen.VideoCall,
    Screen.GroupCall, Screen.PrivateShow, Screen.CreatePost, Screen.Match, Screen.VideoParty, Screen.AudioRoom,
    Screen.KycCapture, Screen.Replay,
)

@Composable
fun AppHost() {
    val nav = rememberNavigator()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val toast: (String) -> Unit = { msg ->
        scope.launch { snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar(msg) }
    }

    BackHandler(enabled = nav.canPop) { nav.pop() }

    // Keeps each stacked screen's rememberSaveable state (e.g. the selected tab in MainShell)
    // alive while another screen covers it, and drops it once the screen is popped.
    val holder = rememberSaveableStateHolder()
    val liveKeys = remember { mutableSetOf<String>() }
    val stackKeys = nav.stack.mapIndexed { i, d -> stateKey(d, i) }.toSet()
    LaunchedEffect(stackKeys) {
        (liveKeys - stackKeys).forEach { holder.removeState(it) }
        liveKeys.retainAll(stackKeys)
    }

    CompositionLocalProvider(LocalNav provides nav, LocalToast provides toast) {
        Box(Modifier.fillMaxSize().background(Canvas)) {
            AnimatedContent(
                targetState = nav.current to nav.stack.size,
                transitionSpec = {
                    val forward = targetState.second >= initialState.second
                    val target = if (forward) targetState.first.screen else initialState.first.screen
                    when {
                        target in modalScreens && forward ->
                            (slideInVertically(tween(340)) { it / 2 } + fadeIn(tween(260))) togetherWith fadeOut(tween(200))
                        target in modalScreens ->
                            fadeIn(tween(220)) togetherWith fadeOut(tween(220))
                        forward ->
                            (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(300)) { -it / 5 } + fadeOut(tween(200)))
                        else ->
                            (slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300))) togetherWith
                                (slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(200)))
                    }
                },
                contentKey = { "${it.first.screen}:${it.first.arg}:${it.second}" },
                label = "app-nav"
            ) { (dest, size) ->
                val key = stateKey(dest, size - 1)
                liveKeys += key
                holder.SaveableStateProvider(key) { Render(dest) }
            }

            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 72.dp))
        }
    }
}

private fun stateKey(dest: Dest, depth: Int) = "$depth:${dest.screen}:${dest.arg}"

@Composable
internal fun Render(dest: Dest) {
    val nav = LocalNav.current
    val back: () -> Unit = { nav.pop() }
    when (dest.screen) {
        Screen.Main -> MainShell()
        Screen.Search -> SearchScreen(back)
        Screen.LiveViewer -> LiveViewerScreen(startCreator = dest.arg, onClose = back)
        Screen.GoLiveSetup -> GoLiveSetupScreen(back)
        Screen.Broadcaster -> BroadcasterScreen(audioOnly = dest.arg == 1)
        Screen.VideoParty -> com.live.ishqexpress.ui.party.VideoPartyRoom(host = null, isHost = true, active = true, onClose = back)
        Screen.Battle -> BattleScreen(opponent = dest.arg)
        Screen.DualBroadcast -> DualBroadcastScreen(partner = dest.arg)
        Screen.LiveSummary -> LiveSummaryScreen()
        Screen.AudioRooms -> AudioRoomsScreen(back)
        Screen.AudioRoom -> AudioRoomScreen(roomId = dest.arg, onLeave = back)
        Screen.Match -> MatchScreen(back)
        Screen.Feed -> FeedScreen(back)
        Screen.CreatePost -> CreatePostScreen(back)
        Screen.PostDetail -> PostDetailScreen(postId = dest.arg, onBack = back)
        Screen.News -> NewsScreen(back)
        Screen.Notifications -> NotificationsScreen(back)
        Screen.ChatThread -> ChatThreadScreen(conversationId = dest.arg, onBack = back)
        Screen.MediaPacks -> MediaPacksScreen(back)
        Screen.VideoCall -> VideoCallScreen(creatorId = dest.arg)
        Screen.GroupCall -> GroupCallScreen()
        Screen.IncomingCall -> IncomingCallScreen(creatorId = dest.arg)
        Screen.PrivateShow -> PrivateShowScreen(creatorId = dest.arg)
        Screen.OtherProfile -> OtherProfileScreen(creatorId = dest.arg, onBack = back)
        Screen.EditProfile -> EditProfileScreen(back)
        Screen.Follows -> FollowsScreen(startTab = dest.arg, onBack = back)
        Screen.Subscriptions -> SubscriptionsScreen(back)
        Screen.CoinStore -> CoinStoreScreen(back)
        Screen.Transactions -> TransactionsScreen(back)
        Screen.Vip -> VipScreen(back)
        Screen.DailyRewards -> DailyRewardsScreen(back)
        Screen.Earnings -> EarningsScreen(back)
        Screen.Payout -> PayoutScreen(back)
        Screen.PayoutHistory -> PayoutHistoryScreen(back)
        Screen.Leaderboards -> LeaderboardsScreen(back)
        Screen.Settings -> SettingsScreen(back)
        Screen.Help -> HelpScreen(back)
        Screen.UserLevel -> UserLevelScreen(back)
        Screen.Crowns -> CrownsScreen(back)
        Screen.CreatorLevel -> CreatorLevelScreen(back)
        Screen.Agency -> AgencyScreen(back)
        Screen.PhoneNumber -> com.live.ishqexpress.ui.more.PhoneNumberScreen(back)
        Screen.LinkedAccounts -> com.live.ishqexpress.ui.more.LinkedAccountsScreen(back)
        Screen.Legal -> com.live.ishqexpress.ui.more.LegalScreen(startTab = dest.arg, onBack = back)
        Screen.About -> com.live.ishqexpress.ui.more.AboutScreen(back)
        Screen.SafetyCentre -> com.live.ishqexpress.ui.more.SafetyCentreScreen(back)
        Screen.PaymentMethods -> com.live.ishqexpress.ui.more.PaymentMethodsScreen(back)
        Screen.KycCapture -> com.live.ishqexpress.ui.more.KycCaptureScreen(selfie = dest.arg == 1, onBack = back)
        Screen.RankingRules -> com.live.ishqexpress.ui.more.RankingRulesScreen(back)
        Screen.Replay -> com.live.ishqexpress.ui.more.ReplayPlayerScreen(creatorId = dest.arg, onBack = back)
        Screen.AgencyStatement -> com.live.ishqexpress.ui.more.AgencyStatementScreen(month = dest.arg, onBack = back)
        Screen.OfficialTalent -> com.live.ishqexpress.ui.more.OfficialTalentScreen(back)
    }
}

/* ============================== Main tabs ============================== */

private enum class Tab { Home, Explore, Chats, Me }

@Composable
internal fun MainShell(initialTab: Int = 0) {
    val nav = LocalNav.current
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    BackHandler(enabled = tab != Tab.Home.ordinal) { tab = Tab.Home.ordinal }

    Column(Modifier.fillMaxSize().appBackground()) {
        Box(Modifier.weight(1f)) {
            when (Tab.entries[tab]) {
                Tab.Home -> HomeScreen()
                Tab.Explore -> ExploreScreen()
                Tab.Chats -> ChatsScreen()
                Tab.Me -> MyProfileScreen()
            }
        }
        BottomBar(
            selected = Tab.entries[tab],
            onSelect = { tab = it.ordinal },
            onGoLive = { nav.push(Screen.GoLiveSetup) }
        )
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit, onGoLive: () -> Unit) {
    val barShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .shadow(18.dp, barShape, ambientColor = BrandPink.copy(alpha = 0.35f), spotColor = BrandViolet.copy(alpha = 0.55f))
                .clip(barShape)
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF5FA))))
                .drawBehind {
                    // Brand hairline along the top edge.
                    drawRect(Brush.horizontalGradient(listOf(BrandPink.copy(alpha = 0.0f), BrandPink.copy(alpha = 0.55f), BrandViolet.copy(alpha = 0.55f), BrandViolet.copy(alpha = 0f))), size = Size(size.width, 1.dp.toPx()))
                }
                .navigationBarsPadding()
                .height(66.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Rounded.Home, "Home", Ramps.home, selected == Tab.Home, Modifier.weight(1f)) { onSelect(Tab.Home) }
            NavItem(Icons.Rounded.Explore, "Explore", Ramps.explore, selected == Tab.Explore, Modifier.weight(1f)) { onSelect(Tab.Explore) }
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                FitText("Go Live", MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, brush = BrandBrush.text), Modifier.padding(bottom = 5.dp))
            }
            NavItem(Icons.Rounded.ChatBubble, "Messages", Ramps.chats, selected == Tab.Chats, Modifier.weight(1f), badge = 3) { onSelect(Tab.Chats) }
            NavItem(Icons.Rounded.Person, "Me", Ramps.me, selected == Tab.Me, Modifier.weight(1f)) { onSelect(Tab.Me) }
        }
        GoLiveOrb(onGoLive, Modifier.align(Alignment.TopCenter).offset(y = (-10).dp))
    }
}

/** Raised 3D "Go live" button that sits above the bar with a slow pulsing halo. */
@Composable
private fun GoLiveOrb(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "orb").animateFloat(
        0f, 1f, infiniteRepeatable(tween(2_200, easing = LinearEasing)), label = "pulse"
    )
    Box(modifier.size(74.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(56.dp).graphicsLayer {
                val p = pulse.value
                scaleX = 1f + p * 0.32f; scaleY = scaleX; alpha = (1f - p) * 0.55f
            }.clip(CircleShape).background(Brush.radialGradient(listOf(BrandPink.copy(alpha = 0.6f), BrandViolet.copy(alpha = 0f))))
        )
        Bubble3D(
            Ramps.live,
            Modifier.size(56.dp).border(3.dp, Color.White, CircleShape).clickable(onClick = onClick),
            shape = CircleShape, elevation = 14.dp
        ) { BubbleGlyph(Icons.Rounded.Videocam, 28.dp, "Go live") }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    ramp: Ramp,
    selected: Boolean,
    modifier: Modifier = Modifier,
    badge: Int? = null,
    onClick: () -> Unit,
) {
    val lift by animateDpAsState(if (selected) (-3).dp else 0.dp, spring(dampingRatio = 0.55f, stiffness = 500f), label = "lift")
    val scale by animateFloatAsState(if (selected) 1f else 0.92f, spring(dampingRatio = 0.55f, stiffness = 500f), label = "scale")
    Column(
        modifier.fillMaxHeight().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.height(38.dp).offset(y = lift).graphicsLayer { scaleX = scale; scaleY = scale }, contentAlignment = Alignment.Center) {
            if (selected) {
                Bubble3D(ramp, Modifier.size(width = 48.dp, height = 36.dp), RoundedCornerShape(14.dp), elevation = 9.dp) { BubbleGlyph(icon, 22.dp, label) }
            } else {
                GlossyIcon(icon, ramp, 27.dp, Modifier.graphicsLayer { alpha = 0.85f }, contentDescription = label)
            }
            if (badge != null) {
                Text(
                    "$badge",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-1).dp)
                        .shadow(3.dp, CircleShape, spotColor = LiveRed)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFFFF6B88), LiveRed)))
                        .border(1.5.dp, Color.White, CircleShape)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        FitText(
            label,
            MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                brush = if (selected) ramp.diagonal else null,
            ),
            color = if (selected) Color.Unspecified else InkTertiary,
        )
    }
}
