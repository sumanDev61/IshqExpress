package com.live.ishqexpress.ui.wallet

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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

/*
 * 58 Coin store · 59 Checkout · 60 Transaction history · 61 VIP · 62 Daily rewards
 * 63 Creator earnings · 64 Payout + KYC · 65 Payout history · 66 Leaderboards · Extra: Agency
 */

/* ============================== 58 · Coin store + 59 · Checkout ============================== */

@Composable
fun CoinStoreScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    var checkout by remember { mutableStateOf<CoinPack?>(null) }
    var firstPurchase by rememberSaveable { mutableStateOf(true) }

    ScreenScaffold("Coin store", onBack, actions = { TopAction(Icons.Rounded.ReceiptLong, "History") { nav.push(Screen.Transactions) } }) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFFFFB020), Color(0xFFFF8C5A), Color(0xFFFF2E8B)))) {
                    Column {
                        Text("My coins", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CoinIcon(30.dp); Spacer(Modifier.width(8.dp))
                            AnimatedContent(DemoWallet.coins, label = "bal") { Text(it.grouped(), style = MaterialTheme.typography.displaySmall, color = Color.White) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Coins are used for gifts, tickets, private shows and subscriptions.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
            if (firstPurchase) item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PinkTint).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🎉", fontSize = 26.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("First purchase bonus", style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text("Double coins on the ₹449 pack · ends in 23:41:08", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sampleCoinPacks.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { p -> PackTile(p, firstPurchase, Modifier.weight(1f)) { checkout = p } }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, Hairline, RoundedCornerShape(16.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Language, null, tint = BattleBlue)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Get 20% more coins on the web", style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text("ishq.express/coins · same account", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    Icon(Icons.Rounded.OpenInNew, null, tint = InkTertiary)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IshqCard(Modifier.weight(1f).clickable { nav.push(Screen.DailyRewards) }, padding = 14.dp) {
                        Text("📅", fontSize = 22.sp); Text("Free coins", style = MaterialTheme.typography.titleSmall, color = Ink); Text("Daily check-in", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    IshqCard(Modifier.weight(1f).clickable { nav.push(Screen.Vip) }, padding = 14.dp) {
                        Text("👑", fontSize = 22.sp); Text("VIP ${Me.vipTier}", style = MaterialTheme.typography.titleSmall, color = Ink); Text("Coupons & perks", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                }
            }
            item {
                Text("Purchases are processed by Google Play. Coins have no cash value and can't be refunded or transferred.", style = MaterialTheme.typography.bodySmall, color = InkTertiary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    checkout?.let { p ->
        CheckoutSheet(p, firstPurchase, onDismiss = { checkout = null }, onPaid = { total ->
            DemoWallet.coins += total; firstPurchase = false; checkout = null
        })
    }
}

@Composable
private fun PackTile(p: CoinPack, firstPurchase: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val doubled = firstPurchase && p.tag?.startsWith("First") == true
    val tag = if (!firstPurchase && p.tag?.startsWith("First") == true) null else p.tag
    Box(modifier) {
        Column(
            Modifier.fillMaxWidth().tilt3D()
                .then(if (tag != null) Modifier.glass(RoundedCornerShape(18.dp), glow = BrandPink, elevation = 14.dp).neonBorder(RoundedCornerShape(18.dp))
                      else Modifier.glass(RoundedCornerShape(18.dp)))
                .clickable(onClick = onClick).padding(top = 14.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val tier = when { p.coins >= 7_000 -> 5; p.coins >= 3_500 -> 4; p.coins >= 1_400 -> 3; p.coins >= 350 -> 2; else -> 1 }
            CoinStack3D(tier, Modifier.size(70.dp).floating(3.dp, 2_000 + tier * 300))
            Spacer(Modifier.height(4.dp))
            CoinAmount(if (doubled) p.coins * 2 else p.coins, iconSize = 16.dp, style = MaterialTheme.typography.titleLarge)
            if (p.bonus > 0 || doubled) Text(if (doubled) "×2 first purchase" else "+${p.bonus.grouped()} bonus", style = MaterialTheme.typography.labelMedium, color = BrandPink)
            else Text(" ", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(10.dp))
            Text(p.price, style = MaterialTheme.typography.labelLarge, color = Color.White, modifier = Modifier.shadow(8.dp, CircleShape, spotColor = BrandPink).clip(CircleShape).background(BrandBrush.primary)
                .drawWithContent { drawContent(); drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = 0.3f), 0.5f to Color.Transparent)) }.padding(horizontal = 18.dp, vertical = 6.dp))
        }
        if (tag != null) Text(tag, style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-8).dp).clip(CircleShape).background(BrandBrush.deep).padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun CheckoutSheet(p: CoinPack, firstPurchase: Boolean, onDismiss: () -> Unit, onPaid: (Int) -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val doubled = firstPurchase && p.tag?.startsWith("First") == true
    // Coupon picked on the VIP page: percent of base coins, used up on this purchase.
    var coupon by com.live.ishqexpress.data.rememberIntPref("coupon", 0)
    val couponCoins = p.coins * coupon / 100
    val total = (if (doubled) p.coins * 2 else p.coins) + p.bonus + couponCoins
    var state by remember { mutableIntStateOf(0) } // 0 review, 1 processing, 2 done
    LaunchedEffect(state) {
        if (state == 1) { delay(1600); state = 2 }
        if (state == 2) { delay(1200); toast("${total.grouped()} coins added"); coupon = 0; onPaid(total) }
    }
    IshqSheet(onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (state) {
                0 -> {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Shop, null, tint = Success)
                        Spacer(Modifier.width(8.dp))
                        Text("Google Play", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                        Text(Me.handle, style = MaterialTheme.typography.bodySmall, color = InkTertiary)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Hairline)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        CoinIcon(44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${total.grouped()} coins", style = MaterialTheme.typography.titleMedium, color = Ink)
                            Text("ishqexpress", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        Text(p.price, style = MaterialTheme.typography.titleMedium, color = Ink)
                    }
                    Spacer(Modifier.height(12.dp))
                    listOf("Base" to p.coins, "Bonus" to p.bonus, "First purchase" to if (doubled) p.coins else 0, "Coupon +$coupon%" to couponCoins).filter { it.second > 0 }.forEach { (l, v) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Text(l, style = MaterialTheme.typography.bodyMedium, color = InkSecondary, modifier = Modifier.weight(1f))
                            Text("+${v.grouped()}", style = MaterialTheme.typography.bodyMedium, color = Ink)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    MenuRow(Icons.Rounded.CreditCard, "UPI · suman@okhdfc", subtitle = "Change payment method", onClick = { nav.push(Screen.PaymentMethods) }, modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(SurfaceMuted))
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton("Buy · ${p.price}", { state = 1 })
                }
                1 -> {
                    Spacer(Modifier.height(30.dp))
                    CircularProgressIndicator(color = BrandPink)
                    Spacer(Modifier.height(16.dp))
                    Text("Processing payment…", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(40.dp))
                }
                else -> {
                    Spacer(Modifier.height(20.dp))
                    Icon(Icons.Rounded.CheckCircle, null, tint = Success, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Payment successful", style = MaterialTheme.typography.titleLarge, color = Ink)
                    CoinAmount(total, iconSize = 18.dp, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

/* ============================== 60 · Transaction history ============================== */

@Composable
fun TransactionsScreen(onBack: () -> Unit) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val filters = listOf("All", "Coins in", "Coins out")
    ScreenScaffold("Transactions", onBack) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEachIndexed { i, f -> PillChip(f, filter == i, { filter = i }) }
        }
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IshqCard(Modifier.weight(1f), padding = 14.dp) {
                Text("In this month", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                Text("+2,534", style = MaterialTheme.typography.titleLarge, color = Success)
            }
            IshqCard(Modifier.weight(1f), padding = 14.dp) {
                Text("Out this month", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                Text("−3,645", style = MaterialTheme.typography.titleLarge, color = Ink)
            }
        }
        val list = sampleTxs.filter { when (filter) { 1 -> it.amount > 0; 2 -> it.amount < 0; else -> true } }
        LazyColumn {
            items(list) { t ->
                val (icon, color) = when (t.type) {
                    TxType.Purchase -> Icons.Rounded.AddCard to Success
                    TxType.Gift -> Icons.Rounded.CardGiftcard to BrandPink
                    TxType.Subscription -> Icons.Rounded.Star to BrandViolet
                    TxType.Reward -> Icons.Rounded.CalendarMonth to Color(0xFFFF8C5A)
                    TxType.Ticket -> Icons.Rounded.Key to Gold
                    TxType.Refund -> Icons.Rounded.Undo to BattleBlue
                }
                MenuRow(icon, t.title, subtitle = t.time, iconTint = color, iconBg = color.copy(alpha = 0.12f), trailing = {
                    Text(if (t.amount > 0) "+${t.amount.grouped()}" else "−${(-t.amount).grouped()}", style = MaterialTheme.typography.titleSmall, color = if (t.amount > 0) Success else Ink)
                })
            }
        }
    }
}

/* ============================== 61 · VIP / loyalty ============================== */

@Composable
fun VipScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    val nav = LocalNav.current
    val pager = rememberPagerState(initialPage = Me.vipTier - 1) { vipTiers.size }
    ScreenScaffold("VIP", onBack) {
        LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
            item {
                HorizontalPager(pager, contentPadding = PaddingValues(horizontal = 32.dp), pageSpacing = 12.dp) { i ->
                    val t = vipTiers[i]
                    val mine = t.level == Me.vipTier
                    Box(
                        Modifier.fillMaxWidth().aspectRatio(1.6f).clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(t.color, t.color.copy(alpha = 0.6f), BrandNight)))
                            .padding(20.dp)
                    ) {
                        Text("👑", fontSize = 60.sp, modifier = Modifier.align(Alignment.TopEnd).alpha(0.9f))
                        Column {
                            Text("VIP ${t.level}", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                            Text(t.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        }
                        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
                            if (mine && i + 1 < vipTiers.size) {
                                Text("Your tier · 2,640 / ${vipTiers[i + 1].monthlyCoins.grouped()} coins to ${vipTiers[i + 1].name}", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                Spacer(Modifier.height(6.dp))
                                ProgressBar(2_640f / vipTiers[i + 1].monthlyCoins, track = Color.White.copy(alpha = 0.25f), brush = Brush.horizontalGradient(listOf(Color.White, Color.White)))
                            } else {
                                Text("Spend ${t.monthlyCoins.grouped()} coins in 30 days", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            }
                        }
                    }
                }
            }
            item {
                Text("Resets every 30 days · resets on 28 Nov", style = MaterialTheme.typography.bodySmall, color = InkTertiary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(10.dp))
            }
            val t = vipTiers[pager.currentPage]
            item { SectionLabel("${t.name} perks") }
            val perks = listOf(
                Triple(Icons.Rounded.CardGiftcard, "Free weekly gifts", 1),
                Triple(Icons.Rounded.LocalOffer, "Coin coupons (+${t.level * 2}%)", 1),
                Triple(Icons.Rounded.Badge, "VIP badge in chat", 1),
                Triple(Icons.Rounded.AutoAwesome, "Entry effect", 3),
                Triple(Icons.Rounded.VisibilityOff, "Invisible Mode", 6),
                Triple(Icons.Rounded.SupportAgent, "Personal VIP manager", 9),
                Triple(Icons.Rounded.Diamond, "Exclusive VIP gifts", 11),
            )
            items(perks) { (icon, label, from) ->
                val unlocked = t.level >= from
                MenuRow(icon, label, subtitle = if (unlocked) null else "From VIP $from", iconTint = if (unlocked) t.color else InkTertiary, iconBg = if (unlocked) t.color.copy(alpha = 0.12f) else SurfaceMuted,
                    trailing = { Icon(if (unlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock, null, tint = if (unlocked) Success else InkTertiary) })
            }
            item {
                SectionLabel("My coupons")
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    var coupon by com.live.ishqexpress.data.rememberIntPref("coupon", 0)
                    listOf(Triple(6, "+6% coins", "Any pack · 3 days left"), Triple(10, "+10% coins", "₹1,799+ · 6 days left")).forEach { (pct, a, b) ->
                        val on = coupon == pct
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF5A3C0A), Color(0xFF2A1A06))))
                            .then(if (on) Modifier.border(2.dp, Color(0xFFFFC14D), RoundedCornerShape(14.dp)) else Modifier)
                            .clickable { coupon = pct; toast("Coupon $a applied — buy any pack"); nav.push(Screen.CoinStore) }.padding(14.dp)) {
                            Text(a, style = MaterialTheme.typography.titleMedium, color = Color(0xFFFFC14D))
                            Text(if (on) "Selected · used on next purchase" else b, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                    }
                }
            }
        }
    }
}

/* ============================== 62 · Daily rewards ============================== */

@Composable
fun DailyRewardsScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val ctx = androidx.compose.ui.platform.LocalContext.current
    // One check-in per calendar day, remembered across restarts.
    val todayKey = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }
    var lastCheckIn by com.live.ishqexpress.data.rememberStringPref("checkIn", "")
    val claimedToday = lastCheckIn == todayKey
    val streak = 4
    val rewards = listOf(5, 10, 15, 20, 30, 40, 100)
    ScreenScaffold("Daily rewards", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFFFF8C5A), Color(0xFFFF2E8B)))) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔥 ${if (claimedToday) streak + 1 else streak}-day streak", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text("Check in 7 days in a row for the 100-coin chest", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rewards.chunked(4).forEachIndexed { r, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEachIndexed { i, coins ->
                                val day = r * 4 + i
                                val claimed = day < streak || (day == streak && claimedToday)
                                val today = day == streak && !claimedToday
                                Column(
                                    Modifier.weight(if (day == 6) 2.1f else 1f).clip(RoundedCornerShape(16.dp))
                                        .then(if (today) Modifier.background(BrandBrush.primary) else Modifier.background(if (claimed) PinkTint else SurfaceMuted))
                                        .clickable(enabled = today) { lastCheckIn = todayKey; DemoWallet.coins += coins; toast("+$coins coins claimed") }
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Day ${day + 1}", style = MaterialTheme.typography.labelMedium, color = if (today) Color.White else InkSecondary)
                                    Spacer(Modifier.height(6.dp))
                                    if (claimed) Icon(Icons.Rounded.CheckCircle, null, tint = BrandPink, modifier = Modifier.size(28.dp))
                                    else Text(if (day == 6) "🎁" else "🪙", fontSize = 26.sp)
                                    Spacer(Modifier.height(6.dp))
                                    Text("+$coins", style = MaterialTheme.typography.titleSmall, color = if (today) Color.White else Ink)
                                }
                            }
                        }
                    }
                }
            }
            item {
                PrimaryButton(if (claimedToday) "Come back tomorrow" else "Check in · +${rewards[streak]} coins", { lastCheckIn = todayKey; DemoWallet.coins += rewards[streak]; toast("+${rewards[streak]} coins claimed") }, enabled = !claimedToday)
            }
            item { Text("More ways to earn", style = MaterialTheme.typography.titleMedium, color = Ink) }
            val tasks = listOf(
                Triple("Watch lives for 10 minutes", "7 / 10 min", 10),
                Triple("Send a gift", "0 / 1", 15),
                Triple("Follow 3 new creators", "1 / 3", 10),
                Triple("Share a live", "0 / 1", 5),
                Triple("Invite a friend", "They get 50 coins too", 50),
            )
            items(tasks) { (title, progress, coins) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, Hairline, RoundedCornerShape(16.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text(progress, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    CoinAmount(coins)
                    Spacer(Modifier.width(10.dp))
                    SmallOutlineButton("Go", {
                        when {
                            title.contains("Follow", true) -> nav.push(Screen.Search)
                            title.contains("Share", true) || title.contains("Watch", true) || title.contains("gift", true) -> nav.push(Screen.LiveViewer, 1)
                            title.contains("Invite", true) -> shareText(ctx, "Join me on ishqexpress — we both get 50 coins! ${AppLinks.invite("SUMAN-A2")}")
                            else -> nav.popToRoot()
                        }
                    }, height = 32.dp, color = BrandPink)
                }
            }
        }
    }
}

/* ============================== 63 · Creator earnings ============================== */

@Composable
fun EarningsScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    var range by rememberSaveable { mutableIntStateOf(1) }
    val days = listOf(820, 1_240, 610, 2_100, 1_780, 3_020, 2_460)
    ScreenScaffold("Earnings", onBack, actions = { TopAction(Icons.Rounded.History, "Payout history") { nav.push(Screen.PayoutHistory) } }) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth()) {
                    Column {
                        Text("Diamond balance", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DiamondIcon(30.dp); Spacer(Modifier.width(8.dp))
                            Text(Me.diamonds.grouped(), style = MaterialTheme.typography.displaySmall, color = Color.White)
                        }
                        Text("≈ ₹${(Me.diamonds * 0.8).toInt().grouped()} · minimum withdrawal 5,000", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                        Spacer(Modifier.height(14.dp))
                        Box(Modifier.clip(CircleShape).background(Color.White).clickable { nav.push(Screen.Payout) }.padding(horizontal = 20.dp, vertical = 10.dp)) {
                            Text("Withdraw", style = MaterialTheme.typography.labelLarge, color = BrandPink)
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Today", "7 days", "30 days").forEachIndexed { i, r -> PillChip(r, range == i, { range = i }) } }
            }
            item {
                IshqCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("By day", style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
                        CoinAmount(days.sum(), diamond = true, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(Modifier.height(14.dp))
                    val max = days.max()
                    Row(Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                        days.forEachIndexed { i, d ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(d.compact(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = InkTertiary)
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.fillMaxWidth().height((110 * d / max).dp).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).background(if (i == days.lastIndex) BrandBrush.primary else Brush.verticalGradient(listOf(PinkTintStrong, PinkTintStrong))))
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth()) { listOf("W", "T", "F", "S", "S", "M", "T").forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = InkTertiary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)) } }
                }
            }
            item {
                IshqCard(Modifier.fillMaxWidth()) {
                    Text("By source", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(10.dp))
                    val sources = listOf(Triple("Live gifts", 7_420, BrandPink), Triple("Subscriptions", 2_840, BrandViolet), Triple("Private shows", 1_150, BrandOrchid), Triple("Premium tickets", 620, Gold))
                    val total = sources.sumOf { it.second }
                    Row(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape)) {
                        sources.forEach { (_, v, c) -> Box(Modifier.weight(v.toFloat()).fillMaxHeight().background(c)) }
                    }
                    Spacer(Modifier.height(12.dp))
                    sources.forEach { (l, v, c) ->
                        Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(c))
                            Text("  $l", style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.weight(1f))
                            Text("${v * 100 / total}%  ", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
                            CoinAmount(v, diamond = true)
                        }
                    }
                }
            }
            item {
                MenuRow(Icons.Rounded.TrendingUp, "Creator level C4", subtitle = "Earn 11,360 more diamonds this month for C5", onClick = { nav.push(Screen.CreatorLevel) },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(SurfaceMuted))
            }
        }
    }
}

/* ============================== 64 · Payout setup + KYC ============================== */

@Composable
fun PayoutScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var method by rememberSaveable { mutableIntStateOf(0) }
    var amount by rememberSaveable { mutableFloatStateOf(10_000f) }
    var legalName by rememberSaveable { mutableStateOf("") }
    var pan by rememberSaveable { mutableStateOf("") }
    var account by rememberSaveable { mutableStateOf("") }
    var ifsc by rememberSaveable { mutableStateOf("") }
    var upi by rememberSaveable { mutableStateOf("") }
    val kyc = com.live.ishqexpress.data.Session.kyc
    // Format checks only; the server verifies PAN/bank/UPI with the provider (penny drop, VPA lookup).
    val panOk = Regex("[A-Z]{5}[0-9]{4}[A-Z]").matches(pan)
    val ifscOk = Regex("[A-Z]{4}0[A-Z0-9]{6}").matches(ifsc)
    val accountOk = account.length in 9..18
    val upiOk = Regex("[a-zA-Z0-9.\\-_]{2,}@[a-zA-Z]{2,}").matches(upi)
    val stepValid = when (step) {
        0 -> legalName.trim().length >= 3 && panOk && "doc" in kyc && "selfie" in kyc
        1 -> if (method == 0) accountOk && ifscOk else upiOk
        else -> true
    }
    val steps = listOf("Verify ID", "Payout account", "Withdraw")
    val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline, focusedLabelColor = BrandPink)

    ScreenScaffold("Withdraw", onBack) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            steps.forEachIndexed { i, s ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(28.dp).clip(CircleShape).then(if (i <= step) Modifier.background(BrandBrush.primary) else Modifier.background(Hairline)), contentAlignment = Alignment.Center) {
                        if (i < step) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        else Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = if (i == step) Color.White else InkTertiary)
                    }
                    Text(s, style = MaterialTheme.typography.labelSmall, color = if (i <= step) Ink else InkTertiary, modifier = Modifier.padding(top = 4.dp))
                }
                if (i < steps.lastIndex) Box(Modifier.weight(1f).padding(bottom = 16.dp, start = 4.dp, end = 4.dp).height(2.dp).background(if (i < step) BrandPink else Hairline))
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(20.dp)) {
            when (step) {
                0 -> {
                    Text("Verify your identity", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text("Required once by law before your first payout. Your documents are encrypted.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(legalName, { legalName = it.take(60) }, Modifier.fillMaxWidth(), label = { Text("Full legal name") }, supportingText = { Text("As on your PAN card") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(pan, { pan = it.uppercase().filter(Char::isLetterOrDigit).take(10) }, Modifier.fillMaxWidth(), label = { Text("PAN number") }, placeholder = { Text("ABCDE1234F") },
                        isError = pan.length == 10 && !panOk, supportingText = { if (pan.length == 10 && !panOk) Text("Check the PAN format") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters), singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors)
                    Spacer(Modifier.height(16.dp))
                    listOf(Icons.Rounded.Badge to "Upload Aadhaar / passport (front & back)", Icons.Rounded.Face to "Take a selfie for liveness check").forEachIndexed { kycStep, (icon, t) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(14.dp)).border(1.5.dp, Hairline, RoundedCornerShape(14.dp)).clickable { nav.push(Screen.KycCapture, kycStep) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            val done = (if (kycStep == 0) "doc" else "selfie") in kyc
                            Icon(icon, null, tint = BrandPink)
                            Spacer(Modifier.width(12.dp))
                            Text(t, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                            if (done) Icon(Icons.Rounded.CheckCircle, "Done", tint = Success) else Icon(Icons.Rounded.ChevronRight, null, tint = InkTertiary)
                        }
                    }
                }
                1 -> {
                    Text("Where should we send money?", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Spacer(Modifier.height(14.dp))
                    listOf(Icons.Rounded.AccountBalance to "Bank account", Icons.Rounded.QrCode to "UPI").forEachIndexed { i, (icon, l) ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(14.dp)).background(if (method == i) PinkTint else Canvas)
                                .border(if (method == i) 1.5.dp else 1.dp, if (method == i) BrandPink else Hairline, RoundedCornerShape(14.dp)).clickable { method = i }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null, tint = if (method == i) BrandPink else InkSecondary)
                            Spacer(Modifier.width(12.dp))
                            Text(l, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                            RadioButton(method == i, { method = i }, colors = RadioButtonDefaults.colors(selectedColor = BrandPink))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (method == 0) {
                        OutlinedTextField(account, { account = it.filter(Char::isDigit).take(18) }, Modifier.fillMaxWidth(), label = { Text("Account number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(ifsc, { ifsc = it.uppercase().filter(Char::isLetterOrDigit).take(11) }, Modifier.fillMaxWidth(), label = { Text("IFSC code") }, placeholder = { Text("HDFC0001234") },
                            isError = ifsc.length == 11 && !ifscOk, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters), singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors)
                    } else {
                        OutlinedTextField(upi, { upi = it.trim().take(50) }, Modifier.fillMaxWidth(), label = { Text("UPI ID") }, placeholder = { Text("name@bank") },
                            isError = upi.contains('@') && !upiOk, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors)
                    }
                }
                else -> {
                    Text("How much?", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Spacer(Modifier.height(16.dp))
                    IshqCard(Modifier.fillMaxWidth(), background = PinkTint) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CoinAmount((amount / 100).toInt() * 100, diamond = true, iconSize = 22.dp, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                            Text("≈ ₹${((amount / 100).toInt() * 80).grouped()}", style = MaterialTheme.typography.titleMedium, color = Ink)
                        }
                        Slider(amount, { amount = it }, valueRange = 5_000f..Me.diamonds.toFloat(), colors = SliderDefaults.colors(thumbColor = BrandPink, activeTrackColor = BrandPink, inactiveTrackColor = Color.White))
                        Row { Text("Min 5,000", style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.weight(1f)); Text("Max ${Me.diamonds.grouped()}", style = MaterialTheme.typography.bodySmall, color = InkSecondary) }
                    }
                    Spacer(Modifier.height(14.dp))
                    listOf("Rate" to "1 diamond = ₹0.80", "Processing fee" to "₹0", "TDS (1%)" to "₹${((amount / 100).toInt() * 0.8).toInt().grouped()}", "Arrives" to "In 3–5 business days").forEach { (k, v) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Text(k, style = MaterialTheme.typography.bodyMedium, color = InkSecondary, modifier = Modifier.weight(1f))
                            Text(v, style = MaterialTheme.typography.bodyMedium, color = Ink)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    MenuRow(if (method == 0) Icons.Rounded.AccountBalance else Icons.Rounded.QrCode, if (method == 0) "Bank ••${account.takeLast(4)} · $ifsc" else upi, subtitle = "Payout account", modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(SurfaceMuted))
                }
            }
        }
        Box(Modifier.padding(20.dp).navigationBarsPadding()) {
            PrimaryButton(if (step < 2) "Continue" else "Request withdrawal", {
                if (step < 2) step++ else { toast("Withdrawal requested"); nav.replace(Screen.PayoutHistory) }
            }, enabled = stepValid)
        }
    }
}

/* ============================== 65 · Payout history ============================== */

@Composable
fun PayoutHistoryScreen(onBack: () -> Unit) {
    ScreenScaffold("Payout history", onBack) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IshqCard(Modifier.weight(1f), padding = 14.dp) {
                Text("Paid out (2026)", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                Text("₹28,600", style = MaterialTheme.typography.titleLarge, color = Ink)
            }
            IshqCard(Modifier.weight(1f), padding = 14.dp) {
                Text("Pending", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                Text("₹8,240", style = MaterialTheme.typography.titleLarge, color = Gold)
            }
        }
        LazyColumn {
            items(samplePayouts) { p ->
                val (color, icon) = when (p.status) {
                    "Paid" -> Success to Icons.Rounded.CheckCircle
                    "Processing" -> Gold to Icons.Rounded.Schedule
                    else -> LiveRed to Icons.Rounded.Cancel
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)).padding(7.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.amount, style = MaterialTheme.typography.titleMedium, color = Ink)
                        Text("${p.diamonds.grouped()} 💎 · ${p.method}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        if (p.status == "Rejected") Text("Name on UPI didn't match KYC", style = MaterialTheme.typography.bodySmall, color = LiveRed)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(p.status, style = MaterialTheme.typography.labelMedium, color = color)
                        Text(p.date, style = MaterialTheme.typography.bodySmall, color = InkTertiary)
                    }
                }
                HorizontalDivider(Modifier.padding(start = 70.dp), color = Hairline)
            }
        }
    }
}

/* ============================== 66 · Leaderboards ============================== */

@Composable
fun LeaderboardsScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    var board by rememberSaveable { mutableIntStateOf(0) }
    var period by rememberSaveable { mutableIntStateOf(1) }
    val list = remember(board, period) { if (board == 0) sampleLeaders else sampleLeaders.shuffled(kotlin.random.Random(period + 7)) }

    Column(Modifier.fillMaxSize().background(NightBrush).statusBarsPadding()) {
        DarkSystemBars()
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
            Text("Rankings", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f).padding(start = 8.dp))
            GlassButton(Icons.Rounded.HelpOutline, "Rules", { nav.push(Screen.RankingRules) })
        }
        UnderlineTabs(listOf("Top creators", "Top gifters"), board, { board = it }, dark = true)
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).padding(3.dp)) {
            listOf("Hourly", "Daily", "Weekly", "Monthly").forEachIndexed { i, p ->
                Text(p, style = MaterialTheme.typography.labelMedium, color = if (period == i) Color(0xFF1A1029) else Color.White.copy(alpha = 0.75f), textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).clip(CircleShape).then(if (period == i) Modifier.background(Color.White) else Modifier).clickable { period = i }.padding(vertical = 8.dp))
            }
        }
        // Podium
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
            listOf(1, 0, 2).forEach { rank ->
                val e = list[rank]; val c = creatorById(e.creator)
                Column(Modifier.weight(1f).clickable { nav.push(Screen.OtherProfile, c.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(listOf("👑", "🥈", "🥉")[rank], fontSize = if (rank == 0) 28.sp else 22.sp)
                    Avatar(c.imageUrl, if (rank == 0) 84.dp else 66.dp, Modifier.border(3.dp, listOf(Gold, Color(0xFFB8C1CC), Color(0xFFE59A62))[rank], CircleShape))
                    Spacer(Modifier.height(6.dp))
                    Text(c.name.substringBefore(' '), style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1)
                    CoinAmount(e.score, diamond = board == 0, iconSize = 12.dp, color = Gold)
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().padding(horizontal = 6.dp).height(listOf(70, 50, 36)[rank].dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Text("${rank + 1}", style = MaterialTheme.typography.headlineMedium, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(Canvas), contentPadding = PaddingValues(vertical = 10.dp)) {
            itemsIndexed(list.drop(3)) { i, e ->
                val c = creatorById(e.creator)
                Row(Modifier.fillMaxWidth().clickable { nav.push(Screen.OtherProfile, c.id) }.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 4}", style = MaterialTheme.typography.titleMedium, color = InkTertiary, modifier = Modifier.width(30.dp))
                    Avatar(c.imageUrl, 44.dp, live = board == 0 && c.id % 2 == 1)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        NameRow(c.name, c.verified)
                        LevelBadge(15 + c.id * 2)
                    }
                    CoinAmount(e.score, diamond = board == 0)
                }
            }
            item {
                Row(Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PinkTint).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(Me.avatarUrl, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("You · #248", style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text("2,140 more to enter the top 100", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                }
            }
        }
    }
}

/* ============================== Extra · Agency ============================== */

@Composable
fun AgencyScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    ScreenScaffold("Agency", onBack, actions = { TopAction(Icons.Rounded.PersonAdd, "Invite creator") { shareText(ctx, "Join my agency on ishqexpress and start earning from your lives: ${AppLinks.invite("AGENCY-A2")}") } }) {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                GradientCard(Modifier.padding(16.dp).fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFF3B6FE0), Color(0xFF7A2BE8)))) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A2", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 2.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Suman Talent Co.", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Text("Commission 10% · 14 creators", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("Creator withdrawals this month  ₹3.4L / ₹5L for A3", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        Spacer(Modifier.height(6.dp))
                        ProgressBar(0.68f, track = Color.White.copy(alpha = 0.25f), brush = Brush.horizontalGradient(listOf(Color.White, Color.White)))
                    }
                }
            }
            item {
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IshqCard(Modifier.weight(1f), padding = 12.dp) { Stat("₹34,120", "Commission", Modifier.fillMaxWidth()) }
                    IshqCard(Modifier.weight(1f), padding = 12.dp) { Stat("11", "Active", Modifier.fillMaxWidth()) }
                    IshqCard(Modifier.weight(1f), padding = 12.dp) { Stat("3", "Pending", Modifier.fillMaxWidth()) }
                }
            }
            item { Spacer(Modifier.height(10.dp)); UnderlineTabs(listOf("Creators", "Referrals", "Earnings", "Tiers"), tab, { tab = it }) }
            when (tab) {
                0 -> items(sampleCreators.take(8)) { c ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(c.imageUrl, 46.dp, live = c.id % 2 == 1)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            NameRow(c.name, c.verified)
                            Text("C${c.id % 6 + 2} · ${20 + c.id * 3} h live this month", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        CoinAmount(c.viewers * 3, diamond = true)
                    }
                }
                1 -> {
                    item {
                        Row(Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(PinkTint).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Your referral code", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                Text("SUMAN-A2", style = MaterialTheme.typography.titleLarge, color = Ink)
                            }
                            SmallGradientButton("Share", { copyText(ctx, "SUMAN-A2"); shareText(ctx, "Join ishqexpress with my code SUMAN-A2 ${AppLinks.invite("SUMAN-A2")}") }, icon = Icons.Rounded.Share)
                        }
                    }
                    items(listOf(Triple(2, "Pending KYC", Gold), Triple(11, "Joined 28 Oct", Success), Triple(8, "Invited 26 Oct", InkTertiary), Triple(12, "Joined 12 Oct", Success))) { (id, status, color) ->
                        val c = creatorById(id)
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(c.imageUrl, 42.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                            Text(status, style = MaterialTheme.typography.labelMedium, color = color)
                        }
                    }
                }
                2 -> itemsIndexed(listOf("November 2026" to "₹12,480 · in progress", "October 2026" to "₹34,120 · paid 5 Nov", "September 2026" to "₹28,900 · paid 5 Oct", "August 2026" to "₹19,300 · paid 5 Sep")) { monthIndex, (m, v) ->
                    MenuRow(Icons.Rounded.CalendarMonth, m, subtitle = v, iconTint = BattleBlue, iconBg = BattleBlue.copy(alpha = 0.1f), onClick = { nav.push(Screen.AgencyStatement, monthIndex) })
                }
                else -> {
                    val tiers = listOf(Triple("A1", "Up to ₹2L / month", "8% commission"), Triple("A2", "₹2L – ₹5L", "10% · agency ribbon"), Triple("A3", "₹5L – ₹15L", "12% · custom gift"), Triple("A4", "₹15L+", "Up to 15% · dedicated manager"))
                    itemsIndexed(tiers) { i, (t, req, perk) ->
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                                .background(if (i == 1) VioletTint else SurfaceMuted).border(if (i == 1) 1.5.dp else 0.dp, if (i == 1) BrandViolet else Color.Transparent, RoundedCornerShape(16.dp)).padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t, style = MaterialTheme.typography.titleLarge, color = BrandViolet, modifier = Modifier.width(48.dp))
                            Column(Modifier.weight(1f)) {
                                Text(perk, style = MaterialTheme.typography.titleSmall, color = Ink)
                                Text("Creator withdrawals $req", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                            if (i == 1) Text("YOU", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.clip(CircleShape).background(BrandViolet).padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
            }
        }
    }
}
