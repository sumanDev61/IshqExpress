package com.live.ishqexpress.ui.chat

import com.live.ishqexpress.ui.theme.appBackground
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
import com.live.ishqexpress.ui.golive.formatClock
import kotlinx.coroutines.delay

/*
 * 45 Chats inbox · 46 Chat thread · 47 Media Packs · 48 1-to-1 video call
 * 49 Group video call · 50 Incoming call · 51 Private show session
 */

/* ============================== 45 · Chats inbox ============================== */

@Composable
fun ChatsScreen() {
    val nav = LocalNav.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().appBackground().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Messages", style = MaterialTheme.typography.headlineSmall, color = Ink, modifier = Modifier.weight(1f))
            TopAction(Icons.Rounded.Groups, "Group call") { nav.push(Screen.GroupCall) }
            TopAction(Icons.Outlined.NotificationsNone, "Notifications") { nav.push(Screen.Notifications) }
            TopAction(Icons.Rounded.EditNote, "New message") { nav.push(Screen.Search) }
        }
        val chats = sampleConversations.filter { !it.request }
        val requests = sampleConversations.filter { it.request }
        UnderlineTabs(listOf("Chats", "Requests (${requests.size})"), tab, { tab = it })
        LazyColumn(Modifier.fillMaxSize()) {
            if (tab == 0) {
                item {
                    // Online now rail
                    LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(sampleConversations.filter { it.online }) { c ->
                            val cr = creatorById(c.creator)
                            Column(Modifier.width(60.dp).clickable { nav.push(Screen.ChatThread, c.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                                Avatar(cr.imageUrl, 56.dp, online = true)
                                Spacer(Modifier.height(4.dp))
                                Text(cr.name.substringBefore(' '), style = MaterialTheme.typography.bodySmall, color = Ink, maxLines = 1)
                            }
                        }
                    }
                }
                item {
                    MenuRow(Icons.Rounded.SupportAgent, "ishqexpress Team", subtitle = "Welcome! Tap for safety tips and support", iconTint = Color.White, iconBg = BrandPink, onClick = { nav.push(Screen.Help) })
                }
                items(chats, key = { it.id }) { c -> ConversationRow(c) { nav.push(Screen.ChatThread, c.id) } }
            } else {
                item {
                    Text("Message requests from people you don't follow. They won't know you've seen it until you reply.", style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.padding(20.dp))
                }
                items(requests, key = { it.id }) { c -> ConversationRow(c) { nav.push(Screen.ChatThread, c.id) } }
            }
        }
    }
}

@Composable
private fun ConversationRow(c: Conversation, onClick: () -> Unit) {
    val cr = creatorById(c.creator)
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(cr.imageUrl, 54.dp, online = c.online)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            NameRow(cr.name, cr.verified)
            Text(c.last, style = MaterialTheme.typography.bodyMedium, color = if (c.unread > 0) Ink else InkSecondary, fontWeight = if (c.unread > 0) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(c.time, style = MaterialTheme.typography.bodySmall, color = if (c.unread > 0) BrandPink else InkTertiary)
            Spacer(Modifier.height(4.dp))
            if (c.unread > 0) Text("${c.unread}", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.clip(CircleShape).background(BrandBrush.primary).padding(horizontal = 7.dp, vertical = 2.dp))
        }
    }
}

/* ============================== 46 · Chat thread ============================== */

@Composable
fun ChatThreadScreen(conversationId: Int, onBack: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val conv = sampleConversations.firstOrNull { it.id == conversationId } ?: sampleConversations.first()
    val cr = creatorById(conv.creator)
    val messages = remember { mutableStateListOf(*sampleMessages.toTypedArray()) }
    var draft by remember { mutableStateOf("") }
    var translate by remember { mutableStateOf(true) }
    var panel by remember { mutableIntStateOf(0) } // 0 none, 1 gifts, 2 attach
    var accepted by remember { mutableStateOf(!conv.request) }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { listState.animateScrollToItem(messages.size) }

    Column(Modifier.fillMaxSize().appBackground().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TopAction(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
            Row(Modifier.weight(1f).clickable { nav.push(Screen.OtherProfile, cr.id) }, verticalAlignment = Alignment.CenterVertically) {
                Avatar(cr.imageUrl, 40.dp, online = conv.online)
                Spacer(Modifier.width(10.dp))
                Column {
                    NameRow(cr.name, cr.verified)
                    Text(if (conv.online) "Online" else "Last seen recently", style = MaterialTheme.typography.bodySmall, color = if (conv.online) Success else InkTertiary)
                }
            }
            TopAction(Icons.Rounded.Videocam, "Video call") { nav.push(Screen.VideoCall, cr.id) }
            TopAction(if (translate) Icons.Rounded.Translate else Icons.Outlined.Translate, "Translate", tint = if (translate) BrandPink else Ink) {
                translate = !translate; toast(if (translate) "Auto-translate on" else "Auto-translate off")
            }
        }
        HorizontalDivider(color = Hairline)

        LazyColumn(Modifier.weight(1f).fillMaxWidth().background(SurfaceMuted.copy(alpha = 0.5f)), state = listState, contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                Text("Today", style = MaterialTheme.typography.labelSmall, color = InkTertiary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
            }
            items(messages) { m -> Bubble(m, translate) }
        }

        if (!accepted) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${cr.name.substringBefore(' ')} wants to message you", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallOutlineButton("Delete", onBack, Modifier.weight(1f), height = 44.dp, color = LiveRed)
                    SmallGradientButton("Accept", { accepted = true }, Modifier.weight(1f), height = 44.dp)
                }
            }
        } else {
            // Composer
            Row(Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                TopAction(Icons.Rounded.AddCircleOutline, "Attach", tint = if (panel == 2) BrandPink else InkSecondary) { panel = if (panel == 2) 0 else 2 }
                Row(Modifier.weight(1f).heightIn(min = 44.dp).clip(RoundedCornerShape(22.dp)).background(SurfaceMuted).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        if (draft.isEmpty()) Text("Message…", style = MaterialTheme.typography.bodyMedium, color = InkTertiary)
                        BasicTextField(draft, { draft = it }, textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink), cursorBrush = androidx.compose.ui.graphics.SolidColor(BrandPink), maxLines = 4, modifier = Modifier.fillMaxWidth())
                    }
                    Icon(Icons.Rounded.CardGiftcard, "Gifts", tint = if (panel == 1) BrandPink else InkSecondary, modifier = Modifier.size(22.dp).clickable { panel = if (panel == 1) 0 else 1 })
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(if (draft.isBlank()) Brush.linearGradient(listOf(SurfaceMuted, SurfaceMuted)) else BrandBrush.primary).clickable(enabled = draft.isNotBlank()) {
                        messages.add(Message(true, draft.trim(), "now")); draft = ""
                    },
                    contentAlignment = Alignment.Center
                ) { Icon(if (draft.isBlank()) Icons.Rounded.Mic else Icons.AutoMirrored.Rounded.Send, "Send", tint = if (draft.isBlank()) InkSecondary else Color.White) }
            }
            AnimatedVisibility(panel == 1) {
                LazyRow(contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.navigationBarsPadding()) {
                    items(sampleGifts.take(10)) { g ->
                        Column(Modifier.clip(RoundedCornerShape(12.dp)).background(SurfaceMuted).clickable { messages.add(Message(true, "", "now", gift = g.emoji)); panel = 0; toast("Sent ${g.name} · ${g.coins} coins") }.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(g.emoji, fontSize = 30.sp)
                            CoinAmount(g.coins, iconSize = 10.dp, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            AnimatedVisibility(panel == 2) {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AttachItem(Icons.Rounded.Image, "Photo", BrandPink) { messages.add(Message(true, "", "now", image = 300 + messages.size)); panel = 0 }
                    AttachItem(Icons.Rounded.CameraAlt, "Camera", BrandViolet) { panel = 0 }
                    AttachItem(Icons.Rounded.PhotoLibrary, "Media pack", BrandOrchid) { panel = 0; nav.push(Screen.MediaPacks) }
                    AttachItem(Icons.Rounded.Call, "Call", Success) { panel = 0; nav.push(Screen.VideoCall, cr.id) }
                }
            }
        }
    }
}

@Composable
private fun AttachItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(52.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Ink)
    }
}

@Composable
private fun Bubble(m: Message, translate: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.mine) Arrangement.End else Arrangement.Start) {
        val shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = if (m.mine) 18.dp else 4.dp, bottomEnd = if (m.mine) 4.dp else 18.dp)
        when {
            m.gift != null -> Column(Modifier.clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Color(0xFFFFE3F0), Color(0xFFEDE0FF)))).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(m.gift, fontSize = 44.sp)
                Text(if (m.mine) "You sent a gift" else "Sent you a gift", style = MaterialTheme.typography.labelSmall, color = BrandPinkDark)
            }
            m.image != null -> AsyncImage(photo(m.image, 400, 500), null, contentScale = ContentScale.Crop, modifier = Modifier.width(200.dp).aspectRatio(0.8f).clip(RoundedCornerShape(18.dp)))
            else -> Column(
                Modifier.widthIn(max = 280.dp).clip(shape).then(if (m.mine) Modifier.background(BrandBrush.primary) else Modifier.background(Canvas)).padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(m.text, style = MaterialTheme.typography.bodyMedium, color = if (m.mine) Color.White else Ink)
                if (translate && m.translated != null) {
                    HorizontalDivider(Modifier.padding(vertical = 6.dp), color = Hairline)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Translate, null, tint = InkTertiary, modifier = Modifier.size(12.dp))
                        Text(" ${m.translated}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                }
                Text(m.time, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = if (m.mine) Color.White.copy(alpha = 0.75f) else InkTertiary, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

/* ============================== 47 · Media Packs ============================== */

@Composable
fun MediaPacksScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    var creating by remember { mutableStateOf(false) }
    ScreenScaffold("Media Packs", onBack, actions = { TopAction(Icons.Rounded.Add, "New pack") { creating = true } }) {
        Text("Save albums once and send them in any chat — free or for coins.", style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        LazyVerticalGrid(GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sampleMediaPacks) { p ->
                Column(Modifier.clickable { toast("Pack \"${p.title}\" ready to send") }) {
                    Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                        // stacked look
                        Box(Modifier.matchParentSize().padding(start = 10.dp, end = 10.dp).offset(y = (-6).dp).clip(RoundedCornerShape(16.dp)).background(PinkTintStrong))
                        AsyncImage(photo(p.cover, 400, 400), null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(RoundedCornerShape(16.dp)))
                        Row(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                            GlassPill { Icon(Icons.Rounded.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(12.dp)); Text(" ${p.count}", style = MaterialTheme.typography.labelSmall, color = Color.White) }
                        }
                        if (p.price > 0) Row(Modifier.align(Alignment.TopEnd).padding(8.dp).clip(CircleShape).background(Canvas).padding(horizontal = 8.dp, vertical = 4.dp)) { CoinAmount(p.price, iconSize = 12.dp) }
                        else Text("FREE", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).clip(CircleShape).background(Success).padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(p.title, style = MaterialTheme.typography.titleSmall, color = Ink)
                }
            }
        }
    }
    if (creating) {
        var name by remember { mutableStateOf("") }
        var price by remember { mutableIntStateOf(0) }
        IshqSheet({ creating = false }, title = "New media pack") {
            Column(Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Pack name") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline, focusedLabelColor = BrandPink))
                Spacer(Modifier.height(12.dp))
                Text("Price to unlock", style = MaterialTheme.typography.titleSmall, color = Ink)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(0, 99, 199, 499).forEach { p -> PillChip(if (p == 0) "Free" else "$p coins", price == p, { price = p }) } }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(14.dp)).border(1.5.dp, Hairline, RoundedCornerShape(14.dp)), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AddPhotoAlternate, null, tint = BrandPink); Text("  Add photos & videos", style = MaterialTheme.typography.titleSmall, color = InkSecondary)
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Create pack", { creating = false; toast("Pack created") }, enabled = name.isNotBlank())
            }
        }
    }
}

/* ============================== 48 · 1-to-1 video call ============================== */

@Composable
fun VideoCallScreen(creatorId: Int) {
    CallSurface(creatorById(creatorId), title = null, perMinute = null)
}

/* ============================== 51 · Private show session ============================== */

@Composable
fun PrivateShowScreen(creatorId: Int) {
    CallSurface(creatorById(creatorId), title = "Private show", perMinute = 40)
}

@Composable
private fun CallSurface(c: LiveCreator, title: String?, perMinute: Int?) {
    DarkSystemBars()
    val nav = LocalNav.current
    val toast = LocalToast.current
    var connected by remember { mutableStateOf(false) }
    var seconds by remember { mutableIntStateOf(0) }
    var blockLeft by remember { mutableIntStateOf(15 * 60) }
    var mic by remember { mutableStateOf(true) }
    var cam by remember { mutableStateOf(true) }
    var translate by remember { mutableStateOf(false) }
    var filter by remember { mutableIntStateOf(0) }
    var showExtend by remember { mutableStateOf(false) }
    var swapped by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1800); connected = true
        while (true) { delay(1000); seconds++; if (perMinute != null) blockLeft-- }
    }
    LaunchedEffect(blockLeft) { if (perMinute != null && blockLeft == 60) showExtend = true }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        VideoBackdrop(if (swapped) photo(77, 900, 1600) else c.imageUrl.replace("600", "900"), c.tint, scrim = true)
        if (!connected) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).blur(20.dp))

        // Self preview
        Box(
            Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 70.dp, end = 14.dp)
                .size(width = 104.dp, height = 150.dp).shadow(8.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(Color.DarkGray)
                .clickable { swapped = !swapped }
        ) {
            if (cam) AsyncImage(if (swapped) c.imageUrl else photo(77, 300, 450), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.VideocamOff, null, tint = Color.White) }
            if (filter > 0) Text(listOf("", "🐰", "👑", "🕶️")[filter], fontSize = 30.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp))
        }

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassButton(Icons.Rounded.KeyboardArrowDown, "Minimise", { nav.pop() }, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        if (title != null) { Spacer(Modifier.width(6.dp)); Text("🔒 $title", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.clip(CircleShape).background(BrandBrush.primary).padding(horizontal = 8.dp, vertical = 2.dp)) }
                    }
                    Text(if (connected) formatClock(seconds) else "Connecting…", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                }
            }
            if (perMinute != null && connected) {
                Row(Modifier.padding(horizontal = 12.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black.copy(alpha = 0.4f)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Timer, null, tint = if (blockLeft < 120) LiveRed else Gold, modifier = Modifier.size(18.dp))
                    Text(" ${formatClock(blockLeft.coerceAtLeast(0))} left", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Spacer(Modifier.width(10.dp))
                    CoinAmount(perMinute * (seconds / 60 + 1), iconSize = 12.dp, color = Color.White.copy(alpha = 0.85f))
                    Text(" spent", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(10.dp))
                    Text("Extend", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.clip(CircleShape).background(BrandBrush.primary).clickable { showExtend = true }.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            if (translate && connected) {
                Spacer(Modifier.height(8.dp))
                Text("🌐 \"Aap kaise ho?\" → \"How are you?\"", style = MaterialTheme.typography.bodySmall, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.45f)).padding(10.dp))
            }
            Spacer(Modifier.weight(1f))
            // Filters row
            Row(Modifier.align(Alignment.CenterHorizontally).padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("⭕", "🐰", "👑", "🕶️").forEachIndexed { i, e ->
                    Box(Modifier.size(42.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)).then(if (filter == i) Modifier.border(2.dp, BrandPink, CircleShape) else Modifier).clickable { filter = i }, contentAlignment = Alignment.Center) { Text(e, fontSize = 20.sp) }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                GlassButton(if (mic) Icons.Rounded.Mic else Icons.Rounded.MicOff, "Mic", { mic = !mic }, size = 52.dp, highlighted = !mic)
                GlassButton(if (cam) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff, "Camera", { cam = !cam }, size = 52.dp, highlighted = !cam)
                Box(Modifier.size(66.dp).clip(CircleShape).background(LiveRed).clickable {
                    nav.pop(); toast(if (perMinute != null) "Show ended · unused coins refunded" else "Call ended · ${formatClock(seconds)}")
                }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CallEnd, "End", tint = Color.White, modifier = Modifier.size(30.dp)) }
                GlassButton(Icons.Rounded.Translate, "Translate", { translate = !translate }, size = 52.dp, highlighted = translate)
                GlassButton(Icons.Rounded.CardGiftcard, "Gift", { toast("Sent a Rose 🌹") }, size = 52.dp)
            }
        }
    }

    if (showExtend && perMinute != null) IshqSheet({ showExtend = false }, title = "Extend private show") {
        listOf(5 to 200, 15 to 550, 30 to 1_000).forEach { (min, coins) ->
            MenuRow(Icons.Rounded.MoreTime, "+$min minutes", trailing = { CoinAmount(coins, style = MaterialTheme.typography.titleSmall) }, onClick = {
                blockLeft += min * 60; showExtend = false; toast("Extended by $min minutes")
            })
        }
        Spacer(Modifier.height(8.dp))
    }
}

/* ============================== 49 · Group video call ============================== */

@Composable
fun GroupCallScreen() {
    DarkSystemBars()
    val nav = LocalNav.current
    val toast = LocalToast.current
    val people = remember { mutableStateListOf(1, 3, 4, 6, 9, 10, 12, 5) }
    var mic by remember { mutableStateOf(true) }
    var cam by remember { mutableStateOf(true) }
    var seconds by remember { mutableIntStateOf(0) }
    var speaking by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); seconds++; if (seconds % 2 == 0) speaking = (speaking + 1) % (people.size + 1) } }

    Column(Modifier.fillMaxSize().background(Color(0xFF120818)).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassButton(Icons.Rounded.KeyboardArrowDown, "Minimise", { nav.pop() }, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Diwali gang 🪔", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text("${people.size + 1}/9 · ${formatClock(seconds)}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            GlassButton(Icons.Rounded.PersonAdd, "Add people", { if (people.size < 8) people.add(2) else toast("Group calls hold up to 9 people") }, size = 36.dp)
        }
        val tiles = listOf<Int?>(null) + people
        val cols = if (tiles.size <= 4) 2 else 3
        LazyVerticalGrid(GridCells.Fixed(cols), Modifier.weight(1f).padding(horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(tiles) { i, id ->
                Box(
                    Modifier.fillMaxWidth().aspectRatio(if (cols == 2) 0.75f else 0.72f).clip(RoundedCornerShape(14.dp)).background(Color.DarkGray)
                        .then(if (i == speaking) Modifier.border(2.5.dp, BrandPink, RoundedCornerShape(14.dp)) else Modifier)
                ) {
                    if (id == null && !cam) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Avatar(Me.avatarUrl, 56.dp) }
                    else AsyncImage(if (id == null) photo(77, 300, 400) else creatorById(id).imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Row(Modifier.align(Alignment.BottomStart).padding(6.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        if ((id == null && !mic) || i == 3) { Icon(Icons.Rounded.MicOff, null, tint = LiveRed, modifier = Modifier.size(11.dp)); Spacer(Modifier.width(3.dp)) }
                        Text(if (id == null) "You" else creatorById(id).name.substringBefore(' '), style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            GlassButton(if (mic) Icons.Rounded.Mic else Icons.Rounded.MicOff, "Mic", { mic = !mic }, size = 52.dp, highlighted = !mic)
            GlassButton(if (cam) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff, "Camera", { cam = !cam }, size = 52.dp, highlighted = !cam)
            GlassButton(Icons.Rounded.Cameraswitch, "Flip", {}, size = 52.dp)
            Box(Modifier.size(62.dp).clip(CircleShape).background(LiveRed).clickable { nav.pop() }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CallEnd, "Leave", tint = Color.White, modifier = Modifier.size(28.dp)) }
        }
    }
}

/* ============================== 50 · Incoming call ============================== */

@Composable
fun IncomingCallScreen(creatorId: Int) {
    DarkSystemBars()
    val nav = LocalNav.current
    val c = creatorById(creatorId)
    val t = rememberInfiniteTransition(label = "ring")
    val ring by t.animateFloat(1f, 1.6f, infiniteRepeatable(tween(1400), RepeatMode.Restart), label = "r")
    val ringA by t.animateFloat(0.5f, 0f, infiniteRepeatable(tween(1400), RepeatMode.Restart), label = "ra")
    val bounce by t.animateFloat(0f, -10f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "b")

    Box(Modifier.fillMaxSize()) {
        AsyncImage(c.imageUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().blur(40.dp))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BrandNight.copy(alpha = 0.75f), Color.Black.copy(alpha = 0.85f)))))
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(60.dp))
            Text("ishqexpress video call", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(40.dp))
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(130.dp).graphicsLayer { scaleX = ring; scaleY = ring; alpha = ringA }.clip(CircleShape).background(BrandPink))
                Avatar(c.imageUrl, 130.dp, ring = true)
            }
            Spacer(Modifier.height(20.dp))
            NameRow(c.name, c.verified, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text("is calling you…", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.weight(1f))
            Text("Quick replies", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Can't talk now", "Call you later", "In a live 🎙️").forEach {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable { nav.pop() }.padding(horizontal = 12.dp, vertical = 8.dp))
                }
            }
            Spacer(Modifier.height(36.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(70.dp).clip(CircleShape).background(LiveRed).clickable { nav.pop() }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CallEnd, "Decline", tint = Color.White, modifier = Modifier.size(32.dp)) }
                    Spacer(Modifier.height(8.dp))
                    Text("Decline", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.offset(y = bounce.dp).size(70.dp).clip(CircleShape).background(Success).clickable { nav.replace(Screen.VideoCall, c.id) }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Videocam, "Accept", tint = Color.White, modifier = Modifier.size(32.dp)) }
                    Spacer(Modifier.height(8.dp))
                    Text("Accept", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}
