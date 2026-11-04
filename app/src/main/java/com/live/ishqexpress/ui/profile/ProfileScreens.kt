package com.live.ishqexpress.ui.profile

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
import com.live.ishqexpress.ui.explore.MiniLiveTile
import com.live.ishqexpress.ui.live.ReportSheet
import com.live.ishqexpress.ui.social.FanTierSheet
import com.live.ishqexpress.ui.social.PostCard

/*
 * 52 My profile · 53 Other profile · 54 Edit profile · 55 Followers / following
 * 56 Fan tiers sheet (FanTierSheet in social) · 57 My subscriptions
 * Extra: User level · Crowns · Creator level
 */

/* ============================== 52 · My profile ============================== */

@Composable
fun MyProfileScreen(onBack: (() -> Unit)? = null) {
    val nav = LocalNav.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showQr by remember { mutableStateOf(false) }
    if (showQr) MyQrSheet { showQr = false }
    LazyColumn(Modifier.fillMaxSize().appBackground()) {
        item {
            Box(Modifier.fillMaxWidth()) {
                Box(
                    Modifier.fillMaxWidth().height(230.dp)
                        .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            // Fade the banner out so it melts into the night canvas
                            drawRect(Brush.verticalGradient(0.45f to Color.Black, 1f to Color.Transparent), blendMode = androidx.compose.ui.graphics.BlendMode.DstIn)
                        }
                        .background(BrandBrush.deep)
                ) {
                    Box(Modifier.align(Alignment.TopEnd).offset(60.dp, (-40).dp).size(200.dp).floating(6.dp, 4_000).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)))
                    Box(Modifier.align(Alignment.CenterStart).offset((-50).dp, 0.dp).size(140.dp).floating(5.dp, 3_200).clip(CircleShape).background(NeonCyan.copy(alpha = 0.14f)))
                    Sparkles(count = 7)
                }
                Row(Modifier.statusBarsPadding().fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
                    if (onBack != null) { TopAction(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White, onClick = onBack); Spacer(Modifier.weight(1f)) }
                    TopAction(Icons.Rounded.QrCode2, "My QR", tint = Color.White) { showQr = true }
                    TopAction(Icons.Outlined.Settings, "Settings", tint = Color.White) { nav.push(Screen.Settings) }
                }
                Column(Modifier.padding(top = 120.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Box(Modifier.size(104.dp).shadow(22.dp, CircleShape, ambientColor = NeonPink, spotColor = NeonViolet).neonBorder(CircleShape, 3.dp).padding(3.dp)) {
                            Avatar(Me.avatarUrl, 98.dp)
                        }
                        Icon(Icons.Rounded.Edit, "Edit", tint = Color.White, modifier = Modifier.align(Alignment.BottomEnd).size(30.dp).clip(CircleShape).background(BrandPink).border(2.dp, Canvas, CircleShape).clickable { nav.push(Screen.EditProfile) }.padding(6.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(Me.name, style = MaterialTheme.typography.headlineSmall, color = Ink)
                    val bio = com.live.ishqexpress.data.Session.bio
                    if (bio.isNotBlank()) Text(bio, style = MaterialTheme.typography.bodySmall, color = InkSecondary, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.padding(horizontal = 32.dp, vertical = 2.dp))
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        LevelBadge(Me.level, Modifier.clickable { nav.push(Screen.UserLevel) })
                        VipBadge(Me.vipTier, Modifier.clickable { nav.push(Screen.Vip) })
                        Text("${Me.id} · 🇮🇳", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                        Stat(Me.followers.compact(), "Followers", Modifier.weight(1f).clickable { nav.push(Screen.Follows, 0) })
                        Stat(Me.following.compact(), "Following", Modifier.weight(1f).clickable { nav.push(Screen.Follows, 1) })
                        Stat("${Me.fans}", "Fans", Modifier.weight(1f).clickable { nav.push(Screen.Follows, 2) })
                    }
                }
            }
        }
        // Wallet
        item {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WalletTile("Coins", DemoWallet.coins, false, "Recharge", Modifier.weight(1f), listOf(Color(0xFF5A3C0A), Color(0xFF2A1A06))) { nav.push(Screen.CoinStore) }
                WalletTile("Diamonds", Me.diamonds, true, "Withdraw", Modifier.weight(1f), listOf(Color(0xFF5A1850), Color(0xFF2A1A55))) { nav.push(Screen.Earnings) }
            }
        }
        // Shortcuts grid
        item {
            data class S(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val color: Color, val go: Screen)
            val shortcuts = listOf(
                S(Icons.Rounded.CalendarMonth, "Daily rewards", Color(0xFFFF8C5A), Screen.DailyRewards),
                S(Icons.Rounded.WorkspacePremium, "VIP", Gold, Screen.Vip),
                S(Icons.Rounded.TrendingUp, "My level", BattleBlue, Screen.UserLevel),
                S(Icons.Rounded.EmojiEvents, "Crowns", Color(0xFFC236D9), Screen.Crowns),
                S(Icons.Rounded.Leaderboard, "Rankings", Color(0xFFFF2E8B), Screen.Leaderboards),
                S(Icons.Rounded.Star, "Subscriptions", BrandViolet, Screen.Subscriptions),
                S(Icons.Rounded.Videocam, "Creator center", Color(0xFF12B76A), Screen.CreatorLevel),
                S(Icons.Rounded.Business, "Agency", Color(0xFF5B7BEA), Screen.Agency),
                S(Icons.Rounded.ReceiptLong, "Transactions", InkSecondary, Screen.Transactions),
                S(Icons.Rounded.PhotoLibrary, "Media packs", BrandOrchid, Screen.MediaPacks),
                S(Icons.Rounded.Newspaper, "News", Color(0xFFFF6A88), Screen.News),
                S(Icons.Rounded.SupportAgent, "Help", Color(0xFF0BA5A5), Screen.Help),
            )
            IshqCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), padding = 8.dp) {
                shortcuts.chunked(4).forEach { row ->
                    Row {
                        row.forEach { s ->
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable { nav.push(s.go) }.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                IconTile3D(s.icon, s.color, size = 44.dp, glyph = 22.dp)
                                Spacer(Modifier.height(7.dp))
                                FitText(s.label, MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp), Modifier.padding(horizontal = 2.dp), color = Ink, minSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)); UnderlineTabs(listOf("Posts", "Replays", "Badges"), tab, { tab = it }) }
        when (tab) {
            0 -> item { PostGrid(MyPosts.items.mapNotNull { it.media } + samplePosts.mapNotNull { it.image }.map { photo(it + 50, 300, 300) }) }
            1 -> item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Sunday unplugged" to "1:12:48", "Diwali special" to "58:21", "PK vs Rohan" to "10:00").forEachIndexed { i, (t, d) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(width = 110.dp, height = 70.dp).clip(RoundedCornerShape(12.dp))) {
                                AsyncImage(photo(700 + i, 220, 140), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                Text(d, style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 4.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t, style = MaterialTheme.typography.titleSmall, color = Ink)
                                Text("${(i + 1) * 1_240} views · ${i + 2} days ago", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                        }
                    }
                }
            }
            else -> item { BadgeGrid() }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun WalletTile(label: String, amount: Int, diamond: Boolean, cta: String, modifier: Modifier, colors: List<Color>, onClick: () -> Unit) {
    Column(
        modifier.tilt3D().shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = colors.first(), spotColor = if (diamond) BrandPink else Gold)
            .clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(colors))
            .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.04f))), RoundedCornerShape(18.dp))
            .shine(4_500, 0.14f).clickable(onClick = onClick).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (diamond) DiamondIcon(22.dp) else CoinIcon(22.dp)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, color = InkSecondary)
        }
        Spacer(Modifier.height(6.dp))
        Text(amount.grouped(), style = MaterialTheme.typography.headlineSmall, color = Ink)
        Text("$cta ›", style = MaterialTheme.typography.labelMedium, color = if (diamond) Color(0xFFFF8CC6) else Color(0xFFFFC14D))
    }
}

@Composable
private fun PostGrid(images: List<String>) {
    Column(Modifier.padding(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        images.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { AsyncImage(it, null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).aspectRatio(1f).background(SurfaceMuted)) }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BadgeGrid() {
    val badges = listOf("🔥" to "7-day streak", "🎁" to "First gift", "💎" to "Big spender", "🎤" to "First live", "👑" to "Top 10 gifter", "🌙" to "Night owl", "🤝" to "50 friends", "🏆" to "Battle winner")
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        badges.chunked(4).forEachIndexed { r, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { i, (e, l) ->
                    val earned = r * 4 + i < 5
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(if (earned) PinkTint else SurfaceMuted).padding(vertical = 12.dp).alpha(if (earned) 1f else 0.4f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(e, fontSize = 28.sp)
                        Text(l, style = MaterialTheme.typography.labelSmall, color = Ink, textAlign = TextAlign.Center, maxLines = 2)
                    }
                }
            }
        }
    }
}

/* ============================== 53 · Other profile ============================== */

@Composable
fun OtherProfileScreen(creatorId: Int, onBack: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val c = creatorById(creatorId)
    var following by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showTiers by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    val liked = remember { mutableStateListOf<Int>() }
    val isLive = c.id % 2 == 1

    Box(Modifier.fillMaxSize().appBackground()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                Box {
                    AsyncImage(photo(c.id + 900, 800, 500), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(220.dp).background(Brush.linearGradient(c.tint)))
                    Box(Modifier.fillMaxWidth().height(220.dp).background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.35f), 0.4f to Color.Transparent)))
                    Column(Modifier.padding(top = 160.dp).fillMaxWidth().padding(horizontal = 20.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Avatar(c.imageUrl, 100.dp, Modifier.border(4.dp, Canvas, CircleShape).clickable { if (isLive) nav.push(Screen.LiveViewer, c.id) }, live = isLive)
                            Spacer(Modifier.width(8.dp))
                            // Shares the row with the avatar: each stat gets an equal slice and shrinks to fit.
                            Row(Modifier.weight(1f).padding(bottom = 6.dp)) {
                                Stat((c.viewers * 7).compact(), "Followers", Modifier.weight(1f).clickable { nav.push(Screen.Follows, 0) })
                                Stat("${c.id * 31}", "Following", Modifier.weight(1f))
                                Stat((c.viewers * 15).compact(), "Diamonds", Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        NameRow(c.name, c.verified, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("C${(c.id % 6) + 3}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White, modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(Success).padding(horizontal = 5.dp, vertical = 1.5.dp))
                            LevelBadge(20 + c.id)
                            Text("${c.flag} ${c.city} · ID ${80_000 + c.id * 137}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("${c.tag.removePrefix("#")} creator ✨ Live every night at 9 PM. Gifts make me smile!", style = MaterialTheme.typography.bodyMedium, color = Ink)
                    }
                }
            }
            if (isLive) item {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BrandBrush.primary).clickable { nav.push(Screen.LiveViewer, c.id) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveDot(8, Color.White)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Live now", style = MaterialTheme.typography.titleSmall, color = Color.White)
                        Text("${c.viewers.compact()} watching · ${c.tag}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                    Text("Watch ›", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
            // Top fans
            item {
                SectionLabel("Top fans")
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(sampleViewers.take(6)) { i, v ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                Avatar(avatar(v.img), 50.dp, ring = i == 0)
                                if (i < 3) Text(listOf("🥇", "🥈", "🥉")[i], fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomEnd))
                            }
                            Text(v.name, style = MaterialTheme.typography.labelSmall, color = Ink, maxLines = 1)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(10.dp)); UnderlineTabs(listOf("Posts", "Replays", "About"), tab, { tab = it }) }
            when (tab) {
                0 -> items(samplePosts.filter { it.creator == c.id }.ifEmpty { samplePosts.take(2).map { it.copy(creator = c.id) } }, key = { it.id }) { p ->
                    PostCard(p, p.id in liked, { if (p.id in liked) liked.remove(p.id) else liked.add(p.id) }, { nav.push(Screen.PostDetail, p.id) })
                }
                1 -> item {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        sampleCreators.filter { it.id != c.id }.take(2).forEach { MiniLiveTile(it.copy(name = c.name, photo = c.photo), Modifier.weight(1f)) { nav.push(Screen.Replay, c.id) } }
                    }
                }
                else -> item {
                    Column(Modifier.padding(horizontal = 4.dp)) {
                        MenuRow(Icons.Rounded.Language, "Languages", subtitle = "Hindi, English, Punjabi")
                        MenuRow(Icons.Rounded.Cake, "Joined", subtitle = "March 2024")
                        MenuRow(Icons.Rounded.Interests, "Interests", subtitle = "Music, Dance, Travel")
                        MenuRow(Icons.Rounded.Business, "Agency", subtitle = "Rangmanch Talent (A3)")
                    }
                }
            }
        }

        // Top bar
        Row(Modifier.statusBarsPadding().fillMaxWidth().padding(8.dp)) {
            GlassButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
            Spacer(Modifier.weight(1f))
            GlassButton(Icons.Rounded.MoreVert, "More", { showMenu = true })
        }

        // Action bar
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Canvas).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            if (following) SmallOutlineButton("Following", { following = false }, Modifier.weight(1f), height = 48.dp, icon = Icons.Rounded.Check)
            else SmallGradientButton("Follow", { following = true }, Modifier.weight(1f), height = 48.dp, icon = Icons.Rounded.Add)
            SmallOutlineButton("Fan ⭐", { showTiers = true }, Modifier.weight(1f), height = 48.dp, color = BrandPink)
            CircleBtn(Icons.Outlined.ChatBubbleOutline) { nav.push(Screen.ChatThread, sampleConversations.firstOrNull { it.creator == c.id }?.id ?: 1) }
            CircleBtn(Icons.Rounded.Videocam) { nav.push(Screen.VideoCall, c.id) }
        }
    }

    if (showTiers) FanTierSheet(c) { showTiers = false }
    if (showMenu) IshqSheet({ showMenu = false }) {
        MenuRow(Icons.Rounded.Share, "Share profile", onClick = { showMenu = false; shareText(ctx, "Check out ${c.name} on ishqexpress ${AppLinks.profile(c.id)}") })
        MenuRow(Icons.Rounded.NotificationsActive, "Live notifications", trailing = { IshqSwitch(true) {} })
        MenuRow(Icons.Rounded.Block, "Block", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = { showMenu = false; com.live.ishqexpress.data.Session.block(c.id); toast("${c.name} blocked"); onBack() })
        MenuRow(Icons.Rounded.Flag, "Report", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = { showMenu = false; showReport = true })
    }
    if (showReport) ReportSheet(c.name, { showReport = false }, { showReport = false; toast("Report sent") })
}

@Composable
private fun CircleBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).clip(CircleShape).border(1.dp, Hairline, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(22.dp))
    }
}

/* ============================== 54 · Edit profile ============================== */

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    val session = com.live.ishqexpress.data.Session
    var name by rememberSaveable { mutableStateOf(Me.name) }
    var bio by rememberSaveable { mutableStateOf(session.bio) }
    var city by rememberSaveable { mutableStateOf(session.city) }
    val langs = remember { mutableStateListOf(*session.languages.toTypedArray()) }
    val photos = remember { mutableStateListOf(*session.photos.ifEmpty { listOf(Me.avatarUrl) }.toTypedArray()) }
    var replaceAt by remember { mutableIntStateOf(-1) }
    val pick = rememberImagePicker { uri -> if (replaceAt in photos.indices) photos[replaceAt] = uri else if (photos.size < 6) photos.add(uri); replaceAt = -1 }
    val save = {
        if (name.trim().length < 2) toast("Name needs at least 2 letters")
        else { session.updateProfile(name, bio, city, photos.toList(), langs.toList()); toast("Profile saved"); onBack() }
    }
    val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline, focusedLabelColor = BrandPink)

    ScreenScaffold("Edit profile", onBack, actions = { Text("Save", style = MaterialTheme.typography.titleSmall, color = BrandPink, modifier = Modifier.clickable { save() }.padding(12.dp)) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 20.dp)) {
            Text("Photos", style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.padding(vertical = 8.dp))
            Text("The first photo is your avatar. Add up to 6.", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            Spacer(Modifier.height(10.dp))
            (0 until 6).chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    row.forEach { i ->
                        Box(Modifier.weight(1f).aspectRatio(0.8f).clip(RoundedCornerShape(14.dp)).background(SurfaceMuted).then(if (i == 0) Modifier.border(2.dp, BrandPink, RoundedCornerShape(14.dp)) else Modifier)) {
                            if (i < photos.size) {
                                AsyncImage(photos[i], null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clickable { replaceAt = i; pick() })
                                if (i > 0) Icon(Icons.Rounded.Close, "Remove", tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).clickable { photos.removeAt(i) }.padding(3.dp))
                                if (i == 0) Text("Avatar", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp).clip(CircleShape).background(BrandPink).padding(horizontal = 6.dp, vertical = 2.dp))
                            } else {
                                Icon(Icons.Rounded.Add, "Add photo", tint = InkTertiary, modifier = Modifier.align(Alignment.Center).size(48.dp).clip(CircleShape).clickable { replaceAt = -1; pick() }.padding(12.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Display name") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(bio, { if (it.length <= 120) bio = it }, Modifier.fillMaxWidth(), label = { Text("Bio") }, minLines = 3, supportingText = { Text("${bio.length}/120") }, shape = RoundedCornerShape(14.dp), colors = fieldColors)
            OutlinedTextField(city, { city = it }, Modifier.fillMaxWidth(), label = { Text("City") }, singleLine = true, leadingIcon = { Icon(Icons.Rounded.LocationOn, null) }, shape = RoundedCornerShape(14.dp), colors = fieldColors)
            Spacer(Modifier.height(16.dp))
            Text("Languages I speak", style = MaterialTheme.typography.titleSmall, color = Ink)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Hindi", "English", "Punjabi", "Bengali", "Tamil", "Telugu", "Marathi", "Gujarati", "Urdu").forEach { l ->
                    PillChip(l, l in langs, { if (l in langs) langs.remove(l) else langs.add(l) })
                }
            }
            Spacer(Modifier.height(16.dp))
            IshqCard(Modifier.fillMaxWidth(), background = SurfaceMuted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lock, null, tint = InkSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gender and birthday can't be changed after sign-up. Contact support if they're wrong.", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ============================== 55 · Followers / following ============================== */

@Composable
fun FollowsScreen(startTab: Int, onBack: () -> Unit) {
    val nav = LocalNav.current
    var tab by rememberSaveable { mutableIntStateOf(startTab) }
    var query by remember { mutableStateOf("") }
    val following = remember { mutableStateListOf(1, 3, 6, 9) }
    ScreenScaffold(Me.name, onBack) {
        UnderlineTabs(listOf("Followers ${Me.followers.compact()}", "Following ${Me.following}", "Fans ${Me.fans}"), tab, { tab = it })
        OutlinedTextField(
            query, { query = it }, Modifier.fillMaxWidth().padding(16.dp), placeholder = { Text("Search") }, leadingIcon = { Icon(Icons.Rounded.Search, null) },
            singleLine = true, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPink, unfocusedBorderColor = Hairline)
        )
        val list = when (tab) { 1 -> sampleCreators.filter { it.id in following }; 2 -> sampleCreators.takeLast(5); else -> sampleCreators }
            .filter { it.name.contains(query, ignoreCase = true) }
        LazyColumn {
            items(list, key = { it.id }) { c ->
                Row(Modifier.fillMaxWidth().clickable { nav.push(Screen.OtherProfile, c.id) }.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(c.imageUrl, 50.dp, online = c.id % 3 == 0)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        NameRow(c.name, c.verified)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            LevelBadge(10 + c.id * 2)
                            if (tab == 2) Text(fanTiers[c.id % 3].name, style = MaterialTheme.typography.labelSmall, color = BrandPink)
                        }
                    }
                    when {
                        tab == 2 -> Text("since Oct", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
                        c.id in following -> SmallOutlineButton(if (tab == 0) "Friends" else "Following", { following.remove(c.id) }, color = InkSecondary)
                        else -> SmallGradientButton(if (tab == 0) "Follow back" else "Follow", { following.add(c.id) })
                    }
                }
            }
            if (list.isEmpty()) item { EmptyState("👥", "No one here", "Try a different search.") }
        }
    }
}

/* ============================== 57 · My subscriptions ============================== */

@Composable
fun SubscriptionsScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val active = remember { mutableStateListOf(Triple(1, 1, "Renews 1 Dec"), Triple(6, 0, "Renews 14 Nov"), Triple(9, 2, "Renews 22 Nov")) }
    var cancelling by remember { mutableStateOf<Triple<Int, Int, String>?>(null) }
    var changing by remember { mutableStateOf<Triple<Int, Int, String>?>(null) }
    changing?.let { sub ->
        com.live.ishqexpress.ui.social.FanTierSheet(creatorById(sub.first), onDismiss = { changing = null }, initialTier = sub.second, onSubscribed = { tier ->
            val i = active.indexOfFirst { it.first == sub.first }
            if (i >= 0) active[i] = sub.copy(second = tier)
        })
    }
    ScreenScaffold("Subscriptions", onBack) {
        UnderlineTabs(listOf("Active (${active.size})", "Expired"), tab, { tab = it })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (tab == 0) {
                item {
                    IshqCard(Modifier.fillMaxWidth(), background = PinkTint) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Monthly total", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                CoinAmount(active.sumOf { fanTiers[it.second].coins }, iconSize = 20.dp, style = MaterialTheme.typography.headlineSmall)
                            }
                            Text("Auto-renews from\nyour coin balance", style = MaterialTheme.typography.bodySmall, color = InkSecondary, textAlign = TextAlign.End)
                        }
                    }
                }
                items(active, key = { it.first }) { sub ->
                    val c = creatorById(sub.first)
                    val t = fanTiers[sub.second]
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.dp, Hairline, RoundedCornerShape(18.dp))) {
                        Row(Modifier.fillMaxWidth().background(Brush.linearGradient(t.colors)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Avatar(c.imageUrl, 44.dp, Modifier.border(2.dp, Color.White, CircleShape))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.name, style = MaterialTheme.typography.titleSmall, color = Color.White)
                                Text("⭐ ${t.name}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                            }
                            CoinAmount(t.coins, color = Color.White)
                        }
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Autorenew, null, tint = InkTertiary, modifier = Modifier.size(16.dp))
                            Text(" ${sub.third}", style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.weight(1f))
                            Text("Change tier", style = MaterialTheme.typography.labelMedium, color = BrandPink, modifier = Modifier.clickable { changing = sub }.padding(6.dp))
                            Text("Cancel", style = MaterialTheme.typography.labelMedium, color = InkTertiary, modifier = Modifier.clickable { cancelling = sub }.padding(6.dp))
                        }
                    }
                }
            } else {
                items(listOf(4, 12)) { id ->
                    val c = creatorById(id)
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceMuted).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(c.imageUrl, 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                            Text("Fan · ended 2 Oct", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        SmallGradientButton("Renew", { toast("Renewed ${c.name.substringBefore(' ')}") })
                    }
                }
            }
        }
    }
    cancelling?.let { sub ->
        val c = creatorById(sub.first)
        IshqSheet({ cancelling = null }) {
            Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("😢", fontSize = 44.sp)
                Text("Cancel ${fanTiers[sub.second].name}?", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("You keep your perks for ${c.name.substringBefore(' ')} until it ends (${sub.third.removePrefix("Renews ")}).", style = MaterialTheme.typography.bodyMedium, color = InkSecondary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(18.dp))
                PrimaryButton("Keep subscription", { cancelling = null })
                SmallOutlineButton("Cancel subscription", { active.remove(sub); cancelling = null; toast("Subscription cancelled") }, Modifier.fillMaxWidth().padding(top = 8.dp), height = 50.dp, color = LiveRed)
            }
        }
    }
}

/* ============================== Extra · User level ============================== */

@Composable
fun UserLevelScreen(onBack: () -> Unit) {
    ScreenScaffold("My level", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(progress = { Me.levelXp }, modifier = Modifier.size(96.dp), color = Color.White, trackColor = Color.White.copy(alpha = 0.25f), strokeWidth = 8.dp, strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Lv", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                                Text("${Me.level}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                            }
                        }
                        Spacer(Modifier.width(18.dp))
                        Column {
                            Text("Wealth level", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("31,200 / 50,000 XP", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                            Spacer(Modifier.height(6.dp))
                            Text("18,800 XP to Lv ${Me.level + 1}", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
            }
            item {
                IshqCard(Modifier.fillMaxWidth()) {
                    Text("How to earn XP", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    listOf("🎁" to "Send gifts — 1 coin spent = 1 XP", "⭐" to "Fan subscriptions count in full", "🎟️" to "Premium live tickets").forEach { (e, t) ->
                        Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Text(e, fontSize = 18.sp); Spacer(Modifier.width(10.dp)); Text(t, style = MaterialTheme.typography.bodyMedium, color = Ink) }
                    }
                    Text("Your level never resets.", style = MaterialTheme.typography.bodySmall, color = InkTertiary, modifier = Modifier.padding(top = 6.dp))
                }
            }
            item { Text("Milestone rewards", style = MaterialTheme.typography.titleMedium, color = Ink) }
            val milestones = listOf(5 to "Coloured name in chat", 10 to "Entry banner", 20 to "Custom chat bubble", 25 to "Animated entry effect", 30 to "Exclusive gift: Golden Rose", 40 to "Car entry effect", 50 to "Legend frame + badge")
            items(milestones) { (lv, reward) ->
                val reached = Me.level >= lv
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (reached) PinkTint else SurfaceMuted).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    LevelBadge(lv)
                    Spacer(Modifier.width(12.dp))
                    Text(reward, style = MaterialTheme.typography.titleSmall, color = if (reached) Ink else InkSecondary, modifier = Modifier.weight(1f))
                    Icon(if (reached) Icons.Rounded.CheckCircle else Icons.Rounded.Lock, null, tint = if (reached) BrandPink else InkTertiary)
                }
            }
        }
    }
}

/* ============================== Extra · Crowns ============================== */

@Composable
fun CrownsScreen(onBack: () -> Unit) {
    ScreenScaffold("Crowns", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFF2A0B4A), Color(0xFF7A2BE8), Color(0xFFC236D9)))) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👑", fontSize = 56.sp)
                        Text("No crowns yet", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text("Crowns go to the top spenders on ishqexpress. Lifetime spend, never resets.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(14.dp))
                        ProgressBar(0.38f, height = 10.dp, track = Color.White.copy(alpha = 0.2f), brush = Brush.horizontalGradient(listOf(Gold, Color(0xFFFFE08A))))
                        Spacer(Modifier.height(6.dp))
                        Text("38% of the way to your first crown", style = MaterialTheme.typography.labelMedium, color = Gold)
                    }
                }
            }
            item {
                IshqCard(Modifier.fillMaxWidth()) {
                    Text("Crown ribbons", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(10.dp))
                    listOf(1 to "Crown ribbon on your profile and in every live", 5 to "Design your own custom ribbon", 25 to "Gold animated crown frame", 100 to "Royal entry and a personal VIP manager").forEach { (n, t) ->
                        Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("👑$n", style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.width(58.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(Gold, Color(0xFFF08A00)))).padding(vertical = 4.dp), textAlign = TextAlign.Center)
                            Spacer(Modifier.width(12.dp))
                            Text(t, style = MaterialTheme.typography.bodyMedium, color = Ink)
                        }
                    }
                }
            }
            item { Text("Crown holders", style = MaterialTheme.typography.titleMedium, color = Ink) }
            itemsIndexed(sampleViewers.take(5)) { i, v ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(avatar(v.img), 46.dp, ring = true)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(v.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                        LevelBadge(v.level)
                    }
                    Text("👑 ${listOf(42, 18, 9, 5, 2)[i]}", style = MaterialTheme.typography.titleSmall, color = Color(0xFFD08000))
                }
            }
        }
    }
}

/* ============================== Extra · Creator level ============================== */

@Composable
fun CreatorLevelScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    val current = 4
    ScreenScaffold("Creator center", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFF0BA5A5), Color(0xFF12B76A)))) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("C$current", style = MaterialTheme.typography.displaySmall, color = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Rising creator", style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Text("Re-ranked on 1 Dec", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("Diamonds (last 30 days)  18,640 / 30,000", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        ProgressBar(0.62f, track = Color.White.copy(alpha = 0.25f), brush = Brush.horizontalGradient(listOf(Color.White, Color.White)))
                        Spacer(Modifier.height(10.dp))
                        Text("Hours live (last 30 days)  41 / 60", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        ProgressBar(0.68f, track = Color.White.copy(alpha = 0.25f), brush = Brush.horizontalGradient(listOf(Color.White, Color.White)))
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IshqCard(Modifier.weight(1f).clickable { nav.push(Screen.Earnings) }, padding = 14.dp) {
                        DiamondIcon(26.dp); Spacer(Modifier.height(6.dp))
                        Text("Earnings", style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text("${Me.diamonds.grouped()} diamonds", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    IshqCard(Modifier.weight(1f).clickable { nav.push(Screen.GoLiveSetup) }, padding = 14.dp) {
                        Icon(Icons.Rounded.Videocam, null, tint = BrandPink, modifier = Modifier.size(26.dp)); Spacer(Modifier.height(6.dp))
                        Text("Go live", style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text("19 hours to C5", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                }
            }
            item { Text("Levels and benefits", style = MaterialTheme.typography.titleMedium, color = Ink) }
            val levels = listOf(
                Triple(1, "0 💎 · 0 h", "Go live, receive gifts"),
                Triple(2, "2K 💎 · 10 h", "Stream goals"),
                Triple(3, "8K 💎 · 20 h", "Live Party (4 guests)"),
                Triple(4, "15K 💎 · 30 h", "Premium broadcasts"),
                Triple(5, "30K 💎 · 60 h", "Premium lives · +2% bonus"),
                Triple(6, "60K 💎 · 80 h", "Explore boost · +3% bonus"),
                Triple(7, "120K 💎 · 100 h", "Custom gift · +4% bonus"),
                Triple(8, "250K 💎 · 120 h", "Featured banner · +5% bonus"),
                Triple(9, "500K 💎 · 140 h", "Dedicated manager · +6% bonus"),
                Triple(10, "1M 💎 · 160 h", "Top-tier revenue share · +8% bonus"),
            )
            items(levels) { (lv, req, perk) ->
                val state = when { lv < current -> 0; lv == current -> 1; else -> 2 }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(if (state == 1) Color(0xFF0F3A2A) else if (state == 0) SurfaceMuted else Canvas)
                        .border(if (state == 1) 1.5.dp else 1.dp, if (state == 1) Success else Hairline, RoundedCornerShape(16.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("C$lv", style = MaterialTheme.typography.titleMedium, color = if (state == 2) InkTertiary else Success, modifier = Modifier.width(44.dp))
                    Column(Modifier.weight(1f)) {
                        Text(perk, style = MaterialTheme.typography.titleSmall, color = if (state == 2) InkSecondary else Ink)
                        Text(req, style = MaterialTheme.typography.bodySmall, color = InkTertiary)
                    }
                    when (state) {
                        0 -> Icon(Icons.Rounded.CheckCircle, null, tint = Success)
                        1 -> Text("YOU", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.clip(CircleShape).background(Success).padding(horizontal = 8.dp, vertical = 3.dp))
                        else -> Icon(Icons.Rounded.Lock, null, tint = InkTertiary)
                    }
                }
            }
        }
    }
}

/** Your profile as a scannable QR code, with share and copy. */
@Composable
private fun MyQrSheet(onDismiss: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val toast = LocalToast.current
    val link = AppLinks.profile(80412337)
    IshqSheet(onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar(Me.avatarUrl, 64.dp)
            Spacer(Modifier.height(8.dp))
            Text(Me.name, style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(Me.id, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            Spacer(Modifier.height(16.dp))
            Box(Modifier.size(220.dp).shadow(10.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(Color.White).padding(16.dp)) {
                QrCode(link, Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(12.dp))
            Text("Friends can scan this to follow you", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallOutlineButton("Copy link", { copyText(ctx, link); toast("Link copied") }, Modifier.weight(1f), height = 48.dp, icon = Icons.Rounded.Link)
                SmallGradientButton("Share", { shareText(ctx, "Follow me on ishqexpress: $link") }, Modifier.weight(1f), height = 48.dp, icon = Icons.Rounded.Share)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
