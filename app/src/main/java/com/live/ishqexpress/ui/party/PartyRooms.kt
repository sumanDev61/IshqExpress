package com.live.ishqexpress.ui.party

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
import com.live.ishqexpress.ui.gifts.GiftDrawer
import com.live.ishqexpress.ui.gifts.GiftEffectsLayer
import com.live.ishqexpress.ui.gifts.GiftEffectsState
import com.live.ishqexpress.ui.gifts.GiftEvent
import com.live.ishqexpress.ui.golive.AudioPulse
import com.live.ishqexpress.ui.live.ChatFeed
import com.live.ishqexpress.ui.live.ReportSheet
import com.live.ishqexpress.ui.live.RoomBottomBar
import com.live.ishqexpress.ui.live.RoomHeader
import com.live.ishqexpress.ui.live.ShareSheet
import com.live.ishqexpress.ui.live.SimulateRoom
import com.live.ishqexpress.ui.live.asTarget
import com.live.ishqexpress.ui.live.meTarget
import kotlinx.coroutines.delay

/*
 * Video party — up to 6 people on camera in a grid (host + 5 guests), Tango "multi-guest" style.
 * Chat party — audio room with a host seat + 8 numbered mic seats, Vola Party style.
 * Both: seat requests, host seat controls, gifting any seat (or all), emoji reactions, gift effects.
 */

/** One seat. [user] null = empty. */
data class Seat(
    val index: Int,
    val user: GiftTarget? = null,
    val muted: Boolean = false,
    val cameraOff: Boolean = false,
    val locked: Boolean = false,
    val diamonds: Int = 0,
)

private sealed interface PartySheet {
    data class Gifts(val seat: Int?) : PartySheet
    data class SeatMenu(val seat: Int) : PartySheet
    data object Requests : PartySheet
    data object Join : PartySheet
    data object Emoji : PartySheet
    data object More : PartySheet
    data object Share : PartySheet
    data object Report : PartySheet
    data object Beauty : PartySheet
    data object Voice : PartySheet
    data object Music : PartySheet
    data object Viewers : PartySheet
}

/* ============================== Video party ============================== */

/**
 * @param host the creator hosting; null when you are the host (started from Go Live).
 */
@Composable
fun VideoPartyRoom(host: LiveCreator?, isHost: Boolean, active: Boolean, onClose: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val gate = rememberPermissionGate()
    val hostTarget = host?.asTarget() ?: meTarget()
    val effects = remember { GiftEffectsState() }
    val comments = remember { mutableStateListOf(LiveComment("ishqexpress", 0, "Video party · tap an empty seat to join the stage", system = true)) }
    val seats = remember(hostTarget.id) {
        val guests = sampleCreators.filter { it.id != hostTarget.id }.take(3).map { it.asTarget() }
        mutableStateListOf(
            Seat(0, hostTarget, diamonds = 12_400),
            Seat(1, guests[0], diamonds = 3_210),
            Seat(2, guests[1], muted = true, diamonds = 880),
            Seat(3, guests[2], diamonds = 1_540),
            Seat(4),
            Seat(5, locked = true),
        )
    }
    val requests = remember { mutableStateListOf(2, 11, 8) }
    var sheet by remember { mutableStateOf<PartySheet?>(null) }
    var requested by remember { mutableStateOf(false) }
    var speaking by remember { mutableIntStateOf(0) }
    val mySeat = seats.indexOfFirst { it.user?.id == 0 }

    LaunchedEffect(active) { while (active) { delay(1_700); speaking = seats.indices.filter { seats[it].user != null && !seats[it].muted }.randomOrNull() ?: 0 } }
    // Host accepts the viewer's request after a moment (demo).
    LaunchedEffect(requested) {
        if (requested) {
            delay(3_000)
            val free = seats.indexOfFirst { it.user == null && !it.locked }
            if (free >= 0) { seats[free] = seats[free].copy(user = meTarget()); toast("You're on stage! Camera and mic are on") } else toast("Stage is full — you're next in line")
            requested = false
        }
    }
    val receivers = seats.mapNotNull { it.user }.filter { it.id != 0 }
    SimulateRoom(active, effects, receivers, comments, seed = 77 + hostTarget.id, onGift = { e -> credit(seats, e) })

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF3A0F5C), Color(0xFF1B0B2B), Color(0xFF120818))))) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            RoomHeader(
                if (isHost) "My video party" else hostTarget.name, hostTarget.avatarUrl, 2_480,
                onHost = { if (!isHost) host?.let { nav.push(Screen.OtherProfile, it.id) } },
                onViewers = { sheet = PartySheet.Viewers }, onClose = onClose, showFollow = !isHost,
            )
            Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GlassPill { Text("🎥 Video party", style = MaterialTheme.typography.labelSmall, color = Color.White) }
                GlassPill { Text("${seats.count { it.user != null }}/6 on stage", style = MaterialTheme.typography.labelSmall, color = Color.White) }
                if (isHost && requests.isNotEmpty()) GlassPill(Modifier.clickable { sheet = PartySheet.Requests }) {
                    Text("✋ ${requests.size} requests", style = MaterialTheme.typography.labelSmall, color = Gold)
                }
            }
            Spacer(Modifier.height(8.dp))
            // 2 × 3 grid
            Column(Modifier.padding(horizontal = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                seats.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { seat ->
                            VideoSeat(seat, speaking = seat.index == speaking, Modifier.weight(1f)) {
                                when {
                                    seat.user != null -> sheet = PartySheet.SeatMenu(seat.index)
                                    isHost -> sheet = PartySheet.SeatMenu(seat.index)
                                    seat.locked -> toast("This seat is locked by the host")
                                    mySeat >= 0 -> toast("You're already on stage")
                                    else -> sheet = PartySheet.Join
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            ChatFeed(comments, Modifier.weight(1f).padding(start = 12.dp, end = 60.dp))
            RoomBottomBar(
                onComment = { comments.add(LiveComment(Me.name, Me.level, it)) },
                onGift = { sheet = PartySheet.Gifts(null) },
            ) {
                if (mySeat >= 0) {
                    val s = seats[mySeat]
                    GlassButton(if (s.muted) Icons.Rounded.MicOff else Icons.Rounded.Mic, "Mic", { seats[mySeat] = s.copy(muted = !s.muted) }, highlighted = s.muted)
                    Spacer(Modifier.width(6.dp))
                    GlassButton(Icons.Rounded.FaceRetouchingNatural, "Beauty", { sheet = PartySheet.Beauty }, highlighted = CameraFx.anyBeauty || CameraFx.mask != 0 || CameraFx.filter != 0)
                    Spacer(Modifier.width(6.dp))
                    if (!isHost) { GlassButton(Icons.Rounded.Logout, "Leave stage", { seats[mySeat] = Seat(mySeat) ; toast("You left the stage") }); Spacer(Modifier.width(6.dp)) }
                } else if (!isHost) {
                    GlassButton(Icons.Rounded.GroupAdd, "Join stage", { sheet = PartySheet.Join }, highlighted = requested)
                    Spacer(Modifier.width(6.dp))
                }
                if (isHost) { GlassButton(Icons.Rounded.PersonAdd, "Requests", { sheet = PartySheet.Requests }); Spacer(Modifier.width(6.dp)) }
                GlassButton(Icons.Rounded.MoreHoriz, "More", { sheet = PartySheet.More })
                Spacer(Modifier.width(6.dp))
            }
        }
        GiftEffectsLayer(effects, faceAnchor = androidx.compose.ui.geometry.Offset(0.25f, 0.24f), bannerTop = 380.dp, marqueeTop = 58.dp)
    }

    when (val s = sheet) {
        null -> {}
        is PartySheet.Gifts -> {
            val occupied = seats.filter { it.user != null && it.user.id != 0 }
            GiftDrawer(
                effects, occupied.map { it.user!! },
                onDismiss = { sheet = null }, onRecharge = { sheet = null; nav.push(Screen.CoinStore) },
                initialTarget = occupied.indexOfFirst { it.index == s.seat }.coerceAtLeast(0), multiSelect = true,
                onSent = { g, n, to ->
                    to.forEach { t -> credit(seats, GiftEvent(g, n, Me.name, Me.avatarUrl, Me.level, t)) }
                    comments.add(LiveComment(Me.name, Me.level, "sent ${g.name} ×$n to ${to.joinToString { it.name.substringBefore(' ') }}", gift = g.emoji))
                }
            )
        }
        is PartySheet.SeatMenu -> SeatMenuSheet(seats, s.seat, isHost, video = true, onDismiss = { sheet = null },
            onGift = { sheet = PartySheet.Gifts(s.seat) },
            onProfile = { id -> sheet = null; if (id > 0) nav.push(Screen.OtherProfile, id) },
            onInvite = { sheet = PartySheet.Requests })
        PartySheet.Requests -> RequestsSheet(requests, onDismiss = { sheet = null }, onAccept = { id ->
            val free = seats.indexOfFirst { it.user == null && !it.locked }
            if (free < 0) toast("All open seats are taken") else { seats[free] = seats[free].copy(user = creatorById(id).asTarget()); requests.remove(id) }
        })
        PartySheet.Join -> JoinStageSheet(video = true, requested = requested, onDismiss = { sheet = null }, onToggle = {
            sheet = null
            if (requested) { requested = false; toast("Request cancelled") }
            else gate.request(Perms.video) { ok -> if (ok) { requested = true; toast("Request sent to the host") } else toast("Allow camera and microphone to join the stage") }
        })
        PartySheet.More -> PartyMoreSheet(isHost, onDismiss = { sheet = null }, onShare = { sheet = PartySheet.Share }, onReport = { sheet = PartySheet.Report }, onEnd = { sheet = null; onClose() },
            onBeauty = if (mySeat >= 0) ({ sheet = PartySheet.Beauty }) else null,
            onVoice = if (mySeat >= 0) ({ sheet = PartySheet.Voice }) else null,
            onFlip = if (mySeat >= 0) ({ CameraState.front = !CameraState.front; sheet = null }) else null)
        PartySheet.Beauty -> BeautyPanel { sheet = null }
        PartySheet.Voice -> VoiceChangerSheet { sheet = null }
        PartySheet.Music -> {}
        PartySheet.Viewers -> com.live.ishqexpress.ui.live.ViewerListSheet(host ?: sampleCreators.first(), onDismiss = { sheet = null }, onUser = { v ->
            sheet = null; nav.push(Screen.OtherProfile, sampleCreators[kotlin.math.abs(v.name.hashCode()) % sampleCreators.size].id)
        })
        PartySheet.Share -> ShareSheet(host ?: sampleCreators.first(), onDismiss = { sheet = null })
        PartySheet.Report -> ReportSheet(hostTarget.name, { sheet = null }, { sheet = null; toast("Report sent") })
        PartySheet.Emoji -> {}
    }
}

private fun credit(seats: MutableList<Seat>, e: GiftEvent) {
    val i = seats.indexOfFirst { it.user?.id == e.receiver.id }
    if (i >= 0) seats[i] = seats[i].copy(diamonds = seats[i].diamonds + e.totalCoins / 2)
}

@Composable
private fun VideoSeat(seat: Seat, speaking: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier.aspectRatio(0.95f).clip(shape).background(Color.White.copy(alpha = 0.06f))
            .then(if (speaking && seat.user != null) Modifier.border(2.dp, BrandPink, shape) else Modifier.border(0.5.dp, Color.White.copy(alpha = 0.12f), shape))
            .clickable(onClick = onClick)
    ) {
        val u = seat.user
        when {
            u != null && !seat.cameraOff && u.id == 0 -> SelfCameraTile(photo(77, 400, 420), front = CameraState.front)
            u != null && !seat.cameraOff -> AsyncImage(u.avatarUrl.replace("300", "500"), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            u != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Avatar(u.avatarUrl, 56.dp) }
            seat.locked -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Lock, null, tint = Color.White.copy(alpha = 0.4f)); Text("Locked", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            }
            else -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(40.dp).clip(CircleShape).border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, null, tint = Color.White) }
                Spacer(Modifier.height(4.dp))
                Text("Seat ${seat.index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
        }
        if (u != null) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.65f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.6f))))
            Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (seat.index == 0) Text("👑", fontSize = 11.sp)
                if (seat.muted) Icon(Icons.Rounded.MicOff, null, tint = LiveRed, modifier = Modifier.size(13.dp))
                else if (speaking) com.live.ishqexpress.ui.audio.SoundBars(Color.White)
                Text(" ${if (u.id == 0) "You" else u.name.substringBefore(' ')}", style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1, modifier = Modifier.weight(1f))
                CoinAmount(seat.diamonds, diamond = true, iconSize = 10.dp, color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
            if (seat.index == 0) Text("HOST", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color.White,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp).clip(RoundedCornerShape(4.dp)).background(BrandBrush.primary).padding(horizontal = 5.dp, vertical = 1.dp))
        }
    }
}

/* ============================== Chat party ============================== */

/**
 * @param roomId shown as the room ID; 0 = your own room (host).
 */
@Composable
fun ChatPartyRoom(
    title: String,
    host: GiftTarget,
    roomId: Int,
    isHost: Boolean,
    active: Boolean,
    onClose: () -> Unit,
    guests: List<GiftTarget> = sampleCreators.filter { it.id != host.id }.take(5).map { it.asTarget() },
    listeners: Int = 842,
) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val gate = rememberPermissionGate()
    val effects = remember { GiftEffectsState() }
    val comments = remember { mutableStateListOf(LiveComment("ishqexpress", 0, "Welcome to $title 🌸 Be kind, no spam. Tap an empty seat to speak.", system = true)) }
    // seat 0 = host, 1..8 = mic seats
    val seats = remember(roomId) {
        mutableStateListOf<Seat>().apply {
            add(Seat(0, host, diamonds = 24_800))
            (1..8).forEach { i ->
                val g = guests.getOrNull(i - 1)
                add(Seat(i, g, muted = i == 3, locked = g == null && i == 8, diamonds = if (g != null) 300 * (9 - i) else 0))
            }
        }
    }
    val requests = remember { mutableStateListOf(12, 8) }
    var sheet by remember { mutableStateOf<PartySheet?>(null) }
    var speaking by remember { mutableIntStateOf(0) }
    val reactions = remember { mutableStateMapOf<Int, String>() }   // seat → emoji bubble
    var music by remember { mutableStateOf(if (isHost) null else "Kesariya — Arijit Singh") }
    val mySeat = seats.indexOfFirst { it.user?.id == 0 }

    LaunchedEffect(active) {
        var t = 0
        while (active) {
            delay(1_500); t++
            speaking = seats.indices.filter { seats[it].user != null && !seats[it].muted }.randomOrNull() ?: 0
            if (t % 3 == 0) seats.indices.filter { seats[it].user != null && seats[it].user!!.id != 0 }.randomOrNull()?.let {
                reactions[it] = listOf("😂", "❤️", "👏", "🔥", "😍", "🎉")[t % 6]
            }
        }
    }
    val receivers = seats.mapNotNull { it.user }.filter { it.id != 0 }
    SimulateRoom(active, effects, receivers, comments, seed = 500 + roomId, onGift = { e -> credit(seats, e) })

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF4B1D6E), Color(0xFF2A0B4A), Color(0xFF14081E))))) {
        // The host's own room uses the skin picked on Go Live; other rooms would get theirs from the server.
        if (isHost) {
            com.live.ishqexpress.ui.common.SkinBackdrop(com.live.ishqexpress.ui.common.roomSkins[com.live.ishqexpress.ui.common.RoomSkinState.selected], Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.45f)))))
        }
        // Soft room theme
        Box(Modifier.align(Alignment.TopEnd).offset(80.dp, (-60).dp).size(260.dp).clip(CircleShape).background(BrandPink.copy(alpha = 0.12f)))
        Box(Modifier.align(Alignment.CenterStart).offset((-90).dp, 0.dp).size(220.dp).clip(CircleShape).background(BrandViolet.copy(alpha = 0.18f)))

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            RoomHeader(
                title, host.avatarUrl, listeners,
                onHost = { if (host.id > 0) nav.push(Screen.OtherProfile, host.id) },
                onViewers = { sheet = PartySheet.Viewers }, onClose = onClose, showFollow = !isHost,
                subtitle = { Text("ID ${if (roomId == 0) 80412337 else 300_000 + roomId}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f)) },
            )
            Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassPill { Text("📢 Room rules · be kind", style = MaterialTheme.typography.labelSmall, color = Color.White) }
                music?.let { m -> GlassPill { Text("🎵 ", fontSize = 11.sp); Text(m, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1) } }
                if (isHost && requests.isNotEmpty()) GlassPill(Modifier.clickable { sheet = PartySheet.Requests }) { Text("✋ ${requests.size}", style = MaterialTheme.typography.labelSmall, color = Gold) }
            }
            Spacer(Modifier.height(14.dp))

            // Host seat
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AudioSeat(seats[0], speaking = speaking == 0, reaction = reactions[0], size = 76.dp, onReactionDone = { reactions.remove(0) }) { sheet = PartySheet.SeatMenu(0) }
            }
            Spacer(Modifier.height(10.dp))
            // 8 mic seats
            (1..8).chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { i ->
                        val seat = seats[i]
                        AudioSeat(seat, speaking = speaking == i, reaction = reactions[i], size = 56.dp, onReactionDone = { reactions.remove(i) }) {
                            when {
                                seat.user != null || isHost -> sheet = PartySheet.SeatMenu(i)
                                seat.locked -> toast("Seat ${i} is locked")
                                mySeat >= 0 -> { seats[i] = seats[i].copy(user = seats[mySeat].user, muted = seats[mySeat].muted); seats[mySeat] = Seat(mySeat) } // move seats
                                else -> sheet = PartySheet.Join
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            ChatFeed(comments, Modifier.weight(1f).padding(start = 12.dp, end = 24.dp))
            RoomBottomBar(
                onComment = { comments.add(LiveComment(Me.name, Me.level, it)) },
                onGift = { sheet = PartySheet.Gifts(null) },
                placeholder = "Chat…",
            ) {
                if (mySeat >= 0) {
                    val s = seats[mySeat]
                    GlassButton(if (s.muted) Icons.Rounded.MicOff else Icons.Rounded.Mic, "Mic", { seats[mySeat] = s.copy(muted = !s.muted) }, highlighted = !s.muted)
                    Spacer(Modifier.width(6.dp))
                    GlassButton(Icons.Rounded.GraphicEq, "Voice changer", { sheet = PartySheet.Voice }, highlighted = VoiceFx.active)
                    Spacer(Modifier.width(6.dp))
                    GlassButton(Icons.Rounded.EmojiEmotions, "Emoji", { sheet = PartySheet.Emoji })
                    Spacer(Modifier.width(6.dp))
                } else {
                    GlassButton(Icons.Rounded.PanTool, "Request to speak", { sheet = PartySheet.Join })
                    Spacer(Modifier.width(6.dp))
                }
                GlassButton(Icons.Rounded.MoreHoriz, "More", { sheet = PartySheet.More })
                Spacer(Modifier.width(6.dp))
            }
        }
        GiftEffectsLayer(effects, faceAnchor = androidx.compose.ui.geometry.Offset(0.5f, 0.24f), bannerTop = 420.dp, marqueeTop = 58.dp)
    }

    when (val s = sheet) {
        null -> {}
        is PartySheet.Gifts -> {
            val occupied = seats.filter { it.user != null && it.user.id != 0 }
            GiftDrawer(
                effects, occupied.map { it.user!! },
                onDismiss = { sheet = null }, onRecharge = { sheet = null; nav.push(Screen.CoinStore) },
                initialTarget = occupied.indexOfFirst { it.index == s.seat }.coerceAtLeast(0), multiSelect = true,
                onSent = { g, n, to ->
                    to.forEach { t -> credit(seats, GiftEvent(g, n, Me.name, Me.avatarUrl, Me.level, t)) }
                    comments.add(LiveComment(Me.name, Me.level, "sent ${g.name} ×$n to ${to.joinToString { it.name.substringBefore(' ') }}", gift = g.emoji))
                }
            )
        }
        is PartySheet.SeatMenu -> SeatMenuSheet(seats, s.seat, isHost, video = false, onDismiss = { sheet = null },
            onGift = { sheet = PartySheet.Gifts(s.seat) },
            onProfile = { id -> sheet = null; if (id > 0) nav.push(Screen.OtherProfile, id) },
            onInvite = { sheet = PartySheet.Requests })
        PartySheet.Requests -> RequestsSheet(requests, onDismiss = { sheet = null }, onAccept = { id ->
            val free = (1..8).firstOrNull { seats[it].user == null && !seats[it].locked }
            if (free == null) toast("All mic seats are taken") else { seats[free] = seats[free].copy(user = creatorById(id).asTarget()); requests.remove(id) }
        })
        PartySheet.Join -> JoinStageSheet(video = false, requested = false, onDismiss = { sheet = null }, onToggle = {
            sheet = null
            gate.request(Perms.audio) { ok ->
                val free = (1..8).firstOrNull { seats[it].user == null && !seats[it].locked }
                if (!ok) toast("Allow the microphone to take a seat")
                else if (free == null) toast("No free seat — you're in the queue")
                else { seats[free] = seats[free].copy(user = meTarget(), muted = true); toast("You took seat $free · tap the mic to talk") }
            }
        })
        PartySheet.Emoji -> EmojiSheet(onDismiss = { sheet = null }, onPick = { e -> if (mySeat >= 0) reactions[mySeat] = e; sheet = null })
        PartySheet.More -> PartyMoreSheet(isHost, onDismiss = { sheet = null }, onShare = { sheet = PartySheet.Share }, onReport = { sheet = PartySheet.Report }, onEnd = { sheet = null; onClose() },
            onMusic = if (isHost) ({ sheet = PartySheet.Music }) else null,
            onVoice = if (mySeat >= 0) ({ sheet = PartySheet.Voice }) else null)
        PartySheet.Voice -> VoiceChangerSheet { sheet = null }
        PartySheet.Music -> RoomMusicSheet(music, onDismiss = { sheet = null }, onPick = { music = it; sheet = null })
        PartySheet.Beauty -> {}
        PartySheet.Viewers -> com.live.ishqexpress.ui.live.ViewerListSheet(sampleCreators.firstOrNull { it.id == host.id } ?: sampleCreators.first(), onDismiss = { sheet = null }, onUser = { v ->
            sheet = null; nav.push(Screen.OtherProfile, sampleCreators[kotlin.math.abs(v.name.hashCode()) % sampleCreators.size].id)
        })
        PartySheet.Share -> ShareSheet(sampleCreators.first(), onDismiss = { sheet = null })
        PartySheet.Report -> ReportSheet(title, { sheet = null }, { sheet = null; toast("Report sent") })
    }
}

@Composable
private fun AudioSeat(seat: Seat, speaking: Boolean, reaction: String?, size: Dp, onReactionDone: () -> Unit, onClick: () -> Unit) {
    Column(Modifier.width(size + 18.dp).clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null, onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(size + 12.dp), contentAlignment = Alignment.Center) {
            val u = seat.user
            when {
                u != null -> AudioPulse(u.avatarUrl, speaking = speaking && !seat.muted, size = size)
                seat.locked -> Box(Modifier.size(size).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Lock, null, tint = Color.White.copy(alpha = 0.45f)) }
                else -> Box(Modifier.size(size).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.EventSeat, null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(size * 0.42f))
                }
            }
            if (seat.index == 0 && u != null) Text("👑", fontSize = 18.sp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-6).dp))
            if (u != null && seat.muted) Icon(Icons.Rounded.MicOff, null, tint = Color.White, modifier = Modifier.align(Alignment.BottomEnd).size(18.dp).clip(CircleShape).background(LiveRed).padding(3.dp))
            if (reaction != null) {
                key(reaction) {
                    val a = remember { Animatable(0f) }
                    LaunchedEffect(Unit) { a.animateTo(1f, tween(1_800)); onReactionDone() }
                    Text(reaction, fontSize = (size.value * 0.55f).sp, modifier = Modifier.graphicsLayer {
                        val v = a.value; scaleX = if (v < 0.2f) v / 0.2f * 1.2f else 1f; scaleY = scaleX; alpha = if (v > 0.8f) (1f - v) / 0.2f else 1f
                    })
                }
            }
        }
        Text(
            when { seat.user?.id == 0 -> "You"; seat.user != null -> seat.user.name.substringBefore(' '); seat.index == 0 -> "Host"; else -> "${seat.index}" },
            style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = if (seat.user != null) 1f else 0.6f), maxLines = 1
        )
        if (seat.user != null) Row(verticalAlignment = Alignment.CenterVertically) {
            DiamondIcon(9.dp); Text(" ${seat.diamonds.compact()}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White.copy(alpha = 0.75f))
        }
    }
}

/* ============================== Shared party sheets ============================== */

@Composable
private fun SeatMenuSheet(
    seats: MutableList<Seat>, index: Int, isHost: Boolean, video: Boolean,
    onDismiss: () -> Unit, onGift: () -> Unit, onProfile: (Int) -> Unit, onInvite: () -> Unit,
) {
    val toast = LocalToast.current
    val gate = rememberPermissionGate()
    val seat = seats[index]
    val u = seat.user
    IshqSheet(onDismiss) {
        if (u != null) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(u.avatarUrl, 56.dp, ring = index == 0)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (u.id == 0) "${u.name} (you)" else u.name, style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(if (index == 0) "Host" else "Seat ${index + if (video) 1 else 0}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                CoinAmount(seat.diamonds, diamond = true)
            }
            Spacer(Modifier.height(10.dp))
            if (u.id != 0) {
                MenuRow(Icons.Rounded.CardGiftcard, "Send a gift", onClick = onGift)
                MenuRow(Icons.Rounded.Person, "View profile", onClick = { onProfile(u.id) })
            }
            if (isHost && index != 0) {
                MenuRow(if (seat.muted) Icons.Rounded.Mic else Icons.Rounded.MicOff, if (seat.muted) "Unmute" else "Mute mic", onClick = { seats[index] = seat.copy(muted = !seat.muted); onDismiss() })
                if (video) MenuRow(Icons.Rounded.VideocamOff, if (seat.cameraOff) "Ask to turn camera on" else "Turn camera off", onClick = { seats[index] = seat.copy(cameraOff = !seat.cameraOff); onDismiss() })
                MenuRow(Icons.Rounded.PersonRemove, "Remove from seat", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = { seats[index] = Seat(index); toast("${u.name.substringBefore(' ')} moved to audience"); onDismiss() })
            }
        } else {
            Text(if (seat.locked) "Seat is locked" else "Empty seat", style = MaterialTheme.typography.titleLarge, color = Ink, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
            if (isHost) {
                MenuRow(Icons.Rounded.PersonAdd, "Invite someone", onClick = onInvite)
                MenuRow(if (seat.locked) Icons.Rounded.LockOpen else Icons.Rounded.Lock, if (seat.locked) "Unlock seat" else "Lock seat", onClick = { seats[index] = seat.copy(locked = !seat.locked); onDismiss() })
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RequestsSheet(requests: MutableList<Int>, onDismiss: () -> Unit, onAccept: (Int) -> Unit) {
    IshqSheet(onDismiss, title = "Requests to join (${requests.size})") {
        if (requests.isEmpty()) EmptyState("✋", "No requests", "Share the room to get people on stage.")
        requests.toList().forEach { id ->
            val c = creatorById(id)
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(c.imageUrl, 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                    LevelBadge(10 + id)
                }
                SmallOutlineButton("Decline", { requests.remove(id) }, height = 32.dp)
                Spacer(Modifier.width(6.dp))
                SmallGradientButton("Accept", { onAccept(id) }, height = 32.dp)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun JoinStageSheet(video: Boolean, requested: Boolean, onDismiss: () -> Unit, onToggle: () -> Unit) {
    IshqSheet(onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (video) "🎥" else "🎙️", fontSize = 44.sp)
            Text(if (video) "Join the video party" else "Take a mic seat", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                if (video) "Your camera and mic go live to everyone in the room once the host accepts." else "You'll join muted. Tap the mic when you're ready to talk.",
                style = MaterialTheme.typography.bodyMedium, color = InkSecondary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            if (requested) SmallOutlineButton("Cancel request", onToggle, Modifier.fillMaxWidth(), height = 52.dp)
            else PrimaryButton(if (video) "Request to join" else "Take seat", onToggle, leadingIcon = if (video) Icons.Rounded.Videocam else Icons.Rounded.Mic)
        }
    }
}

@Composable
private fun EmojiSheet(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val emojis = listOf("😂", "❤️", "👏", "🔥", "😍", "🎉", "😭", "🙏", "😎", "🤣", "💯", "🥳", "😘", "🤔", "😡", "👍")
    IshqSheet(onDismiss, title = "React on your seat", dark = true) {
        emojis.chunked(8).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { e -> Text(e, fontSize = 30.sp, modifier = Modifier.clip(CircleShape).clickable { onPick(e) }.padding(4.dp)) }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PartyMoreSheet(
    isHost: Boolean, onDismiss: () -> Unit, onShare: () -> Unit, onReport: () -> Unit, onEnd: () -> Unit,
    onMusic: (() -> Unit)? = null, onBeauty: (() -> Unit)? = null, onVoice: (() -> Unit)? = null, onFlip: (() -> Unit)? = null,
) {
    IshqSheet(onDismiss) {
        if (onBeauty != null) MenuRow(Icons.Rounded.FaceRetouchingNatural, "Beauty & filters", subtitle = "Beauty, filters, masks and scenes", onClick = onBeauty)
        if (onVoice != null) MenuRow(Icons.Rounded.GraphicEq, "Voice changer", subtitle = "Now: ${VoiceFx.current.emoji} ${VoiceFx.current.name}", onClick = onVoice)
        if (onFlip != null) MenuRow(Icons.Rounded.Cameraswitch, "Flip camera", onClick = onFlip)
        if (onMusic != null) MenuRow(Icons.Rounded.MusicNote, "Play music", subtitle = "Background music for the room", onClick = onMusic)
        MenuRow(Icons.Rounded.Share, "Share room", onClick = onShare)
        if (isHost) MenuRow(Icons.Rounded.PowerSettingsNew, "End party", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = onEnd)
        else MenuRow(Icons.Rounded.Flag, "Report", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = onReport)
        Spacer(Modifier.height(8.dp))
    }
}

/** Host picks the room's background music (mock catalogue; streamed by the SDK in production). */
private val roomTracks = listOf(
    "Kesariya — Arijit Singh", "Tum Hi Ho — Arijit Singh", "Raataan Lambiyan — Jubin Nautiyal", "Apna Bana Le — Arijit Singh",
    "Chaleya — Arijit Singh", "Pasoori — Ali Sethi", "Heeriye — Arijit Singh", "Lo-fi Chai Beats", "Rain & Piano",
)

@Composable
private fun RoomMusicSheet(current: String?, onDismiss: () -> Unit, onPick: (String?) -> Unit) {
    var q by remember { mutableStateOf("") }
    IshqSheet(onDismiss, title = "Room music") {
        OutlinedTextField(
            q, { q = it }, Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            placeholder = { Text("Search songs") }, leadingIcon = { Icon(Icons.Rounded.Search, null) },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline)
        )
        Spacer(Modifier.height(8.dp))
        roomTracks.filter { it.contains(q, ignoreCase = true) }.forEach { t ->
            val on = t == current
            MenuRow(
                if (on) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote, t.substringBefore(" — "), subtitle = t.substringAfter(" — ", "Instrumental"),
                iconTint = if (on) BrandPink else InkSecondary,
                trailing = { if (on) Text("Playing", style = MaterialTheme.typography.labelMedium, color = BrandPink) },
                onClick = { onPick(t) }
            )
        }
        if (current != null) MenuRow(Icons.Rounded.MusicOff, "Stop music", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = { onPick(null) })
        Spacer(Modifier.height(8.dp))
    }
}
