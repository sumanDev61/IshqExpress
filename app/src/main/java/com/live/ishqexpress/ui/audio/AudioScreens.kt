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
        ScreenScaffold("Chat party", onBack, actions = { TopAction(Icons.Rounded.Search, "Search") { nav.push(Screen.Search) } }) {
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
        ) { SmallGradientButton("Start a party", { creating = true }, icon = Icons.Rounded.Mic, height = 50.dp) }
    }
    if (creating) CreateRoomSheet(onDismiss = { creating = false }, onCreate = { creating = false; nav.push(Screen.AudioRoom, 0) })
}

@Composable
private fun RoomCard(r: AudioRoom, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().tilt3D().glass(RoundedCornerShape(20.dp), glow = BrandPink).clickable(onClick = onClick).padding(16.dp)
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
    IshqSheet(onDismiss, title = "Start a chat party") {
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
            PrimaryButton("Start party", onCreate, enabled = title.isNotBlank(), leadingIcon = Icons.Rounded.Mic)
        }
    }
}

/* ------------------------------ 35 · Audio room → Chat party ------------------------------ */

@Composable
fun AudioRoomScreen(roomId: Int, onLeave: () -> Unit) {
    DarkSystemBars()
    val room = sampleRooms.firstOrNull { it.id == roomId }
    if (room == null) {
        com.live.ishqexpress.ui.party.ChatPartyRoom(
            title = "${Me.name}'s party 🎶", host = com.live.ishqexpress.ui.live.meTarget(), roomId = 0,
            isHost = true, active = true, onClose = onLeave, guests = emptyList(), listeners = 0
        )
    } else {
        com.live.ishqexpress.ui.party.ChatPartyRoom(
            title = room.title, host = creatorById(room.host).let { GiftTarget(it.id, it.name, it.imageUrl) },
            roomId = room.id, isHost = false, active = true, onClose = onLeave,
            guests = room.speakers.filter { it != room.host }.map { id -> creatorById(id).let { GiftTarget(it.id, it.name, it.imageUrl) } },
            listeners = room.listeners
        )
    }
}
