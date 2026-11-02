package com.live.ishqexpress.ui.audio

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
import com.live.ishqexpress.ui.golive.AudioPulse
import kotlinx.coroutines.delay

/* 34 Audio rooms list · 35 Audio room · 36 Create audio room */

@Composable
fun AudioRoomsScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    var filter by rememberSaveable { mutableStateOf("All") }
    var creating by remember { mutableStateOf(false) }
    val filters = listOf("All", "Talk", "Music", "Shayari", "Games")

    Box(Modifier.fillMaxSize()) {
        ScreenScaffold("Audio rooms", onBack, actions = { TopAction(Icons.Rounded.Search, "Search") { nav.push(Screen.Search) } }) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f -> PillChip(f, filter == f, { filter = f }) }
            }
            val rooms = sampleRooms.filter { filter == "All" || it.tag == filter }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (rooms.isEmpty()) item { EmptyState("🎙️", "No $filter rooms right now", "Start one and invite your friends.") }
                items(rooms, key = { it.id }) { r -> RoomCard(r) { nav.push(Screen.AudioRoom, r.id) } }
            }
        }
        Box(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp)
        ) { SmallGradientButton("Start a room", { creating = true }, icon = Icons.Rounded.Mic, height = 50.dp) }
    }
    if (creating) CreateRoomSheet(onDismiss = { creating = false }, onCreate = { creating = false; nav.push(Screen.AudioRoom, 0) })
}

@Composable
private fun RoomCard(r: AudioRoom, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(Color(0xFFFFF2F8), Color(0xFFF3ECFF)))).clickable(onClick = onClick).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(r.tag.uppercase(), style = MaterialTheme.typography.labelSmall, color = BrandOrchid)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Headphones, null, tint = InkSecondary, modifier = Modifier.size(14.dp))
                Text(" ${r.listeners.compact()}", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(r.title, style = MaterialTheme.typography.titleMedium, color = Ink)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                r.speakers.take(4).forEach { Avatar(creatorById(it).imageUrl, 36.dp, Modifier.border(2.dp, Canvas, CircleShape)) }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                r.speakers.take(2).joinToString(", ") { creatorById(it).name.substringBefore(' ') } + if (r.speakers.size > 2) " +${r.speakers.size - 2} speaking" else " speaking",
                style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            SoundBars()
        }
    }
}

@Composable
fun SoundBars(color: Color = BrandPink) {
    val t = rememberInfiniteTransition(label = "bars")
    Row(Modifier.height(16.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        repeat(4) { i ->
            val h by t.animateFloat(0.25f, 1f, infiniteRepeatable(tween(400 + i * 120), RepeatMode.Reverse), label = "b$i")
            Box(Modifier.width(3.dp).fillMaxHeight(h).clip(CircleShape).background(color))
        }
    }
}

/* ------------------------------ 36 · Create ------------------------------ */

@Composable
private fun CreateRoomSheet(onDismiss: () -> Unit, onCreate: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var privacy by remember { mutableIntStateOf(0) }
    var tag by remember { mutableStateOf("Talk") }
    IshqSheet(onDismiss, title = "Start an audio room") {
        Column(Modifier.padding(horizontal = 20.dp)) {
            OutlinedTextField(
                title, { if (it.length <= 50) title = it }, Modifier.fillMaxWidth(), label = { Text("Room title") },
                supportingText = { Text("${title.length}/50") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline, focusedLabelColor = BrandPink)
            )
            Spacer(Modifier.height(6.dp))
            Text("Topic", style = MaterialTheme.typography.titleSmall, color = Ink)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Talk", "Music", "Shayari", "Games").forEach { PillChip(it, tag == it, { tag = it }) } }
            Spacer(Modifier.height(16.dp))
            Text("Who can join", style = MaterialTheme.typography.titleSmall, color = Ink)
            Spacer(Modifier.height(8.dp))
            data class P(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val sub: String)
            listOf(P(Icons.Rounded.Public, "Open", "Anyone can listen"), P(Icons.Rounded.People, "Followers", "People who follow you"), P(Icons.Rounded.Lock, "Private", "Invite only")).forEachIndexed { i, p ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (privacy == i) PinkTint else Canvas)
                        .border(if (privacy == i) 1.5.dp else 1.dp, if (privacy == i) BrandPink else Hairline, RoundedCornerShape(14.dp))
                        .clickable { privacy = i }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(p.icon, null, tint = if (privacy == i) BrandPink else InkSecondary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.label, style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text(p.sub, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    if (privacy == i) Icon(Icons.Rounded.CheckCircle, null, tint = BrandPink)
                }
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Start room", onCreate, enabled = title.isNotBlank(), leadingIcon = Icons.Rounded.Mic)
        }
    }
}

/* ------------------------------ 35 · Audio room ------------------------------ */

@Composable
fun AudioRoomScreen(roomId: Int, onLeave: () -> Unit) {
    DarkSystemBars()
    val toast = LocalToast.current
    val room = sampleRooms.firstOrNull { it.id == roomId }
        ?: AudioRoom(0, "${Me.name}'s room", 0, emptyList(), 0, "Talk")
    val isHost = roomId == 0
    var handRaised by remember { mutableStateOf(false) }
    var onStage by remember { mutableStateOf(isHost) }
    var muted by remember { mutableStateOf(!isHost) }
    var speaking by remember { mutableIntStateOf(0) }
    var gifts by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { delay(1800); speaking = (speaking + 1) % 6 } }

    Column(Modifier.fillMaxSize().background(NightBrush).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassButton(Icons.Rounded.KeyboardArrowDown, "Minimise", onLeave, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(room.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${room.tag} · ${room.listeners.compact()} listening", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f))
            }
            GlassButton(Icons.Rounded.Share, "Share", { toast("Room link copied") }, size = 36.dp)
        }

        // Speakers grid
        val speakers: List<Pair<String, String>> = buildList {
            if (onStage) add(Me.name to Me.avatarUrl)
            room.speakers.forEach { add(creatorById(it).name.substringBefore(' ') to creatorById(it).imageUrl) }
        }
        val hostIndex = if (onStage && !isHost) 1 else 0
        LazyVerticalGrid(
            GridCells.Fixed(4),
            Modifier.weight(1f).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { Text("On stage", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.7f)) }
            itemsIndexed(speakers) { i, (name, url) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        AudioPulse(url, speaking = i == speaking && !(i == 0 && onStage && muted), size = 60.dp)
                        if (i == hostIndex) Text("👑", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopEnd))
                        if ((i == 0 && onStage && muted) || i == 2) Icon(Icons.Rounded.MicOff, null, tint = Color.White, modifier = Modifier.align(Alignment.BottomEnd).size(20.dp).clip(CircleShape).background(LiveRed).padding(3.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(name, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1)
                    if (i == hostIndex || (i == 0 && onStage)) Text(if (i == hostIndex) "Host" else "You", style = MaterialTheme.typography.labelSmall, color = Gold)
                }
            }
            if (speakers.size < 8) items(8 - speakers.size) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(60.dp).clip(CircleShape).border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.EventSeat, null, tint = Color.White.copy(alpha = 0.35f))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Empty", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.35f))
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("Listeners", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(top = 8.dp))
            }
            items(sampleViewers) { v ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Avatar(avatar(v.img), 48.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(v.name, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f), maxLines = 1)
                }
            }
        }

        // Controls
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Leave quietly", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFF6B8A),
                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).clickable(onClick = onLeave).padding(horizontal = 18.dp, vertical = 12.dp)
            )
            Spacer(Modifier.weight(1f))
            GlassButton(Icons.Rounded.CardGiftcard, "Gift", { gifts = true }, size = 46.dp)
            Spacer(Modifier.width(10.dp))
            if (onStage) {
                GlassButton(if (muted) Icons.Rounded.MicOff else Icons.Rounded.Mic, "Mic", { muted = !muted }, size = 46.dp, highlighted = !muted)
            } else {
                GlassButton(Icons.Rounded.PanTool, "Raise hand", {
                    handRaised = !handRaised
                    toast(if (handRaised) "Hand raised — the host will invite you" else "Hand lowered")
                }, size = 46.dp, highlighted = handRaised)
            }
        }
    }

    LaunchedEffect(handRaised) { if (handRaised) { delay(4000); handRaised = false; onStage = true; toast("You're on stage — tap the mic to talk") } }

    if (gifts) IshqSheet({ gifts = false }, title = "Send a gift to the room", dark = true) {
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sampleGifts.take(8)) { g ->
                Column(Modifier.clip(RoundedCornerShape(14.dp)).clickable { gifts = false; toast("Sent ${g.name} ${g.emoji}") }.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(g.emoji, fontSize = 36.sp)
                    Text(g.name, style = MaterialTheme.typography.labelSmall, color = Color.White)
                    CoinAmount(g.coins, iconSize = 10.dp, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
