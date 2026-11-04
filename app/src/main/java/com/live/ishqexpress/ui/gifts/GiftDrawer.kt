package com.live.ishqexpress.ui.gifts

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
import kotlinx.coroutines.delay

private val Quantities = listOf(1, 10, 66, 99, 188, 520, 1314)

/**
 * Tango-style gift drawer.
 *
 * @param targets who can receive. One target = no picker (solo live). Several = party seats or PK sides.
 * @param multiSelect parties let you gift several seats at once ("All on mic").
 * @param onSent called after each successful send (e.g. to add a chat line).
 */
@Composable
fun GiftDrawer(
    effects: GiftEffectsState,
    targets: List<GiftTarget>,
    onDismiss: () -> Unit,
    onRecharge: () -> Unit,
    initialTarget: Int = 0,
    multiSelect: Boolean = false,
    pkMode: Boolean = false,
    onSent: (Gift, Int, List<GiftTarget>) -> Unit = { _, _, _ -> },
) {
    val toast = LocalToast.current
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Gift?>(null) }
    var count by remember { mutableIntStateOf(1) }
    var qtyMenu by remember { mutableStateOf(false) }
    val chosen = remember { mutableStateListOf<Int>().apply { if (targets.isNotEmpty()) add(initialTarget.coerceIn(0, targets.lastIndex)) } }
    var comboTick by remember { mutableIntStateOf(0) }   // >0 while the combo window is open
    var comboRun by remember { mutableIntStateOf(0) }

    val tabEnum = GiftTab.entries[tab]
    val fromBag = tabEnum == GiftTab.Bag
    val gifts = if (fromBag) DemoWallet.bag.keys.sorted().map { giftById(it) } else sampleGifts.filter { it.tab == tabEnum }
    LaunchedEffect(tab) { selected = gifts.firstOrNull { it.vip <= Me.vipTier }; comboTick = 0 }

    fun send() {
        val g = selected ?: return
        val to = chosen.sorted().map { targets[it] }
        if (to.isEmpty()) { toast("Choose who to send to"); return }
        when (GiftSender.send(effects, g, count, to, fromBag)) {
            SendResult.Sent -> { comboRun++; comboTick = 1; onSent(g, count, to) }
            SendResult.NotEnoughCoins -> { toast("Not enough coins"); onRecharge() }
            SendResult.NotInBag -> toast("Only ${DemoWallet.bag[g.id] ?: 0} left in your bag")
            SendResult.Locked -> toast("Unlocks at VIP ${g.vip}")
        }
    }

    IshqSheet(onDismiss, dark = true) {
        // ---- Recipients
        if (targets.size > 1) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Send to", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                Spacer(Modifier.width(8.dp))
                LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (multiSelect) item {
                        val all = chosen.size == targets.size
                        Text(
                            "All", style = MaterialTheme.typography.labelMedium, color = Color.White,
                            modifier = Modifier.clip(CircleShape).then(if (all) Modifier.background(BrandBrush.primary) else Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape))
                                .clickable { chosen.clear(); if (!all) chosen.addAll(targets.indices) else chosen.add(0) }.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                    itemsIndexed(targets) { i, t ->
                        val on = i in chosen
                        Box(Modifier.clickable {
                            if (multiSelect) { if (on && chosen.size > 1) chosen.remove(i) else if (!on) chosen.add(i) }
                            else { chosen.clear(); chosen.add(i) }
                        }) {
                            Avatar(t.avatarUrl, 34.dp, Modifier.border(2.dp, if (on) BrandPink else Color.Transparent, CircleShape).alpha(if (on) 1f else 0.55f))
                            Text(
                                if (pkMode) (if (i == 0) "A" else "B") else "${i + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color.White,
                                modifier = Modifier.align(Alignment.BottomEnd).clip(CircleShape).background(if (on) BrandPink else Color.Gray).padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        // ---- Tabs
        LazyRow(contentPadding = PaddingValues(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            itemsIndexed(GiftTab.entries) { i, t ->
                Column(Modifier.clip(RoundedCornerShape(10.dp)).clickable { tab = i }.padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(t.label, style = MaterialTheme.typography.titleSmall, color = if (i == tab) Color.White else Color.White.copy(alpha = 0.5f))
                        if (t == GiftTab.Bag && DemoWallet.bag.isNotEmpty()) Text(" ${DemoWallet.bag.values.sum().compact()}", style = MaterialTheme.typography.labelSmall, color = Gold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.width(if (i == tab) 16.dp else 0.dp).height(3.dp).clip(CircleShape).background(BrandBrush.primary))
                }
            }
        }

        // ---- Tab hint
        val hint = when (tabEnum) {
            GiftTab.Motion -> "🎬 Plays a full-screen motion effect for everyone in the room"
            GiftTab.Ar -> "✨ Appears on the host's face for 6 seconds"
            GiftTab.Exclusive -> "👑 VIP-only gifts · you're VIP ${Me.vipTier}"
            GiftTab.Bag -> "🎒 Free gifts from events and daily rewards — sending costs nothing"
            GiftTab.Popular -> if (pkMode) "⚡ PK Booster counts double in battles" else null
        }
        if (hint != null) Text(hint, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))

        // ---- Grid (+ combo button floating over its corner for 3 s after a send)
        Box {
        if (gifts.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(232.dp), contentAlignment = Alignment.Center) {
                Text("Your bag is empty. Events and daily rewards fill it up.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
            }
        } else LazyVerticalGrid(
            GridCells.Fixed(4), Modifier.height(232.dp).padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(gifts, key = { it.id }) { g ->
                val sel = selected?.id == g.id
                val locked = g.vip > Me.vipTier
                Column(
                    Modifier.padding(horizontal = 3.dp).clip(RoundedCornerShape(14.dp))
                        .then(if (sel) Modifier.background(Color.White.copy(alpha = 0.08f)).border(1.5.dp, BrandPink, RoundedCornerShape(14.dp)) else Modifier)
                        .clickable { if (locked) toast("${g.name} unlocks at VIP ${g.vip}") else { selected = g; comboTick = 0 } }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        Text(g.emoji, fontSize = 34.sp, modifier = Modifier.alpha(if (locked) 0.45f else 1f).graphicsLayer { if (sel) { scaleX = 1.12f; scaleY = 1.12f } })
                        val badge = when {
                            locked -> "VIP${g.vip}" to vipTiers[g.vip - 1].color
                            g.kind == GiftKind.Motion -> "MOTION" to BrandOrchid
                            g.kind == GiftKind.Ar -> "AR" to BattleBlue
                            g.pkBoost -> "PK×2" to Color(0xFFFF6A00)
                            else -> null
                        }
                        if (badge != null) Text(
                            badge.first, style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.5.sp), color = Color.White,
                            modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-2).dp).clip(RoundedCornerShape(3.dp)).background(badge.second).padding(horizontal = 3.dp)
                        )
                        if (locked) Icon(Icons.Rounded.Lock, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(16.dp))
                    }
                    Text(g.name, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(2.dp))
                    if (fromBag) Text("×${DemoWallet.bag[g.id] ?: 0}", style = MaterialTheme.typography.labelSmall, color = Gold)
                    else CoinAmount(g.coins, iconSize = 10.dp, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        if (comboTick > 0) ComboButton(comboRun, Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 4.dp), onTap = { send() }, onTimeout = { comboTick = 0; comboRun = 0 })
        }

        // ---- Footer
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.clip(CircleShape).clickable(onClick = onRecharge).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                CoinIcon(18.dp)
                AnimatedContent(DemoWallet.coins, label = "coins") { Text(" ${it.grouped()}", style = MaterialTheme.typography.titleSmall, color = Color.White) }
                Text("  Recharge ›", style = MaterialTheme.typography.labelMedium, color = Gold)
            }
            Spacer(Modifier.weight(1f))
            Box {
                Row(
                    Modifier.height(38.dp).clip(RoundedCornerShape(topStart = 19.dp, bottomStart = 19.dp)).border(1.dp, BrandPink, RoundedCornerShape(topStart = 19.dp, bottomStart = 19.dp))
                        .clickable { qtyMenu = true }.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("×$count", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Icon(Icons.Rounded.KeyboardArrowUp, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(qtyMenu, { qtyMenu = false }, containerColor = Color(0xFF241A2E)) {
                    Quantities.reversed().forEach { q ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("×$q", style = MaterialTheme.typography.titleSmall, color = if (q == count) BrandPink else Color.White, modifier = Modifier.width(56.dp))
                                    Text(when (q) { 1314 -> "Forever"; 520 -> "I love you"; 188 -> "Hug"; 66 -> "Smooth"; else -> "" }, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                                }
                            },
                            onClick = { count = q; qtyMenu = false; comboTick = 0 }
                        )
                    }
                }
            }
            Box(
                Modifier.height(38.dp).clip(RoundedCornerShape(topEnd = 19.dp, bottomEnd = 19.dp))
                    .background(if (selected != null) BrandBrush.primary else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
                    .clickable(enabled = selected != null && comboTick == 0) { send() }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) { Text("Send", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp), color = Color.White) }
        }
    }
}

@Composable
private fun ComboButton(run: Int, modifier: Modifier, onTap: () -> Unit, onTimeout: () -> Unit) {
    val ring = remember { Animatable(1f) }
    LaunchedEffect(run) { ring.snapTo(1f); ring.animateTo(0f, tween(3_000, easing = LinearEasing)); onTimeout() }
    val press = remember { Animatable(1f) }
    LaunchedEffect(run) { press.snapTo(0.85f); press.animateTo(1f, spring(dampingRatio = 0.4f)) }
    run {
        Box(
            modifier.size(92.dp).graphicsLayer { scaleX = press.value; scaleY = press.value }.clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null, onClick = onTap),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color.Black.copy(alpha = 0.35f))
                drawArc(Brush.sweepGradient(listOf(Gold, BrandPink, Gold)), -90f, 360f * ring.value, false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            Box(Modifier.size(74.dp).clip(CircleShape).background(BrandBrush.deep), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("COMBO", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("×${run}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Gold)
                }
            }
        }
    }
}
