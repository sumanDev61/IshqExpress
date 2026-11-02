package com.live.ishqexpress.ui.live

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.common.*
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.home.LiveCreator
import com.live.ishqexpress.ui.home.compact
import com.live.ishqexpress.ui.home.sampleCreators
import com.live.ishqexpress.ui.nav.*
import com.live.ishqexpress.ui.theme.*
import com.live.ishqexpress.ui.home.LiveMode
import kotlinx.coroutines.delay

/*
 * 10 Swipe live viewer · 11 Viewer list · 12 Mini profile · 13 Gift drawer · 14 Gift combo layer
 * 15 Goal bar · 16 Premium entry · 17 Private show request · 18 Join party · 19 Share · 20 Report
 */

private sealed interface ViewerSheet {
    data object Viewers : ViewerSheet
    data class Mini(val name: String, val img: Int, val level: Int) : ViewerSheet
    data object Gifts : ViewerSheet
    data object PrivateShow : ViewerSheet
    data object JoinParty : ViewerSheet
    data object Share : ViewerSheet
    data object Report : ViewerSheet
    data object More : ViewerSheet
}

/** A gift that is currently animating on screen. */
private data class GiftBurst(val gift: Gift, val count: Int, val key: Long)

@Composable
fun LiveViewerScreen(startCreator: Int, onClose: () -> Unit) {
    DarkSystemBars()
    val startIndex = sampleCreators.indexOfFirst { it.id == startCreator }.coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = startIndex) { sampleCreators.size }

    VerticalPager(pager, Modifier.fillMaxSize().background(Color.Black), beyondViewportPageCount = 1) { page ->
        LiveRoom(sampleCreators[page], active = pager.currentPage == page, onClose = onClose)
    }
}

@Composable
private fun LiveRoom(creator: LiveCreator, active: Boolean, onClose: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    var sheet by remember { mutableStateOf<ViewerSheet?>(null) }
    var following by remember { mutableStateOf(false) }
    var coins by remember { mutableIntStateOf(Me.coins) }
    var goal by remember { mutableIntStateOf(3_420) }
    val goalTarget = 5_000
    var burst by remember { mutableStateOf<GiftBurst?>(null) }
    val comments = remember { mutableStateListOf<LiveComment>().apply { addAll(sampleLiveComments) } }
    // Creator 6 runs a ticketed (premium) broadcast in the sample data.
    var premiumLocked by remember { mutableStateOf(creator.id == 6) }
    var partyRequested by remember { mutableStateOf(false) }
    var likes by remember { mutableIntStateOf(0) }

    // Fake incoming chat while the page is on screen.
    LaunchedEffect(active, premiumLocked) {
        val extra = listOf("❤️❤️❤️", "Hi from Pune!", "sent Rose", "Wah wah 👏", "Follow back please 🙏", "Best live today")
        var i = 0
        while (active && !premiumLocked) {
            delay(2600)
            comments.add(LiveComment(sampleViewers[i % sampleViewers.size].name, sampleViewers[i % sampleViewers.size].level, extra[i % extra.size]))
            if (comments.size > 40) comments.removeAt(1)
            i++
        }
    }

    fun sendGift(g: Gift, count: Int) {
        val cost = g.coins * count
        if (cost > coins) { toast("Not enough coins"); nav.push(Screen.CoinStore); return }
        coins -= cost
        goal = (goal + cost).coerceAtMost(goalTarget)
        burst = GiftBurst(g, count, System.nanoTime())
        comments.add(LiveComment(Me.name, Me.level, "sent ${g.name} ×$count", gift = g.emoji))
    }

    Box(Modifier.fillMaxSize()) {
        VideoBackdrop(creator.imageUrl.replace("600", "900"), creator.tint)

        // Live Party: guest tiles down the right side.
        if (creator.mode == LiveMode.Party) PartyGuests(Modifier.align(Alignment.CenterEnd).padding(end = 10.dp))

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // ---- Header
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.32f))
                        .clickable { sheet = ViewerSheet.Mini(creator.name, creator.photo, 38) }
                        .padding(start = 3.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(creator.imageUrl, 34.dp)
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.widthIn(max = 100.dp)) {
                        Text(creator.name, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        CoinAmount(186_400, diamond = true, iconSize = 10.dp, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.width(6.dp))
                    AnimatedContent(following, label = "follow") { f ->
                        if (f) Icon(Icons.Rounded.Check, "Following", tint = Color.White, modifier = Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).padding(6.dp))
                        else Icon(Icons.Rounded.Add, "Follow", tint = Color.White, modifier = Modifier.size(30.dp).clip(CircleShape).background(BrandBrush.primary).clickable { following = true; toast("Following ${creator.name}") }.padding(5.dp))
                    }
                }
                Spacer(Modifier.weight(1f))
                // Top fans (11)
                Row(Modifier.clickable { sheet = ViewerSheet.Viewers }, verticalAlignment = Alignment.CenterVertically) {
                    sampleViewers.take(3).forEachIndexed { i, v ->
                        Avatar(avatar(v.img), 30.dp, Modifier.offset(x = (-6 * i).dp))
                    }
                    GlassPill(Modifier.offset(x = (-10).dp)) {
                        Text(creator.viewers.compact(), style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
                GlassButton(Icons.Rounded.Close, "Close", onClose, size = 34.dp)
            }
            // ---- Ranking + goal (15)
            Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassPill(Modifier.clickable { nav.push(Screen.Leaderboards) }) {
                    Text("🏆 Hourly #${creator.id + 2}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
                GlassPill { Text("ID ${80_000 + creator.id * 137}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f)) }
                if (creator.mode != LiveMode.Solo) GlassPill { Text(creator.mode.label, style = MaterialTheme.typography.labelSmall, color = Color.White) }
            }
            Spacer(Modifier.height(8.dp))
            GoalBar(goal, goalTarget, Modifier.padding(horizontal = 12.dp).width(210.dp))

            Spacer(Modifier.weight(1f))

            // ---- Gift combo banner (14)
            burst?.let { b -> ComboBanner(b, Modifier.padding(start = 12.dp, bottom = 8.dp)) }

            // ---- Chat
            ChatFeed(
                comments,
                Modifier.padding(start = 12.dp, end = 80.dp).height(230.dp),
                onUser = { c -> if (!c.system) sheet = ViewerSheet.Mini(c.user, sampleViewers.firstOrNull { it.name == c.user }?.img ?: Me.photo, c.level) }
            )
            Spacer(Modifier.height(8.dp))

            // ---- Bottom actions
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                var draft by remember { mutableStateOf("") }
                Row(
                    Modifier.weight(1f).height(42.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)).padding(start = 14.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        if (draft.isEmpty()) Text("Say hi…", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
                        BasicTextField(
                            draft, { draft = it }, singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (draft.isNotBlank()) {
                        Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = Color.White, modifier = Modifier.size(34.dp).clip(CircleShape).background(BrandBrush.primary).clickable {
                            comments.add(LiveComment(Me.name, Me.level, draft.trim())); draft = ""
                        }.padding(8.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (creator.mode == LiveMode.Party) {
                    GlassButton(Icons.Rounded.GroupAdd, "Join party", { sheet = ViewerSheet.JoinParty }, highlighted = partyRequested)
                    Spacer(Modifier.width(6.dp))
                }
                GlassButton(Icons.Rounded.MoreHoriz, "More", { sheet = ViewerSheet.More })
                Spacer(Modifier.width(6.dp))
                GlassButton(Icons.Rounded.Favorite, "Like", { likes++ }, tint = Color(0xFFFF7AB6))
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(44.dp).shadow(8.dp, CircleShape, spotColor = BrandPink).clip(CircleShape).background(BrandBrush.deep).clickable { sheet = ViewerSheet.Gifts },
                    contentAlignment = Alignment.Center
                ) { Text("🎁", fontSize = 22.sp) }
            }
        }

        // Floating hearts from likes
        FloatingHearts(likes, Modifier.align(Alignment.BottomEnd).padding(end = 70.dp, bottom = 70.dp))

        // Full-screen gift animation (14)
        burst?.takeIf { it.gift.fullScreen }?.let { b -> FullScreenGift(b) }
        LaunchedEffect(burst?.key) { if (burst != null) { delay(3200); burst = null } }

        // Premium broadcast gate (16)
        if (premiumLocked) PremiumGate(creator, coins, onClose = onClose, onPay = { fee ->
            if (fee > coins) { toast("Not enough coins"); nav.push(Screen.CoinStore) } else { coins -= fee; premiumLocked = false; toast("Welcome in! Enjoy the show") }
        })
    }

    when (val s = sheet) {
        null -> {}
        ViewerSheet.Viewers -> ViewerListSheet(creator, onDismiss = { sheet = null }, onUser = { v -> sheet = ViewerSheet.Mini(v.name, v.img, v.level) })
        is ViewerSheet.Mini -> MiniProfileSheet(
            s, isHost = s.name == creator.name,
            onDismiss = { sheet = null },
            onProfile = { sheet = null; nav.push(Screen.OtherProfile, creator.id) },
            onGift = { sheet = ViewerSheet.Gifts },
            onReport = { sheet = ViewerSheet.Report },
            onMention = { comments.add(LiveComment(Me.name, Me.level, "@${s.name} 👋")); sheet = null },
            onPrivate = { sheet = ViewerSheet.PrivateShow },
        )
        ViewerSheet.Gifts -> GiftDrawer(coins, onDismiss = { sheet = null }, onRecharge = { sheet = null; nav.push(Screen.CoinStore) }, onSend = { g, n -> sendGift(g, n) })
        ViewerSheet.PrivateShow -> PrivateShowSheet(creator, coins, onDismiss = { sheet = null }, onRequest = { sheet = null; toast("Request sent — ${creator.name.substringBefore(' ')} will accept soon"); nav.push(Screen.PrivateShow, creator.id) })
        ViewerSheet.JoinParty -> JoinPartySheet(creator, partyRequested, onDismiss = { sheet = null }, onToggle = { partyRequested = !partyRequested; sheet = null; toast(if (partyRequested) "Request sent to host" else "Request cancelled") })
        ViewerSheet.Share -> ShareSheet(creator, onDismiss = { sheet = null })
        ViewerSheet.Report -> ReportSheet(creator.name, onDismiss = { sheet = null }, onSubmit = { sheet = null; toast("Thanks — our safety team will review this") })
        ViewerSheet.More -> MoreSheet(
            creator,
            onDismiss = { sheet = null },
            onShare = { sheet = ViewerSheet.Share },
            onPrivate = { sheet = ViewerSheet.PrivateShow },
            onReport = { sheet = ViewerSheet.Report },
            onSubscribe = { sheet = null; nav.push(Screen.OtherProfile, creator.id) },
        )
    }
}

/* ------------------------------ 15 · Goal bar ------------------------------ */

@Composable
fun GoalBar(current: Int, target: Int, modifier: Modifier = Modifier, title: String = "Goal: Diwali song request 🪔") {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.32f)).padding(horizontal = 10.dp, vertical = 7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (current >= target) "Done!" else "${current * 100 / target}%", style = MaterialTheme.typography.labelSmall, color = Gold)
        }
        Spacer(Modifier.height(5.dp))
        ProgressBar(current / target.toFloat(), height = 5.dp, track = Color.White.copy(alpha = 0.25f), brush = Brush.horizontalGradient(listOf(Gold, BrandPink)))
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(10.dp)
            Text(" ${current.grouped()} / ${target.grouped()}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White.copy(alpha = 0.8f))
        }
    }
}

/* ------------------------------ Chat ------------------------------ */

@Composable
fun ChatFeed(comments: List<LiveComment>, modifier: Modifier = Modifier, onUser: (LiveComment) -> Unit = {}) {
    val state = rememberLazyListState()
    LaunchedEffect(comments.size) { if (comments.isNotEmpty()) state.animateScrollToItem(comments.lastIndex) }
    LazyColumn(
        state = state,
        modifier = modifier.graphicsLayer { alpha = 0.99f }.drawWithContent {
            drawContent()
            drawRect(Brush.verticalGradient(0f to Color.Transparent, 0.18f to Color.Black), blendMode = androidx.compose.ui.graphics.BlendMode.DstIn)
        },
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Bottom)
    ) {
        items(comments) { c -> ChatLine(c) { onUser(c) } }
    }
}

@Composable
private fun ChatLine(c: LiveComment, onClick: () -> Unit) {
    val bg = when {
        c.system -> BrandPink.copy(alpha = 0.35f)
        c.gift != null -> BrandOrchid.copy(alpha = 0.45f)
        else -> Color.Black.copy(alpha = 0.28f)
    }
    Row(
        Modifier.clip(RoundedCornerShape(14.dp)).background(bg).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!c.system) { LevelBadge(c.level); Spacer(Modifier.width(5.dp)) }
        Text(
            buildAnnotatedStringSafe(if (c.system) "" else "${c.user}: ", c.text),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White,
        )
        if (c.gift != null) { Spacer(Modifier.width(4.dp)); Text(c.gift, fontSize = 16.sp) }
    }
}

private fun buildAnnotatedStringSafe(name: String, text: String) =
    androidx.compose.ui.text.buildAnnotatedString {
        pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFFFFC1DC), fontWeight = FontWeight.SemiBold)); append(name); pop()
        append(text)
    }

/* ------------------------------ Party guests ------------------------------ */

@Composable
private fun PartyGuests(modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(3, 9, 10).forEachIndexed { i, id ->
            val c = creatorById(id)
            Box(Modifier.size(width = 84.dp, height = 110.dp).clip(RoundedCornerShape(12.dp)).border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))) {
                AsyncImage(c.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Text(
                    c.name.substringBefore(' '), style = MaterialTheme.typography.labelSmall, color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(alpha = 0.4f)).padding(4.dp)
                )
                if (i == 1) Icon(Icons.Rounded.MicOff, null, tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(14.dp))
            }
        }
        Box(
            Modifier.size(width = 84.dp, height = 110.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.3f)).border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Add, null, tint = Color.White)
                Text("Open seat", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

/* ------------------------------ 14 · Gift animations ------------------------------ */

@Composable
private fun ComboBanner(b: GiftBurst, modifier: Modifier) {
    val scale = remember(b.key) { Animatable(0.4f) }
    LaunchedEffect(b.key) { scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f)) }
    Row(
        modifier
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(BrandPink.copy(alpha = 0.9f), BrandOrchid.copy(alpha = 0.6f), Color.Transparent)))
            .padding(start = 4.dp, end = 30.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(Me.avatarUrl, 34.dp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(Me.name, style = MaterialTheme.typography.labelMedium, color = Color.White)
            Text("sent ${b.gift.name}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
        }
        Spacer(Modifier.width(8.dp))
        Text(b.gift.emoji, fontSize = 30.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            "×${b.count}",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
            color = Gold,
            modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
        )
    }
}

@Composable
private fun FullScreenGift(b: GiftBurst) {
    val t = remember(b.key) { Animatable(0f) }
    LaunchedEffect(b.key) { t.animateTo(1f, tween(2800, easing = LinearEasing)) }
    val v = t.value
    val scale = when { v < 0.2f -> v / 0.2f * 1.2f; v < 0.3f -> 1.2f - (v - 0.2f) * 2f; else -> 1f }
    val alpha = if (v > 0.8f) (1f - v) / 0.2f else 1f
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f * alpha)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }) {
            Text(b.gift.emoji, fontSize = 140.sp)
            Text(
                "${Me.name} sent ${b.gift.name}!",
                style = MaterialTheme.typography.titleLarge, color = Color.White,
                modifier = Modifier.clip(CircleShape).background(BrandBrush.primary).padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        // sparkles
        repeat(10) { i ->
            val angle = i * 36f + v * 90f
            val r = 120f + v * 160f
            Text("✨", fontSize = 22.sp, modifier = Modifier.graphicsLayer {
                translationX = (r * kotlin.math.cos(Math.toRadians(angle.toDouble()))).toFloat()
                translationY = (r * kotlin.math.sin(Math.toRadians(angle.toDouble()))).toFloat()
                this.alpha = alpha
            })
        }
    }
}

@Composable
private fun FloatingHearts(count: Int, modifier: Modifier) {
    val hearts = remember { mutableStateListOf<Int>() }
    LaunchedEffect(count) { if (count > 0) { hearts.add(count); delay(1600); hearts.remove(count) } }
    Box(modifier.size(width = 60.dp, height = 260.dp), contentAlignment = Alignment.BottomCenter) {
        hearts.forEach { h ->
            key(h) {
                val y = remember { Animatable(0f) }
                LaunchedEffect(Unit) { y.animateTo(1f, tween(1500, easing = FastOutSlowInEasing)) }
                Text(
                    listOf("❤️", "💖", "💗", "💜")[h % 4], fontSize = 26.sp,
                    modifier = Modifier.graphicsLayer {
                        translationY = -y.value * 600f
                        translationX = kotlin.math.sin(y.value * 6f + h) * 30f
                        alpha = 1f - y.value
                    }
                )
            }
        }
    }
}

/* ------------------------------ 16 · Premium entry ------------------------------ */

@Composable
private fun PremiumGate(creator: LiveCreator, coins: Int, onClose: () -> Unit, onPay: (Int) -> Unit) {
    val fee = 500
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).blur(0.dp).clickable(enabled = false) {}) {
        AsyncImage(creator.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().blur(30.dp).alpha(0.6f))
        GlassButton(Icons.Rounded.Close, "Close", onClose, Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp), size = 34.dp)
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(Canvas).navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(BrandBrush.deep), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Key, null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("Premium live", style = MaterialTheme.typography.headlineSmall, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("${creator.name} is hosting a ticketed show. Pay once to watch the whole stream.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PinkTint).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Entry ticket", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text("Super Fans enter free", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                CoinAmount(fee, iconSize = 18.dp, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(8.dp))
            Text("Your balance: ${coins.grouped()} coins", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Pay ${fee.grouped()} coins & enter", onClick = { onPay(fee) })
            Spacer(Modifier.height(4.dp))
            Text(
                "Become a Super Fan instead",
                style = MaterialTheme.typography.titleSmall, color = BrandPink,
                modifier = Modifier.clip(CircleShape).clickable { }.padding(12.dp)
            )
        }
    }
}

/* ------------------------------ 11 · Viewer list ------------------------------ */

@Composable
private fun ViewerListSheet(creator: LiveCreator, onDismiss: () -> Unit, onUser: (Viewer) -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    IshqSheet(onDismiss) {
        UnderlineTabs(listOf("Top fans", "Viewers (${creator.viewers.compact()})"), tab, { tab = it })
        val list = if (tab == 0) sampleViewers.filter { it.coins > 0 } else sampleViewers
        LazyColumn(Modifier.heightIn(max = 460.dp)) {
            itemsIndexed(list) { i, v ->
                Row(Modifier.fillMaxWidth().clickable { onUser(v) }.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (tab == 0) {
                        Text(
                            "${i + 1}", style = MaterialTheme.typography.titleMedium,
                            color = when (i) { 0 -> Gold; 1 -> Color(0xFF9AA4B2); 2 -> Color(0xFFC98B5B); else -> InkTertiary },
                            modifier = Modifier.width(26.dp)
                        )
                    }
                    Avatar(avatar(v.img), 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(v.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            LevelBadge(v.level)
                            if (v.vip > 0) VipBadge(v.vip)
                            if (v.fan) FanBadge()
                        }
                    }
                    if (tab == 0) CoinAmount(v.coins)
                }
            }
        }
    }
}

/* ------------------------------ 12 · Mini profile ------------------------------ */

@Composable
private fun MiniProfileSheet(
    s: ViewerSheet.Mini, isHost: Boolean,
    onDismiss: () -> Unit, onProfile: () -> Unit, onGift: () -> Unit, onReport: () -> Unit, onMention: () -> Unit, onPrivate: () -> Unit,
) {
    var followed by remember { mutableStateOf(false) }
    IshqSheet(onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) {
                Text("Report", style = MaterialTheme.typography.labelMedium, color = InkTertiary, modifier = Modifier.clickable(onClick = onReport))
                Spacer(Modifier.weight(1f))
                Text("@ Mention", style = MaterialTheme.typography.labelMedium, color = InkTertiary, modifier = Modifier.clickable(onClick = onMention))
            }
            Avatar(avatar(s.img), 84.dp, ring = true)
            Spacer(Modifier.height(10.dp))
            NameRow(s.name, isHost, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { LevelBadge(s.level); if (s.level > 20) VipBadge(3) }
            Spacer(Modifier.height(4.dp))
            Text("🇮🇳 India · ID ${s.img * 1_000_337}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                Stat("${(s.img * 431).compact()}", "Followers", Modifier.weight(1f))
                Stat("${s.img * 7}", "Following", Modifier.weight(1f))
                Stat((s.level * 1_300).compact(), "Sent", Modifier.weight(1f))
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (followed) SmallOutlineButton("Following", { followed = false }, Modifier.weight(1f), height = 44.dp)
                else SmallGradientButton("Follow", { followed = true }, Modifier.weight(1f), icon = Icons.Rounded.Add, height = 44.dp)
                SmallOutlineButton("Gift", onGift, Modifier.weight(1f), icon = Icons.Rounded.CardGiftcard, height = 44.dp)
                SmallOutlineButton("Profile", onProfile, Modifier.weight(1f), height = 44.dp)
            }
            if (isHost) {
                Spacer(Modifier.height(10.dp))
                SmallOutlineButton("Request private show", onPrivate, Modifier.fillMaxWidth(), icon = Icons.Rounded.Lock, height = 44.dp, color = BrandPink)
            }
        }
    }
}

/* ------------------------------ 13 · Gift drawer ------------------------------ */

@Composable
private fun GiftDrawer(coins: Int, onDismiss: () -> Unit, onRecharge: () -> Unit, onSend: (Gift, Int) -> Unit) {
    var cat by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Gift?>(sampleGifts.first()) }
    var count by remember { mutableIntStateOf(1) }
    var combo by remember { mutableIntStateOf(0) }
    IshqSheet(onDismiss, dark = true) {
        UnderlineTabs(GiftCategory.entries.map { it.label }, cat, { cat = it }, dark = true)
        val gifts = sampleGifts.filter { it.category == GiftCategory.entries[cat] }
        LazyVerticalGrid(
            GridCells.Fixed(4), Modifier.height(230.dp).padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(gifts, key = { it.id }) { g ->
                val sel = selected?.id == g.id
                Column(
                    Modifier.padding(horizontal = 3.dp).clip(RoundedCornerShape(14.dp))
                        .then(if (sel) Modifier.background(Color.White.copy(alpha = 0.08f)).border(1.5.dp, BrandPink, RoundedCornerShape(14.dp)) else Modifier)
                        .clickable { selected = g; combo = 0 }.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        Text(g.emoji, fontSize = 36.sp)
                        if (g.fullScreen) Text("FX", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp), color = Color.White, modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp).clip(RoundedCornerShape(3.dp)).background(BrandOrchid).padding(horizontal = 3.dp))
                    }
                    Text(g.name, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
                    Spacer(Modifier.height(2.dp))
                    CoinAmount(g.coins, iconSize = 10.dp, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.clip(CircleShape).clickable(onClick = onRecharge).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                CoinIcon(18.dp)
                Text(" ${coins.grouped()}", style = MaterialTheme.typography.titleSmall, color = Color.White)
                Text("  Recharge ›", style = MaterialTheme.typography.labelMedium, color = Gold)
            }
            Spacer(Modifier.weight(1f))
            // Quantity selector
            Row(Modifier.clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)) {
                listOf(1, 10, 99).forEach { n ->
                    Text(
                        "×$n", style = MaterialTheme.typography.labelMedium, color = if (count == n) Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.clip(CircleShape).then(if (count == n) Modifier.background(Color.White.copy(alpha = 0.15f)) else Modifier).clickable { count = n }.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.height(38.dp).clip(CircleShape).background(if (selected != null) BrandBrush.primary else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
                    .clickable(enabled = selected != null) { selected?.let { combo++; onSend(it, count) } }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(if (combo > 0) "Combo ×$combo" else "Send", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp), color = Color.White)
            }
        }
    }
}

/* ------------------------------ 17 · Private show request ------------------------------ */

@Composable
private fun PrivateShowSheet(creator: LiveCreator, coins: Int, onDismiss: () -> Unit, onRequest: () -> Unit) {
    data class Option(val label: String, val sub: String, val coins: Int)
    val options = listOf(
        Option("Per minute", "Pay as you go · stop anytime", 40),
        Option("15 minutes", "Fixed block", 400),
        Option("30 minutes", "Fixed block · best value", 700),
    )
    var sel by remember { mutableIntStateOf(1) }
    IshqSheet(onDismiss, title = "Private show") {
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(creator.imageUrl, 48.dp, live = true)
            Spacer(Modifier.width(12.dp))
            Column {
                NameRow(creator.name, creator.verified)
                Text("1-to-1 video · only you and ${creator.name.substringBefore(' ')}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            }
        }
        Spacer(Modifier.height(14.dp))
        options.forEachIndexed { i, o ->
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(if (sel == i) PinkTint else Canvas)
                    .border(if (sel == i) 1.5.dp else 1.dp, if (sel == i) BrandPink else Hairline, RoundedCornerShape(16.dp))
                    .clickable { sel = i }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(sel == i, { sel = i }, colors = RadioButtonDefaults.colors(selectedColor = BrandPink))
                Column(Modifier.weight(1f)) {
                    Text(o.label, style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text(o.sub, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                CoinAmount(o.coins, style = MaterialTheme.typography.titleSmall)
                if (i == 0) Text("/min", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Coins are held when the creator accepts and refunded if the show ends early.", style = MaterialTheme.typography.bodySmall, color = InkTertiary, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(14.dp))
        Box(Modifier.padding(horizontal = 20.dp)) {
            PrimaryButton(if (options[sel].coins > coins) "Not enough coins" else "Send request", onClick = onRequest, enabled = options[sel].coins <= coins, leadingIcon = Icons.Rounded.Lock)
        }
    }
}

/* ------------------------------ 18 · Join Live Party ------------------------------ */

@Composable
private fun JoinPartySheet(creator: LiveCreator, requested: Boolean, onDismiss: () -> Unit, onToggle: () -> Unit) {
    var camera by remember { mutableStateOf(true) }
    IshqSheet(onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                Avatar(creator.imageUrl, 60.dp, ring = true)
                Avatar(Me.avatarUrl, 60.dp, ring = true)
            }
            Spacer(Modifier.height(12.dp))
            Text(if (requested) "Waiting for the host…" else "Join ${creator.name.substringBefore(' ')}'s party", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text("You'll appear on stage with up to 3 other guests.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(true to "Video", false to "Audio only").forEach { (v, label) ->
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(if (camera == v) PinkTint else SurfaceMuted)
                            .border(if (camera == v) 1.5.dp else 0.dp, if (camera == v) BrandPink else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable { camera = v }.padding(14.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (v) Icons.Rounded.Videocam else Icons.Rounded.Mic, null, tint = if (camera == v) BrandPink else InkSecondary)
                        Spacer(Modifier.width(6.dp))
                        Text(label, style = MaterialTheme.typography.titleSmall, color = Ink)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Queue: 2 people ahead of you", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
            Spacer(Modifier.height(14.dp))
            if (requested) SmallOutlineButton("Cancel request", onToggle, Modifier.fillMaxWidth(), height = 52.dp)
            else PrimaryButton("Request to join", onToggle, leadingIcon = Icons.Rounded.GroupAdd)
        }
    }
}

/* ------------------------------ 19 · Share ------------------------------ */

@Composable
fun ShareSheet(creator: LiveCreator, onDismiss: () -> Unit) {
    val toast = LocalToast.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    val link = "https://ishq.express/live/${creator.id}"
    IshqSheet(onDismiss, title = "Share live") {
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(sampleConversations.take(6)) { conv ->
                val c = creatorById(conv.creator)
                Column(Modifier.width(62.dp).clickable { toast("Sent to ${c.name.substringBefore(' ')}") }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Avatar(c.imageUrl, 54.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(c.name.substringBefore(' '), style = MaterialTheme.typography.bodySmall, color = Ink, maxLines = 1)
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Hairline)
        data class Target(val label: String, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)
        val targets = listOf(
            Target("WhatsApp", Color(0xFF25D366), Icons.Rounded.Chat),
            Target("Instagram", Color(0xFFE1306C), Icons.Rounded.CameraAlt),
            Target("Facebook", Color(0xFF1877F2), Icons.Rounded.Public),
            Target("Copy link", InkSecondary, Icons.Rounded.Link),
            Target("More", InkSecondary, Icons.Rounded.MoreHoriz),
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            targets.forEach { t ->
                Column(
                    Modifier.clickable {
                        if (t.label == "Copy link") { clipboard.setText(androidx.compose.ui.text.AnnotatedString(link)); toast("Link copied") } else toast("Opening ${t.label}…")
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.size(52.dp).clip(CircleShape).background(t.color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(t.icon, null, tint = t.color)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(t.label, style = MaterialTheme.typography.bodySmall, color = Ink)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/* ------------------------------ 20 · Report ------------------------------ */

@Composable
fun ReportSheet(target: String, onDismiss: () -> Unit, onSubmit: () -> Unit) {
    val reasons = listOf("Nudity or sexual content", "Violence or dangerous acts", "Hate speech or harassment", "Underage user", "Scam or fraud", "Spam", "Something else")
    var sel by remember { mutableIntStateOf(-1) }
    var block by remember { mutableStateOf(false) }
    IshqSheet(onDismiss, title = "Report $target") {
        Text("Your report is anonymous. If someone is in immediate danger, contact local emergency services.", style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(8.dp))
        reasons.forEachIndexed { i, r ->
            Row(Modifier.fillMaxWidth().clickable { sel = i }.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(sel == i, { sel = i }, colors = RadioButtonDefaults.colors(selectedColor = BrandPink))
                Text(r, style = MaterialTheme.typography.bodyLarge, color = Ink)
            }
        }
        Row(Modifier.fillMaxWidth().clickable { block = !block }.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(block, { block = it }, colors = CheckboxDefaults.colors(checkedColor = BrandPink))
            Text("Also block $target", style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.padding(horizontal = 20.dp)) { PrimaryButton("Submit report", onSubmit, enabled = sel >= 0) }
    }
}

/* ------------------------------ More menu ------------------------------ */

@Composable
private fun MoreSheet(creator: LiveCreator, onDismiss: () -> Unit, onShare: () -> Unit, onPrivate: () -> Unit, onReport: () -> Unit, onSubscribe: () -> Unit) {
    var clean by remember { mutableStateOf(false) }
    var translate by remember { mutableStateOf(true) }
    IshqSheet(onDismiss) {
        MenuRow(Icons.Rounded.Star, "Become a fan", subtitle = "From 499 coins / month", onClick = onSubscribe)
        MenuRow(Icons.Rounded.Lock, "Request private show", onClick = onPrivate)
        MenuRow(Icons.Rounded.Share, "Share", onClick = onShare)
        ToggleRow(Icons.Rounded.Translate, "Auto-translate chat", translate, { translate = it })
        ToggleRow(Icons.Rounded.VisibilityOff, "Clean screen", clean, { clean = it }, subtitle = "Hide chat and gift effects")
        MenuRow(Icons.Rounded.Flag, "Report", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = onReport)
    }
}
