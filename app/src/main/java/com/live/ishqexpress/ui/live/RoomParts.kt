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
import com.live.ishqexpress.ui.gifts.Entrance
import com.live.ishqexpress.ui.gifts.GiftEffectsState
import com.live.ishqexpress.ui.gifts.GiftEvent
import kotlinx.coroutines.delay
import kotlin.random.Random

/*
 * Pieces shared by every room type: solo live, PK, dual, video party and chat party.
 */

/* ------------------------------ Header ------------------------------ */

@Composable
fun RoomHeader(
    title: String,
    avatarUrl: String,
    viewerCount: Int,
    onHost: () -> Unit,
    onViewers: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: @Composable () -> Unit = { CoinAmount(186_400, diamond = true, iconSize = 10.dp, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall) },
    showFollow: Boolean = true,
) {
    val toast = LocalToast.current
    var following by remember { mutableStateOf(false) }
    Row(modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.32f)).clickable(onClick = onHost).padding(start = 3.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(avatarUrl, 34.dp)
            Spacer(Modifier.width(6.dp))
            Column(Modifier.widthIn(max = 110.dp)) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                subtitle()
            }
            Spacer(Modifier.width(6.dp))
            if (showFollow) AnimatedContent(following, label = "follow") { f ->
                if (f) Icon(Icons.Rounded.Check, "Following", tint = Color.White, modifier = Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).padding(6.dp))
                else Icon(Icons.Rounded.Add, "Follow", tint = Color.White, modifier = Modifier.size(30.dp).clip(CircleShape).background(BrandBrush.primary).clickable { following = true; toast("Following $title") }.padding(5.dp))
            } else Spacer(Modifier.width(6.dp))
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.clickable(onClick = onViewers), verticalAlignment = Alignment.CenterVertically) {
            sampleViewers.take(3).forEachIndexed { i, v -> Avatar(avatar(v.img), 30.dp, Modifier.offset(x = (-6 * i).dp)) }
            GlassPill(Modifier.offset(x = (-10).dp)) { Text(viewerCount.compact(), style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1, softWrap = false) }
        }
        GlassButton(Icons.Rounded.Close, "Close", onClose, size = 34.dp)
    }
}

/* ------------------------------ Bottom bar ------------------------------ */

@Composable
fun RoomBottomBar(
    onComment: (String) -> Unit,
    onGift: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Say hi…",
    actions: @Composable RowScope.() -> Unit = {},
) {
    var draft by remember { mutableStateOf("") }
    Row(modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).height(42.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)).padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                if (draft.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f), maxLines = 1)
                BasicTextField(
                    draft, { draft = it }, singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (draft.isNotBlank()) {
                Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = Color.White, modifier = Modifier.size(34.dp).clip(CircleShape).background(BrandBrush.primary).clickable {
                    onComment(draft.trim()); draft = ""
                }.padding(8.dp))
            }
        }
        Spacer(Modifier.width(6.dp))
        actions()
        Box(
            Modifier.size(44.dp).shadow(8.dp, CircleShape, spotColor = BrandPink).clip(CircleShape).background(BrandBrush.deep).clickable(onClick = onGift),
            contentAlignment = Alignment.Center
        ) { Text("🎁", fontSize = 22.sp) }
    }
}

/* ------------------------------ Hearts ------------------------------ */

@Composable
fun FloatingHearts(count: Int, modifier: Modifier) {
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

/* ------------------------------ Simulation (stands in for the room's real-time channel) ------------------------------ */

private val rides = listOf("🏎️", "🏍️", "🚁", "🦄", "🛸")
private val chatter = listOf("❤️❤️❤️", "Hi from Pune!", "Wah wah 👏", "Follow back please 🙏", "Best live today", "Kya baat hai 🔥", "Hello everyone", "😂😂")

/**
 * Feeds a room with fake viewer activity while [active]: chat lines, gifts from other viewers
 * (classic, the odd motion gift), VIP entrances and platform marquees.
 * Replace with the subscription to the room's event stream.
 *
 * Debug builds only: in release, fake users sending gifts would mislead real users (Play deceptive-behaviour policy).
 */
@Composable
fun SimulateRoom(
    active: Boolean,
    effects: GiftEffectsState,
    receivers: List<GiftTarget>,
    comments: MutableList<LiveComment>,
    seed: Int,
    onGift: (GiftEvent) -> Unit = {},
) {
    LaunchedEffect(active, receivers) {
        if (!com.live.ishqexpress.BuildConfig.DEBUG || !active || receivers.isEmpty()) return@LaunchedEffect
        val r = Random(seed)
        var tick = 0
        delay(1_200)
        while (true) {
            tick++
            val v = sampleViewers[r.nextInt(sampleViewers.size)]
            if (tick % 2 == 0) comments.add(LiveComment(v.name, v.level, chatter[r.nextInt(chatter.size)]))
            if (tick % 3 == 0) {
                val pool = sampleGifts.filter { it.vip == 0 && (it.kind != GiftKind.Motion || r.nextInt(4) == 0) }
                val g = pool[r.nextInt(pool.size)]
                val count = if (g.coins < 50) listOf(1, 1, 3, 10, 66)[r.nextInt(5)] else 1
                val to = receivers[r.nextInt(receivers.size)]
                val e = GiftEvent(g, count, v.name, avatar(v.img), v.level, to)
                effects.post(e)
                onGift(e)
                comments.add(LiveComment(v.name, v.level, "sent ${g.name} ×$count to ${to.name.substringBefore(' ')}", gift = g.emoji))
            }
            if (tick % 5 == 1 && v.vip > 0) {
                effects.enter(Entrance(v.name, avatar(v.img), v.level, v.vip, rides[r.nextInt(rides.size)]))
                comments.add(LiveComment(v.name, v.level, "joined the room"))
            }
            if (tick % 11 == 0) {
                val c = sampleCreators[r.nextInt(sampleCreators.size)]
                effects.showMarquee("${sampleViewers[r.nextInt(3)].name} sent 🚀 Rocket to ${c.name}")
            }
            if (comments.size > 60) comments.removeAt(1)
            delay(1_300L + r.nextLong(900))
        }
    }
}

fun LiveCreator.asTarget() = GiftTarget(id, name, imageUrl)
fun meTarget() = GiftTarget(0, Me.name, Me.avatarUrl)
