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

import androidx.compose.ui.geometry.Offset
import com.live.ishqexpress.ui.gifts.GiftDrawer
import com.live.ishqexpress.ui.gifts.GiftEffectsLayer
import com.live.ishqexpress.ui.gifts.GiftEffectsState
import com.live.ishqexpress.ui.gifts.GiftEvent

/*
 * 10 Swipe live viewer — every room type in one vertical pager, like Tango:
 *    Solo live · PK battle · Dual (co-host) live · Video party · Chat party
 * 11 Viewer list · 12 Mini profile · 13 Gift drawer (ui/gifts) · 14 Gift effects (ui/gifts)
 * 15 Goal bar · 16 Premium entry · 19 Share · 20 Report
 */

data class MiniUser(val name: String, val img: Int, val level: Int)

private sealed interface ViewerSheet {
    data object Viewers : ViewerSheet
    data class Mini(val user: MiniUser) : ViewerSheet
    data class Gifts(val target: Int) : ViewerSheet
    data object Share : ViewerSheet
    data object Report : ViewerSheet
    data object More : ViewerSheet
}

@Composable
fun LiveViewerScreen(startCreator: Int, onClose: () -> Unit) {
    DarkSystemBars()
    val startIndex = sampleCreators.indexOfFirst { it.id == startCreator }.coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = startIndex) { sampleCreators.size }

    VerticalPager(pager, Modifier.fillMaxSize().background(Color.Black), beyondViewportPageCount = 1) { page ->
        val creator = sampleCreators[page]
        val active = pager.currentPage == page && !pager.isScrollInProgress
        when (creator.mode) {
            LiveMode.Party -> com.live.ishqexpress.ui.party.VideoPartyRoom(host = creator, isHost = false, active = active, onClose = onClose)
            LiveMode.Audio -> com.live.ishqexpress.ui.party.ChatPartyRoom(
                title = "${creator.name.substringBefore(' ')}'s party 🎶", host = creator.asTarget(), roomId = 1000 + creator.id,
                isHost = false, active = active, onClose = onClose
            )
            else -> LiveRoom(creator, active = active, onClose = onClose)
        }
    }
}

/** Opponent / co-host for PK and Dual rooms in the sample data. */
private fun partnerOf(c: LiveCreator): LiveCreator = when (c.id) {
    2 -> creatorById(9); 9 -> creatorById(2); 10 -> creatorById(4)
    else -> sampleCreators.first { it.id != c.id && it.mode == LiveMode.Solo }
}

@Composable
private fun LiveRoom(creator: LiveCreator, active: Boolean, onClose: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val effects = remember { GiftEffectsState() }
    var sheet by remember { mutableStateOf<ViewerSheet?>(null) }
    val comments = remember { mutableStateListOf<LiveComment>().apply { add(sampleLiveComments.first()) } }
    var goal by remember { mutableIntStateOf(3_420) }
    val goalTarget = 5_000
    // Creator 6 runs a ticketed (premium) broadcast in the sample data.
    var premiumLocked by remember { mutableStateOf(creator.id == 6) }
    var likes by remember { mutableIntStateOf(0) }

    val split = creator.mode == LiveMode.Pk || creator.mode == LiveMode.Dual
    val partner = remember(creator.id) { partnerOf(creator) }
    val targets = remember(creator.id) { if (split) listOf(creator.asTarget(), partner.asTarget()) else listOf(creator.asTarget()) }

    // PK scores (points = coins; PK Booster counts double)
    var scoreA by remember { mutableIntStateOf(18_400) }
    var scoreB by remember { mutableIntStateOf(16_950) }
    var pkLeft by remember { mutableIntStateOf(252) }
    LaunchedEffect(active, creator.mode) { if (creator.mode == LiveMode.Pk) while (active && pkLeft > 0) { delay(1000); pkLeft-- } }
    fun score(e: GiftEvent) {
        val pts = e.totalCoins * if (e.gift.pkBoost) 2 else 1
        if (e.receiver.id == creator.id) scoreA += pts else scoreB += pts
        if (e.receiver.id == creator.id) goal = (goal + e.totalCoins).coerceAtMost(goalTarget)
    }

    SimulateRoom(active && !premiumLocked, effects, targets, comments, seed = creator.id, onGift = ::score)

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (split) {
            SplitStage(creator, partner, pk = creator.mode == LiveMode.Pk, scoreA, scoreB, pkLeft,
                onSide = { i -> sheet = ViewerSheet.Gifts(i) })
        } else {
            VideoBackdrop(creator.imageUrl.replace("600", "900"), creator.tint)
        }

        GiftEffectsLayer(
            effects,
            faceAnchor = if (split) Offset(0.25f, 0.2f) else Offset(0.5f, 0.3f),
            bannerTop = if (split) 420.dp else 330.dp
        )

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            RoomHeader(
                creator.name, creator.imageUrl, creator.viewers,
                onHost = { sheet = ViewerSheet.Mini(MiniUser(creator.name, creator.photo, 38)) },
                onViewers = { sheet = ViewerSheet.Viewers },
                onClose = onClose
            )
            if (!split) {
                Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassPill(Modifier.clickable { nav.push(Screen.Leaderboards) }) { Text("🏆 Hourly #${creator.id + 2}", style = MaterialTheme.typography.labelSmall, color = Color.White) }
                    GlassPill { Text("ID ${80_000 + creator.id * 137}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f)) }
                }
                Spacer(Modifier.height(8.dp))
                GoalBar(goal, goalTarget, Modifier.padding(horizontal = 12.dp).width(210.dp))
            }
            Spacer(Modifier.weight(1f))
            ChatFeed(
                comments,
                Modifier.padding(start = 12.dp, end = 80.dp).height(if (split) 190.dp else 230.dp),
                onUser = { c -> if (!c.system) sheet = ViewerSheet.Mini(MiniUser(c.user, sampleViewers.firstOrNull { it.name == c.user }?.img ?: Me.photo, c.level)) }
            )
            Spacer(Modifier.height(8.dp))
            RoomBottomBar(
                onComment = { comments.add(LiveComment(Me.name, Me.level, it)) },
                onGift = { sheet = ViewerSheet.Gifts(0) },
            ) {
                GlassButton(Icons.Rounded.MoreHoriz, "More", { sheet = ViewerSheet.More })
                Spacer(Modifier.width(6.dp))
                GlassButton(Icons.Rounded.Favorite, "Like", { likes++ }, tint = Color(0xFFFF7AB6))
                Spacer(Modifier.width(6.dp))
            }
        }

        FloatingHearts(likes, Modifier.align(Alignment.BottomEnd).padding(end = 70.dp, bottom = 70.dp))

        if (premiumLocked) PremiumGate(creator, DemoWallet.coins, onClose = onClose, onPay = { fee ->
            if (fee > DemoWallet.coins) { toast("Not enough coins"); nav.push(Screen.CoinStore) }
            else { DemoWallet.coins -= fee; premiumLocked = false; toast("Welcome in! Enjoy the show") }
        }, onFan = { premiumLocked = false })
    }

    when (val s = sheet) {
        null -> {}
        ViewerSheet.Viewers -> ViewerListSheet(creator, onDismiss = { sheet = null }, onUser = { v -> sheet = ViewerSheet.Mini(MiniUser(v.name, v.img, v.level)) })
        is ViewerSheet.Mini -> MiniProfileSheet(
            s.user, isHost = s.user.name == creator.name,
            onDismiss = { sheet = null },
            onProfile = { sheet = null; nav.push(Screen.OtherProfile, creator.id) },
            onGift = { sheet = ViewerSheet.Gifts(0) },
            onReport = { sheet = ViewerSheet.Report },
            onMention = { comments.add(LiveComment(Me.name, Me.level, "@${s.user.name} 👋")); sheet = null },
        )
        is ViewerSheet.Gifts -> GiftDrawer(
            effects, targets,
            onDismiss = { sheet = null },
            onRecharge = { sheet = null; nav.push(Screen.CoinStore) },
            initialTarget = s.target,
            pkMode = creator.mode == LiveMode.Pk,
            onSent = { g, n, to ->
                to.forEach { t -> score(GiftEvent(g, n, Me.name, Me.avatarUrl, Me.level, t)) }
                comments.add(LiveComment(Me.name, Me.level, "sent ${g.name} ×$n", gift = g.emoji))
            }
        )
        ViewerSheet.Share -> ShareSheet(creator, onDismiss = { sheet = null })
        ViewerSheet.Report -> ReportSheet(creator.name, onDismiss = { sheet = null }, onSubmit = { sheet = null; toast("Thanks — our safety team will review this") })
        ViewerSheet.More -> MoreSheet(
            creator,
            onDismiss = { sheet = null },
            onShare = { sheet = ViewerSheet.Share },
            onReport = { sheet = ViewerSheet.Report },
            onSubscribe = { sheet = null; nav.push(Screen.OtherProfile, creator.id) },
        )
    }
}

/* ------------------------------ PK / Dual stage ------------------------------ */

@Composable
private fun SplitStage(a: LiveCreator, b: LiveCreator, pk: Boolean, scoreA: Int, scoreB: Int, secondsLeft: Int, onSide: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2A0B4A), Color(0xFF120818))))) {
        Spacer(Modifier.statusBarsPadding().height(96.dp))
        Box(Modifier.fillMaxWidth().aspectRatio(1.1f)) {
            Row(Modifier.fillMaxSize()) {
                listOf(a, b).forEachIndexed { i, c ->
                    Box(Modifier.weight(1f).fillMaxHeight().clickable { onSide(i) }) {
                        AsyncImage(c.imageUrl, c.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().background(Brush.linearGradient(c.tint)))
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.7f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.6f))))
                        Row(Modifier.align(if (i == 0) Alignment.BottomStart else Alignment.BottomEnd).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(c.imageUrl, 22.dp)
                            Text(" ${c.name.substringBefore(' ')}", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        }
                        if (pk) {
                            val winning = if (i == 0) scoreA >= scoreB else scoreB > scoreA
                            if (winning) Text("WIN", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.align(if (i == 0) Alignment.TopStart else Alignment.TopEnd).padding(8.dp).clip(RoundedCornerShape(5.dp)).background(Gold).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    if (i == 0) Box(Modifier.width(2.dp).fillMaxHeight().background(BrandBrush.deep))
                }
            }
            if (pk) {
                Column(Modifier.align(Alignment.TopCenter).padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PK", fontSize = 22.sp, fontWeight = FontWeight.Black, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Gold)
                    GlassPill { Text(if (secondsLeft > 0) com.live.ishqexpress.ui.golive.formatClock(secondsLeft) else "Result", style = MaterialTheme.typography.labelMedium, color = Color.White) }
                }
            } else {
                GlassPill(Modifier.align(Alignment.TopCenter).padding(top = 8.dp)) { Text("👯 Co-host live", style = MaterialTheme.typography.labelMedium, color = Color.White) }
            }
        }
        if (pk) {
            val total = (scoreA + scoreB).coerceAtLeast(1)
            val f by animateFloatAsState((scoreA / total.toFloat()).coerceIn(0.08f, 0.92f), label = "pk")
            Row(Modifier.fillMaxWidth().height(24.dp)) {
                Box(Modifier.weight(f).fillMaxHeight().background(BrandBrush.primary), contentAlignment = Alignment.CenterStart) {
                    Text("  ${scoreA.grouped()}", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
                Box(Modifier.weight(1f - f).fillMaxHeight().background(Brush.horizontalGradient(listOf(BattleBlue, Color(0xFF6EC6FF)))), contentAlignment = Alignment.CenterEnd) {
                    Text("${scoreB.grouped()}  ", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) { sampleViewers.take(3).forEach { Avatar(avatar(it.img), 26.dp) } }
                Text("Top gifters", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)) { sampleViewers.drop(3).take(3).forEach { Avatar(avatar(it.img), 26.dp) } }
            }
        }
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


/* ------------------------------ 16 · Premium entry ------------------------------ */

@Composable
private fun PremiumGate(creator: LiveCreator, coins: Int, onClose: () -> Unit, onPay: (Int) -> Unit, onFan: () -> Unit = {}) {
    val fee = 500
    var fanSheet by remember { mutableStateOf(false) }
    if (fanSheet) com.live.ishqexpress.ui.social.FanTierSheet(creator, onDismiss = { fanSheet = false }, onSubscribed = { onFan() })
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
                modifier = Modifier.clip(CircleShape).clickable { fanSheet = true }.padding(12.dp)
            )
        }
    }
}

/* ------------------------------ 11 · Viewer list ------------------------------ */

@Composable
fun ViewerListSheet(creator: LiveCreator, onDismiss: () -> Unit, onUser: (Viewer) -> Unit) {
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
fun MiniProfileSheet(
    s: MiniUser, isHost: Boolean,
    onDismiss: () -> Unit, onProfile: () -> Unit, onGift: () -> Unit, onReport: () -> Unit, onMention: () -> Unit,
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
        }
    }
}




/* ------------------------------ 19 · Share ------------------------------ */

@Composable
fun ShareSheet(creator: LiveCreator, onDismiss: () -> Unit) {
    val toast = LocalToast.current
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val link = AppLinks.live(creator.id)
    val message = "${creator.name} is live on ishqexpress — join now! $link"
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
                        when (t.label) {
                            "Copy link" -> { copyText(ctx, link); toast("Link copied") }
                            "WhatsApp" -> shareTo(ctx, "com.whatsapp", message)
                            "Instagram" -> shareTo(ctx, "com.instagram.android", message)
                            "Facebook" -> shareTo(ctx, "com.facebook.katana", message)
                            else -> shareText(ctx, message)
                        }
                        onDismiss()
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
private fun MoreSheet(creator: LiveCreator, onDismiss: () -> Unit, onShare: () -> Unit, onReport: () -> Unit, onSubscribe: () -> Unit) {
    var clean by remember { mutableStateOf(false) }
    var translate by remember { mutableStateOf(true) }
    IshqSheet(onDismiss) {
        MenuRow(Icons.Rounded.Star, "Become a fan", subtitle = "From 499 coins / month", onClick = onSubscribe)
        MenuRow(Icons.Rounded.Share, "Share", onClick = onShare)
        ToggleRow(Icons.Rounded.Translate, "Auto-translate chat", translate, { translate = it })
        ToggleRow(Icons.Rounded.VisibilityOff, "Clean screen", clean, { clean = it }, subtitle = "Hide chat and gift effects")
        MenuRow(Icons.Rounded.Flag, "Report", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = onReport)
    }
}
