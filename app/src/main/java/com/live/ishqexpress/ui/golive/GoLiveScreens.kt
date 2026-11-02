package com.live.ishqexpress.ui.golive

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
import androidx.compose.runtime.saveable.rememberSaveable
import com.live.ishqexpress.ui.live.ChatFeed
import com.live.ishqexpress.ui.live.GoalBar
import kotlinx.coroutines.delay

/*
 * 21 Go Live setup · 22 AR & beauty panel · 23 Broadcaster · 24 Premium settings · 25 Party host panel
 * 26 Battle invite · 27 Battle · 28 Battle result · 29 Dual broadcast · 30 Media share · 31 Goal setup
 * 32 Live summary · 33 Moderator assign
 */

private enum class LiveType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Video("Video", Icons.Rounded.Videocam), Audio("Audio", Icons.Rounded.Mic), Party("Party", Icons.Rounded.Groups)
}

/* ============================== 21 · Go Live setup ============================== */

@Composable
fun GoLiveSetupScreen(onBack: () -> Unit) {
    DarkSystemBars()
    val nav = LocalNav.current
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableIntStateOf(0) }
    var type by rememberSaveable { mutableIntStateOf(0) }
    var premium by rememberSaveable { mutableStateOf(false) }
    var fee by rememberSaveable { mutableIntStateOf(500) }
    var showBeauty by remember { mutableStateOf(false) }
    var showPremium by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(800)
            if (countdown == 1) nav.replace(Screen.Broadcaster, if (LiveType.entries[type] == LiveType.Audio) 1 else 0) else countdown--
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (LiveType.entries[type] == LiveType.Audio) {
            Box(Modifier.fillMaxSize().background(NightBrush), contentAlignment = Alignment.Center) {
                Avatar(Me.avatarUrl, 120.dp, ring = true)
            }
        } else VideoBackdrop(photo(77, 900, 1600), brandPair)

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassButton(Icons.Rounded.Close, "Close", onBack)
                Spacer(Modifier.weight(1f))
                GlassButton(Icons.Rounded.Cameraswitch, "Flip camera", {})
            }
            // Setup card
            Column(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.4f)).padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(72.dp).clip(RoundedCornerShape(14.dp))) {
                        AsyncImage(Me.avatarUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Text("Change cover", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color.White, textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.5f)).padding(vertical = 3.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (title.isEmpty()) Text("Add a title to get more viewers…", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.55f))
                        BasicTextField(
                            title, { if (it.length <= 60) title = it },
                            textStyle = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(sampleCategories) { i, c ->
                        Text(
                            "${c.emoji} ${c.label}", style = MaterialTheme.typography.labelMedium, color = Color.White,
                            modifier = Modifier.clip(CircleShape)
                                .then(if (i == category) Modifier.background(BrandBrush.primary) else Modifier.background(Color.White.copy(alpha = 0.15f)))
                                .clickable { category = i }.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)).clickable { showPremium = true }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Key, null, tint = Gold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Premium broadcast", style = MaterialTheme.typography.titleSmall, color = Color.White)
                        Text(if (premium) "Ticket: ${fee.grouped()} coins" else "Charge an entry ticket", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    }
                    IshqSwitch(premium) { premium = it; if (it) showPremium = true }
                }
            }

            Spacer(Modifier.weight(1f))

            // Tools
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                ToolButton(Icons.Rounded.FaceRetouchingNatural, "Beauty") { showBeauty = true }
                ToolButton(Icons.Rounded.AutoAwesome, "Filters") { showBeauty = true }
                ToolButton(Icons.Rounded.Flag, "Goal") {}
                ToolButton(Icons.Rounded.Share, "Share") {}
            }
            Spacer(Modifier.height(18.dp))
            Box(Modifier.padding(horizontal = 24.dp)) {
                PrimaryButton("Go Live", onClick = { countdown = 3 }, leadingIcon = Icons.Rounded.Videocam)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LiveType.entries.forEachIndexed { i, t ->
                    Row(
                        Modifier.clip(CircleShape).then(if (i == type) Modifier.background(Color.White) else Modifier).clickable { type = i }.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(t.icon, null, tint = if (i == type) Ink else Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(t.label, style = MaterialTheme.typography.labelMedium, color = if (i == type) Ink else Color.White)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (countdown > 0) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                AnimatedContent(countdown, transitionSpec = { (scaleIn(initialScale = 2f) + fadeIn()) togetherWith fadeOut() }, label = "count") { n ->
                    Text("$n", fontSize = 120.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }

    if (showBeauty) BeautyPanel { showBeauty = false }
    if (showPremium) PremiumSettingsSheet(premium, fee, onDismiss = { showPremium = false }, onSave = { on, f -> premium = on; fee = f; showPremium = false })
}

@Composable
private fun ToolButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, highlighted: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        GlassButton(icon, label, onClick, size = 44.dp, highlighted = highlighted)
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

/* ============================== 22 · AR filters & beauty ============================== */

@Composable
private fun BeautyPanel(onDismiss: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val sliders = remember { mutableStateMapOf("Smooth" to 0.6f, "Brighten" to 0.4f, "Slim face" to 0.2f, "Big eyes" to 0.1f) }
    var filter by remember { mutableIntStateOf(0) }
    IshqSheet(onDismiss, dark = true) {
        UnderlineTabs(listOf("Beauty", "Filters", "Masks", "Background"), tab, { tab = it }, dark = true)
        Spacer(Modifier.height(10.dp))
        when (tab) {
            0 -> Column(Modifier.padding(horizontal = 20.dp)) {
                sliders.keys.toList().forEach { k ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(k, style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.width(84.dp))
                        Slider(
                            sliders.getValue(k), { sliders[k] = it }, Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = BrandPink, inactiveTrackColor = Color.White.copy(alpha = 0.2f))
                        )
                        Text("${(sliders.getValue(k) * 100).toInt()}", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
                    }
                }
            }
            else -> {
                val items = when (tab) {
                    1 -> listOf("None" to "⭕", "Warm" to "🌅", "Cool" to "❄️", "Pink" to "🌸", "Vintage" to "📷", "B&W" to "⚫", "Glow" to "✨")
                    2 -> listOf("None" to "⭕", "Bunny" to "🐰", "Cat" to "🐱", "Crown" to "👑", "Hearts" to "💕", "Glasses" to "🕶️", "Devil" to "😈")
                    else -> listOf("None" to "⭕", "Blur" to "🌫️", "Studio" to "🎙️", "Beach" to "🏖️", "Neon" to "🌃", "Hearts" to "💗", "Space" to "🌌")
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(items) { i, (label, emoji) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { filter = i }) {
                            Box(
                                Modifier.size(62.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.08f))
                                    .then(if (filter == i) Modifier.border(2.dp, BrandPink, RoundedCornerShape(16.dp)) else Modifier),
                                contentAlignment = Alignment.Center
                            ) { Text(emoji, fontSize = 28.sp) }
                            Spacer(Modifier.height(4.dp))
                            Text(label, style = MaterialTheme.typography.labelSmall, color = if (filter == i) BrandPink else Color.White)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

/* ============================== 24 · Premium settings ============================== */

@Composable
private fun PremiumSettingsSheet(enabled: Boolean, fee: Int, onDismiss: () -> Unit, onSave: (Boolean, Int) -> Unit) {
    var on by remember { mutableStateOf(enabled) }
    var price by remember { mutableIntStateOf(fee) }
    var fansFree by remember { mutableStateOf(true) }
    IshqSheet(onDismiss, title = "Premium broadcast") {
        ToggleRow(Icons.Rounded.Key, "Charge an entry ticket", on, { on = it }, subtitle = "Viewers pay once to watch")
        AnimatedVisibility(on) {
            Column {
                Text("Entry fee", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(200, 500, 1_000, 2_000).forEach { p ->
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(if (price == p) PinkTint else SurfaceMuted)
                                .border(if (price == p) 1.5.dp else 0.dp, if (price == p) BrandPink else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { price = p }.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CoinIcon(18.dp)
                            Spacer(Modifier.height(4.dp))
                            Text(p.grouped(), style = MaterialTheme.typography.titleSmall, color = Ink)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("You receive about ${(price / 2).grouped()} diamonds per viewer.", style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.padding(horizontal = 20.dp))
                ToggleRow(Icons.Rounded.Star, "Super Fans enter free", fansFree, { fansFree = it })
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.padding(horizontal = 20.dp)) { PrimaryButton("Save", { onSave(on, price) }) }
    }
}

/* ============================== 23 · Broadcaster ============================== */

private enum class HostSheet { Party, Battle, Goal, Media, Moderators, Premium, Beauty, End }

@Composable
fun BroadcasterScreen(audioOnly: Boolean) {
    DarkSystemBars()
    val nav = LocalNav.current
    val toast = LocalToast.current
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    var viewers by remember { mutableIntStateOf(0) }
    var diamonds by remember { mutableIntStateOf(0) }
    var mic by remember { mutableStateOf(true) }
    var sheet by remember { mutableStateOf<HostSheet?>(null) }
    var partyOn by remember { mutableStateOf(false) }
    var goal by remember { mutableStateOf<Pair<String, Int>?>("Diwali song request 🪔" to 5_000) }
    var goalProgress by remember { mutableIntStateOf(1_200) }
    var media by remember { mutableStateOf<String?>(null) }
    var premium by remember { mutableStateOf(false) }
    val comments = remember { mutableStateListOf<LiveComment>().apply { add(sampleLiveComments.first()) } }
    val giftFeed = remember { mutableStateListOf<Pair<String, String>>() }

    BackHandlerCompat { sheet = HostSheet.End }

    LaunchedEffect(Unit) {
        var i = 0
        while (true) {
            delay(1000)
            seconds++
            viewers += (0..6).random()
            if (seconds % 3 == 0) {
                val v = sampleViewers[i % sampleViewers.size]
                val c = sampleLiveComments[1 + i % (sampleLiveComments.size - 1)]
                comments.add(c.copy(user = v.name, level = v.level))
                if (c.gift != null) {
                    val gained = 120 + i * 7
                    diamonds += gained; goalProgress += gained * 2
                    giftFeed.add(0, v.name to c.gift); if (giftFeed.size > 3) giftFeed.removeAt(3)
                }
                i++
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        when {
            partyOn -> PartyGrid()
            audioOnly -> Box(Modifier.fillMaxSize().background(NightBrush), contentAlignment = Alignment.Center) { AudioPulse(Me.avatarUrl, mic) }
            else -> VideoBackdrop(photo(77, 900, 1600), brandPair)
        }

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.32f)).padding(start = 3.dp, end = 12.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(Me.avatarUrl, 34.dp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiveDot(6)
                            Text(" ${formatClock(seconds)}", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            if (premium) Text("  🔑", fontSize = 10.sp)
                        }
                        CoinAmount(diamonds, diamond = true, iconSize = 10.dp, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.width(8.dp))
                GlassPill {
                    Icon(Icons.Rounded.Visibility, null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Text(" ${viewers.compact()}", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
                Spacer(Modifier.weight(1f))
                GlassButton(Icons.Rounded.PowerSettingsNew, "End live", { sheet = HostSheet.End }, size = 36.dp, tint = Color(0xFFFF6B8A))
            }
            goal?.let { (t, target) -> GoalBar(goalProgress.coerceAtMost(target), target, Modifier.padding(horizontal = 12.dp).width(210.dp), title = "Goal: $t") }

            media?.let { m ->
                Spacer(Modifier.height(10.dp))
                Box(Modifier.padding(start = 12.dp).size(width = 190.dp, height = 108.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black)) {
                    AsyncImage(photo(m.length, 400, 225), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Icon(Icons.Rounded.PlayCircle, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(36.dp))
                    Text(m, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(alpha = 0.5f)).padding(5.dp))
                    Icon(Icons.Rounded.Close, "Stop sharing", tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).clickable { media = null }.padding(3.dp))
                }
            }

            Spacer(Modifier.weight(1f))

            // Gift feed
            Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                giftFeed.forEach { (who, g) ->
                    Row(Modifier.clip(CircleShape).background(Brush.horizontalGradient(listOf(BrandPink.copy(alpha = 0.85f), Color.Transparent))).padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("$who sent ", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        Text(g, fontSize = 20.sp)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            ChatFeed(comments, Modifier.padding(start = 12.dp, end = 80.dp).height(200.dp))
            Spacer(Modifier.height(8.dp))

            // Host toolbar
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                item { ToolButton(if (mic) Icons.Rounded.Mic else Icons.Rounded.MicOff, if (mic) "Mute" else "Unmute") { mic = !mic } }
                if (!audioOnly) item { ToolButton(Icons.Rounded.FaceRetouchingNatural, "Beauty") { sheet = HostSheet.Beauty } }
                item { ToolButton(Icons.Rounded.Groups, "Party", highlighted = partyOn) { sheet = HostSheet.Party } }
                item { ToolButton(Icons.Rounded.SportsMma, "Battle") { sheet = HostSheet.Battle } }
                item { ToolButton(Icons.Rounded.Flag, "Goal") { sheet = HostSheet.Goal } }
                item { ToolButton(Icons.Rounded.QueueMusic, "Media") { sheet = HostSheet.Media } }
                item { ToolButton(Icons.Rounded.Shield, "Mods") { sheet = HostSheet.Moderators } }
                item { ToolButton(Icons.Rounded.Key, "Premium", highlighted = premium) { sheet = HostSheet.Premium } }
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    when (sheet) {
        null -> {}
        HostSheet.Beauty -> BeautyPanel { sheet = null }
        HostSheet.Party -> PartyHostPanel(partyOn, onDismiss = { sheet = null }, onToggle = { partyOn = it })
        HostSheet.Battle -> BattleInviteSheet(onDismiss = { sheet = null }, onStart = { id, dual -> sheet = null; nav.push(if (dual) Screen.DualBroadcast else Screen.Battle, id) })
        HostSheet.Goal -> GoalSetupSheet(goal, onDismiss = { sheet = null }, onSave = { goal = it; goalProgress = 0; sheet = null; toast(if (it == null) "Goal removed" else "Goal is live") })
        HostSheet.Media -> MediaShareSheet(onDismiss = { sheet = null }, onPick = { media = it; sheet = null })
        HostSheet.Moderators -> ModeratorSheet { sheet = null }
        HostSheet.Premium -> PremiumSettingsSheet(premium, 500, onDismiss = { sheet = null }, onSave = { on, _ -> premium = on; sheet = null })
        HostSheet.End -> IshqSheet({ sheet = null }) {
            Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("End your live?", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("${viewers.compact()} people are watching right now.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                Spacer(Modifier.height(18.dp))
                PrimaryButton("End live", { sheet = null; nav.replace(Screen.LiveSummary) })
                SmallOutlineButton("Keep streaming", { sheet = null }, Modifier.fillMaxWidth().padding(top = 8.dp), height = 52.dp)
            }
        }
    }
}

@Composable
private fun BackHandlerCompat(onBack: () -> Unit) = androidx.activity.compose.BackHandler(onBack = onBack)

fun formatClock(sec: Int): String = if (sec >= 3600) "%d:%02d:%02d".format(sec / 3600, sec / 60 % 60, sec % 60) else "%02d:%02d".format(sec / 60, sec % 60)

@Composable
fun AudioPulse(url: String, speaking: Boolean, size: Dp = 120.dp) {
    val t = rememberInfiniteTransition(label = "pulse")
    val s by t.animateFloat(1f, 1.35f, infiniteRepeatable(tween(1100), RepeatMode.Restart), label = "s")
    val a by t.animateFloat(0.5f, 0f, infiniteRepeatable(tween(1100), RepeatMode.Restart), label = "a")
    Box(contentAlignment = Alignment.Center) {
        if (speaking) Box(Modifier.size(size).graphicsLayer { scaleX = s; scaleY = s; alpha = a }.clip(CircleShape).background(BrandPink))
        Avatar(url, size, ring = true)
    }
}

/* ============================== 25 · Live Party host panel ============================== */

@Composable
private fun PartyGrid() {
    val guests = listOf(null, 3, 9, 10)
    Column(Modifier.fillMaxSize().background(Color(0xFF120818)).statusBarsPadding().padding(top = 96.dp, bottom = 300.dp).padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        guests.chunked(2).forEach { row ->
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { id ->
                    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(if (id == null) photo(77, 400, 500) else creatorById(id).imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Text(
                            if (id == null) "You (host)" else creatorById(id).name.substringBefore(' '),
                            style = MaterialTheme.typography.labelSmall, color = Color.White,
                            modifier = Modifier.align(Alignment.BottomStart).padding(6.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.4f)).padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartyHostPanel(on: Boolean, onDismiss: () -> Unit, onToggle: (Boolean) -> Unit) {
    val toast = LocalToast.current
    val requests = remember { mutableStateListOf(2, 11, 8) }
    val guests = remember { mutableStateListOf(3, 9, 10) }
    val muted = remember { mutableStateListOf(9) }
    IshqSheet(onDismiss, title = "Live Party") {
        ToggleRow(Icons.Rounded.Groups, "Party mode", on, onToggle, subtitle = "Up to 4 people on stage")
        SectionLabel("On stage (${guests.size}/3)", action = "Invite", onAction = { toast("Invite link copied") })
        guests.forEach { id ->
            val c = creatorById(id)
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(c.imageUrl, 42.dp)
                Spacer(Modifier.width(12.dp))
                Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                TopAction(if (id in muted) Icons.Rounded.MicOff else Icons.Rounded.Mic, "Mute", tint = if (id in muted) LiveRed else InkSecondary) {
                    if (id in muted) muted.remove(id) else muted.add(id)
                }
                TopAction(Icons.Rounded.PersonRemove, "Remove", tint = InkSecondary) { guests.remove(id) }
            }
        }
        SectionLabel("Requests (${requests.size})")
        if (requests.isEmpty()) Text("No pending requests", style = MaterialTheme.typography.bodySmall, color = InkTertiary, modifier = Modifier.padding(horizontal = 20.dp))
        requests.toList().forEach { id ->
            val c = creatorById(id)
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(c.imageUrl, 42.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                    LevelBadge(10 + id)
                }
                SmallOutlineButton("Decline", { requests.remove(id) }, height = 32.dp)
                Spacer(Modifier.width(6.dp))
                SmallGradientButton("Accept", {
                    requests.remove(id)
                    if (guests.size < 3) guests.add(id) else toast("Stage is full")
                    onToggle(true)
                }, height = 32.dp)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/* ============================== 26 · Battle invite ============================== */

@Composable
private fun BattleInviteSheet(onDismiss: () -> Unit, onStart: (Int, Boolean) -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(5) }
    var dual by remember { mutableStateOf(false) }
    var invited by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(invited) { invited?.let { delay(1500); onStart(it, dual) } }
    IshqSheet(onDismiss, title = if (dual) "Dual broadcast" else "PK Battle") {
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillChip("⚔️ Battle", !dual, { dual = false })
            PillChip("👯 Dual live", dual, { dual = true })
        }
        if (!dual) {
            Text("Duration", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 8.dp))
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 5, 10).forEach { m -> PillChip("$m min", duration == m, { duration = m }) }
            }
        }
        Spacer(Modifier.height(10.dp))
        UnderlineTabs(listOf("Friends live", "Random match"), tab, { tab = it })
        if (tab == 0) {
            LazyColumn(Modifier.heightIn(max = 320.dp)) {
                items(sampleCreators.filter { it.verified }) { c ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(c.imageUrl, 46.dp, live = true)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            NameRow(c.name, c.verified)
                            Text("${c.viewers.compact()} watching · Win rate ${50 + c.id * 3}%", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        if (invited == c.id) Text("Waiting…", style = MaterialTheme.typography.labelMedium, color = BrandPink)
                        else SmallGradientButton("Invite", { invited = c.id }, height = 32.dp)
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎲", fontSize = 48.sp)
                Text("We'll pair you with a creator at a similar level.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                if (invited != null) { CircularProgressIndicator(color = BrandPink); Text("Finding opponent…", style = MaterialTheme.typography.labelMedium, color = InkSecondary, modifier = Modifier.padding(top = 8.dp)) }
                else PrimaryButton("Start matching", { invited = sampleCreators.random().id })
            }
        }
    }
}

/* ============================== 27 · Battle + 28 · Result ============================== */

@Composable
fun BattleScreen(opponent: Int) {
    DarkSystemBars()
    val nav = LocalNav.current
    val other = creatorById(opponent)
    var left by rememberSaveable { mutableIntStateOf(300) }
    var mine by remember { mutableIntStateOf(0) }
    var theirs by remember { mutableIntStateOf(0) }
    var over by remember { mutableStateOf(false) }
    val comments = remember { mutableStateListOf<LiveComment>().apply { add(LiveComment("ishqexpress", 0, "PK Battle started! Gifts count as points ⚔️", system = true)) } }

    LaunchedEffect(over) {
        while (!over && left > 0) {
            delay(1000)
            left--
            mine += (0..140).random(); theirs += (0..120).random()
            if (left % 4 == 0) comments.add(sampleLiveComments[(left / 4) % sampleLiveComments.size].let { if (it.system) it.copy(system = false, user = "Karan", level = 27) else it })
        }
        if (left == 0) over = true
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF120818))) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("⚔️ PK Battle", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(Modifier.weight(1f))
                GlassPill { Icon(Icons.Rounded.Timer, null, tint = Gold, modifier = Modifier.size(14.dp)); Text(" ${formatClock(left)}", style = MaterialTheme.typography.labelLarge, color = Color.White) }
                Spacer(Modifier.width(8.dp))
                GlassButton(Icons.Rounded.Close, "End battle", { over = true }, size = 34.dp)
            }
            // Split screens
            Box(Modifier.fillMaxWidth().aspectRatio(1.25f)) {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        AsyncImage(photo(77, 400, 600), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Text("You", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
                    }
                    Spacer(Modifier.width(2.dp))
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        AsyncImage(other.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Text(other.name.substringBefore(' '), style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp))
                    }
                }
                Text("VS", fontSize = 34.sp, fontWeight = FontWeight.Black, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Gold, modifier = Modifier.align(Alignment.Center))
            }
            ScoreBar(mine, theirs)
            // MVP rows
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) { sampleViewers.take(3).forEach { Avatar(avatar(it.img), 28.dp) } }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)) { sampleViewers.drop(3).take(3).forEach { Avatar(avatar(it.img), 28.dp) } }
            }
            Spacer(Modifier.weight(1f))
            ChatFeed(comments, Modifier.padding(horizontal = 12.dp).height(180.dp))
            Spacer(Modifier.height(12.dp))
        }

        if (over) BattleResult(mine, theirs, other, onRematch = { left = 300; mine = 0; theirs = 0; over = false }, onBack = { nav.pop() })
    }
}

@Composable
private fun ScoreBar(mine: Int, theirs: Int) {
    val total = (mine + theirs).coerceAtLeast(1)
    val f by animateFloatAsState((mine / total.toFloat()).coerceIn(0.08f, 0.92f), label = "score")
    Box(Modifier.fillMaxWidth().height(26.dp)) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(f).fillMaxHeight().background(BrandBrush.primary), contentAlignment = Alignment.CenterStart) {
                Text("  ${mine.grouped()}", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
            Box(Modifier.weight(1f - f).fillMaxHeight().background(Brush.horizontalGradient(listOf(BattleBlue, Color(0xFF6EC6FF)))), contentAlignment = Alignment.CenterEnd) {
                Text("${theirs.grouped()}  ", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
    }
}

@Composable
private fun BattleResult(mine: Int, theirs: Int, other: LiveCreator, onRematch: () -> Unit, onBack: () -> Unit) {
    val won = mine >= theirs
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(28.dp).clip(RoundedCornerShape(28.dp)).background(Canvas).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (won) "🏆" else "💪", fontSize = 64.sp)
            Text(if (won) "You won!" else "${other.name.substringBefore(' ')} won", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Text("${mine.grouped()} vs ${theirs.grouped()} points", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
            Spacer(Modifier.height(18.dp))
            Text("MVP gifters", style = MaterialTheme.typography.titleSmall, color = Ink)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                sampleViewers.take(3).forEachIndexed { i, v ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            Avatar(avatar(v.img), if (i == 0) 64.dp else 52.dp, ring = i == 0)
                            Text(listOf("🥇", "🥈", "🥉")[i], fontSize = 18.sp, modifier = Modifier.align(Alignment.BottomEnd))
                        }
                        Text(v.name, style = MaterialTheme.typography.labelMedium, color = Ink)
                        CoinAmount(v.coins / 4, iconSize = 10.dp, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            PrimaryButton("Rematch", onRematch, leadingIcon = Icons.Rounded.Replay)
            SmallOutlineButton("Back to my live", onBack, Modifier.fillMaxWidth().padding(top = 8.dp), height = 50.dp)
        }
    }
}

/* ============================== 29 · Dual broadcast ============================== */

@Composable
fun DualBroadcastScreen(partner: Int) {
    DarkSystemBars()
    val nav = LocalNav.current
    val other = creatorById(partner)
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); seconds++ } }
    val comments = remember { mutableStateListOf(*sampleLiveComments.toTypedArray()) }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxHeight()) { VideoBackdrop(photo(77, 500, 1000), brandPair) }
            Box(Modifier.width(2.dp).fillMaxHeight().background(BrandBrush.deep))
            Box(Modifier.weight(1f).fillMaxHeight()) { VideoBackdrop(other.imageUrl, other.tint) }
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassPill { LiveDot(6); Text(" Dual live · ${formatClock(seconds)}", style = MaterialTheme.typography.labelMedium, color = Color.White) }
                Spacer(Modifier.weight(1f))
                GlassButton(Icons.Rounded.CallEnd, "Leave", { nav.pop() }, size = 36.dp, tint = Color(0xFFFF6B8A))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                GlassPill { Avatar(Me.avatarUrl, 22.dp); Text(" You", style = MaterialTheme.typography.labelMedium, color = Color.White) }
                Spacer(Modifier.weight(1f))
                GlassPill { Avatar(other.imageUrl, 22.dp); Text(" ${other.name.substringBefore(' ')}", style = MaterialTheme.typography.labelMedium, color = Color.White) }
            }
            Spacer(Modifier.weight(1f))
            Text("Viewers from both lives see this split screen", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(start = 12.dp, bottom = 6.dp))
            ChatFeed(comments, Modifier.padding(start = 12.dp, end = 60.dp).height(200.dp))
            Spacer(Modifier.height(16.dp))
        }
    }
}

/* ============================== 30 · Media share ============================== */

@Composable
private fun MediaShareSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val tracks = listOf("Kesariya — Brahmastra" to "4:28", "Tum Hi Ho — Aashiqui 2" to "4:22", "Chaleya — Jawan" to "3:20", "Apna Bana Le — Bhediya" to "4:21", "Pasoori — Coke Studio" to "3:44")
    var query by remember { mutableStateOf("") }
    IshqSheet(onDismiss, title = "Share media") {
        OutlinedTextField(
            query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text("Search music videos") }, leadingIcon = { Icon(Icons.Rounded.Search, null) },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline)
        )
        Spacer(Modifier.height(8.dp))
        tracks.filter { it.first.contains(query, ignoreCase = true) }.forEachIndexed { i, (t, len) ->
            Row(Modifier.fillMaxWidth().clickable { onPick(t) }.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(width = 80.dp, height = 46.dp).clip(RoundedCornerShape(8.dp))) {
                    AsyncImage(photo(t.length, 160, 92), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Text(len, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 3.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(t, style = MaterialTheme.typography.titleSmall, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${(i + 2) * 31}M views", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                Icon(Icons.Rounded.PictureInPicture, "Share", tint = BrandPink)
            }
        }
        Text("Shared media plays picture-in-picture for all viewers.", style = MaterialTheme.typography.bodySmall, color = InkTertiary, modifier = Modifier.padding(20.dp))
    }
}

/* ============================== 31 · Goal setup ============================== */

@Composable
private fun GoalSetupSheet(current: Pair<String, Int>?, onDismiss: () -> Unit, onSave: (Pair<String, Int>?) -> Unit) {
    var title by remember { mutableStateOf(current?.first ?: "") }
    var target by remember { mutableFloatStateOf((current?.second ?: 5_000).toFloat()) }
    var reward by remember { mutableStateOf("I'll sing your request") }
    IshqSheet(onDismiss, title = "Stream goal") {
        Column(Modifier.padding(horizontal = 20.dp)) {
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("What's the goal?") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline, focusedLabelColor = BrandPink))
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Target", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                CoinAmount((target / 100).toInt() * 100, style = MaterialTheme.typography.titleMedium)
            }
            Slider(target, { target = it }, valueRange = 500f..50_000f, colors = SliderDefaults.colors(thumbColor = BrandPink, activeTrackColor = BrandPink, inactiveTrackColor = Hairline))
            OutlinedTextField(reward, { reward = it }, Modifier.fillMaxWidth(), label = { Text("Reward for viewers") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline, focusedLabelColor = BrandPink))
            Spacer(Modifier.height(18.dp))
            PrimaryButton("Start goal", { onSave(title.ifBlank { "Gift goal" } to (target / 100).toInt() * 100) })
            if (current != null) SmallOutlineButton("Remove goal", { onSave(null) }, Modifier.fillMaxWidth().padding(top = 8.dp), height = 50.dp, color = LiveRed)
        }
    }
}

/* ============================== 33 · Moderators ============================== */

@Composable
private fun ModeratorSheet(onDismiss: () -> Unit) {
    val mods = remember { mutableStateListOf("Amit K") }
    IshqSheet(onDismiss, title = "Moderators") {
        Text("Moderators can mute, kick and pin messages in your lives.", style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.heightIn(max = 420.dp)) {
            items(sampleViewers) { v ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(avatar(v.img), 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(v.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                            if (v.name in mods) { Spacer(Modifier.width(4.dp)); Icon(Icons.Rounded.Shield, null, tint = BrandViolet, modifier = Modifier.size(14.dp)) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { LevelBadge(v.level); if (v.fan) FanBadge() }
                    }
                    if (v.name in mods) SmallOutlineButton("Remove", { mods.remove(v.name) }, height = 32.dp, color = LiveRed)
                    else SmallGradientButton("Make mod", { mods.add(v.name) }, height = 32.dp)
                }
            }
        }
    }
}

/* ============================== 32 · Live summary ============================== */

@Composable
fun LiveSummaryScreen() {
    val nav = LocalNav.current
    val toast = LocalToast.current
    Column(Modifier.fillMaxSize().background(Canvas).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().background(BrandBrush.deep).statusBarsPadding().padding(24.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(Me.avatarUrl, 84.dp, ring = true)
                Spacer(Modifier.height(10.dp))
                Text("Live ended", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Text("Great show, ${Me.name}! Here's how it went.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
            }
        }
        Spacer(Modifier.height(16.dp))
        val stats = listOf("1:12:48" to "Duration", "3,284" to "Viewers", "412" to "Peak", "18" to "New fans", "246" to "New followers", "7.4K" to "Likes")
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientCard(Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFFFFF2F8), Color(0xFFF3ECFF)))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DiamondIcon(44.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Diamonds earned", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                        Text("12,640", style = MaterialTheme.typography.headlineMedium, color = Ink)
                        Text("+38% vs your last live", style = MaterialTheme.typography.labelMedium, color = Success)
                    }
                }
            }
            stats.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (v, l) -> IshqCard(Modifier.weight(1f), padding = 12.dp) { Stat(v, l, Modifier.fillMaxWidth()) } }
                }
            }
        }
        SectionLabel("Top gifters")
        sampleViewers.take(5).forEachIndexed { i, v ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${i + 1}", style = MaterialTheme.typography.titleMedium, color = if (i < 3) Gold else InkTertiary, modifier = Modifier.width(24.dp))
                Avatar(avatar(v.img), 40.dp)
                Spacer(Modifier.width(12.dp))
                Text(v.name, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                CoinAmount(v.coins / 3, diamond = true)
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.padding(horizontal = 20.dp).navigationBarsPadding()) {
            PrimaryButton("Done", { nav.popToRoot() })
            SmallOutlineButton("Share highlights", { toast("Highlights link copied") }, Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), height = 50.dp, icon = Icons.Rounded.Share)
        }
    }
}
