package com.live.ishqexpress.ui.social

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
import androidx.compose.ui.focus.focusRequester
import com.live.ishqexpress.ui.common.*
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.home.LiveCreator
import com.live.ishqexpress.ui.home.compact
import com.live.ishqexpress.ui.home.sampleCreators
import com.live.ishqexpress.ui.nav.*
import com.live.ishqexpress.ui.theme.*
import com.live.ishqexpress.ui.home.LiveMode
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import kotlinx.coroutines.launch

/*
 * 37 Match · 38 Match result · 39 Feed · 40 Create post · 41 Post detail · 42 Locked post
 * 43 News Center · 44 Notifications
 */

/* ============================== 37 · Match ============================== */

@Composable
fun MatchScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    var index by rememberSaveable { mutableIntStateOf(0) }
    var matched by remember { mutableStateOf<MatchCard?>(null) }
    val scope = rememberCoroutineScope()

    ScreenScaffold("Match", onBack, actions = { TopAction(Icons.Rounded.Tune, "Filters") {} }) {
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            if (index >= sampleMatches.size) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EmptyState("💫", "You're all caught up", "New people join every hour. Check back soon.")
                    SmallGradientButton("Start over", { index = 0 }, height = 44.dp)
                }
            } else {
                // Card behind
                if (index + 1 < sampleMatches.size) MatchCardView(sampleMatches[index + 1], Modifier.graphicsLayer { scaleX = 0.94f; scaleY = 0.94f; translationY = 30f })
                val offsetX = remember(index) { Animatable(0f) }
                val card = sampleMatches[index]
                fun swipe(like: Boolean) {
                    scope.launch {
                        offsetX.animateTo(if (like) 1400f else -1400f, tween(260))
                        if (like && card.creator % 2 == 1) matched = card
                        index++
                    }
                }
                Box(
                    Modifier
                        .graphicsLayer { translationX = offsetX.value; rotationZ = offsetX.value / 40f }
                        .pointerInput(index) {
                            detectDragGestures(
                                onDragEnd = {
                                    when {
                                        offsetX.value > 300f -> swipe(true)
                                        offsetX.value < -300f -> swipe(false)
                                        else -> scope.launch { offsetX.animateTo(0f, spring()) }
                                    }
                                },
                                onDrag = { change, drag -> change.consume(); scope.launch { offsetX.snapTo(offsetX.value + drag.x) } }
                            )
                        }
                ) {
                    MatchCardView(card)
                    val likeAlpha = (offsetX.value / 300f).coerceIn(0f, 1f)
                    val nopeAlpha = (-offsetX.value / 300f).coerceIn(0f, 1f)
                    Text("LIKE", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Success, modifier = Modifier.align(Alignment.TopStart).padding(28.dp).graphicsLayer { alpha = likeAlpha; rotationZ = -15f }.border(3.dp, Success, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp))
                    Text("NOPE", fontSize = 34.sp, fontWeight = FontWeight.Black, color = LiveRed, modifier = Modifier.align(Alignment.TopEnd).padding(28.dp).graphicsLayer { alpha = nopeAlpha; rotationZ = 15f }.border(3.dp, LiveRed, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp))
                }
                // Actions
                Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    RoundAction(Icons.Rounded.Close, LiveRed, 58.dp) { swipe(false) }
                    RoundAction(Icons.Rounded.Star, BrandViolet, 48.dp) { swipe(true) }
                    RoundAction(Icons.Rounded.Favorite, BrandPink, 58.dp) { swipe(true) }
                }
            }
        }
    }

    matched?.let { m ->
        MatchResult(m, onChat = { matched = null; nav.push(Screen.ChatThread, sampleConversations.firstOrNull { it.creator == m.creator }?.id ?: 1) }, onKeep = { matched = null })
    }
}

@Composable
private fun MatchCardView(m: MatchCard, modifier: Modifier = Modifier) {
    val c = creatorById(m.creator)
    Box(modifier.fillMaxWidth().fillMaxHeight(0.86f).shadow(12.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(c.tint))) {
        AsyncImage(c.imageUrl.replace("600", "800"), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.8f))))
        if (m.live) LivePill(Modifier.padding(16.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(start = 20.dp, end = 20.dp, bottom = 92.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${c.name.substringBefore(' ')}, ${m.age}", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                if (c.verified) { Spacer(Modifier.width(6.dp)); Icon(Icons.Rounded.Verified, null, tint = Color(0xFFFF7AB6)) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                Text(" ${c.city} · ${"%.0f".format(c.distanceKm)} km away", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
            Spacer(Modifier.height(8.dp))
            Text(m.bio, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                m.interests.forEach { Text(it, style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 5.dp)) }
            }
        }
    }
}

@Composable
private fun RoundAction(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, size: Dp, onClick: () -> Unit) {
    Box(Modifier.size(size).shadow(10.dp, CircleShape).clip(CircleShape).background(Canvas).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.48f))
    }
}

/* ============================== 38 · Match result ============================== */

@Composable
private fun MatchResult(m: MatchCard, onChat: () -> Unit, onKeep: () -> Unit) {
    val c = creatorById(m.creator)
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.5f)) }
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(BrandPink.copy(alpha = 0.96f), BrandViolet.copy(alpha = 0.96f)))).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        Column(Modifier.padding(28.dp).graphicsLayer { scaleX = scale.value; scaleY = scale.value }, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("It's a match!", fontSize = 40.sp, fontWeight = FontWeight.Black, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.White)
            Text("You and ${c.name.substringBefore(' ')} liked each other", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(30.dp))
            Box(contentAlignment = Alignment.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy((-24).dp)) {
                    Avatar(Me.avatarUrl, 120.dp, Modifier.graphicsLayer { rotationZ = -8f }.border(4.dp, Color.White, CircleShape))
                    Avatar(c.imageUrl, 120.dp, Modifier.graphicsLayer { rotationZ = 8f }.border(4.dp, Color.White, CircleShape))
                }
                Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Text("💘", fontSize = 24.sp) }
            }
            Spacer(Modifier.height(40.dp))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).clickable(onClick = onChat), contentAlignment = Alignment.Center) {
                Text("Send a message", style = MaterialTheme.typography.labelLarge, color = BrandPink)
            }
            Spacer(Modifier.height(10.dp))
            Text("Keep swiping", style = MaterialTheme.typography.labelLarge, color = Color.White, modifier = Modifier.clip(CircleShape).clickable(onClick = onKeep).padding(14.dp))
        }
    }
}

/* ============================== 39 · Feed (+42 locked posts) ============================== */

@Composable
fun FeedScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val liked = remember { mutableStateListOf<Int>() }
    Box(Modifier.fillMaxSize()) {
        ScreenScaffold("Feed", onBack, actions = { TopAction(Icons.Outlined.Newspaper, "News") { nav.push(Screen.News) } }) {
            UnderlineTabs(listOf("Following", "For you"), tab, { tab = it })
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                item { StoriesRow() }
                val posts = if (tab == 0) MyPosts.all().filter { it.creator !in com.live.ishqexpress.data.Session.blocked } else samplePosts.reversed()
                items(posts, key = { it.id }) { p ->
                    PostCard(p, liked = p.id in liked, onLike = { if (p.id in liked) liked.remove(p.id) else liked.add(p.id) }, onOpen = { nav.push(Screen.PostDetail, p.id) })
                    HorizontalDivider(color = Hairline, thickness = 6.dp)
                }
            }
        }
        Box(
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(20.dp).size(58.dp).shadow(12.dp, CircleShape, spotColor = BrandPink).clip(CircleShape).background(BrandBrush.deep).clickable { nav.push(Screen.CreatePost) },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Add, "Create post", tint = Color.White, modifier = Modifier.size(28.dp)) }
    }
}

@Composable
private fun StoriesRow() {
    val nav = LocalNav.current
    LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(Modifier.width(64.dp).clickable { nav.push(Screen.CreatePost) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    Avatar(Me.avatarUrl, 62.dp)
                    Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.align(Alignment.BottomEnd).size(20.dp).clip(CircleShape).background(BrandPink).border(2.dp, Canvas, CircleShape).padding(2.dp))
                }
                Spacer(Modifier.height(6.dp))
                Text("Your vlog", style = MaterialTheme.typography.bodySmall, color = Ink)
            }
        }
        items(sampleCreators.take(8)) { c ->
            Column(Modifier.width(64.dp).clickable { nav.push(Screen.OtherProfile, c.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(c.imageUrl, 62.dp, ring = true)
                Spacer(Modifier.height(6.dp))
                Text(c.name.substringBefore(' '), style = MaterialTheme.typography.bodySmall, color = Ink, maxLines = 1)
            }
        }
    }
}

@Composable
fun PostCard(p: Post, liked: Boolean, onLike: () -> Unit, onOpen: () -> Unit) {
    val nav = LocalNav.current
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val c = creatorById(p.creator)
    val mine = p.creator == 0
    var unlockSheet by remember { mutableStateOf(false) }
    var giftSheet by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(if (mine) Me.avatarUrl else c.imageUrl, 40.dp, Modifier.clickable { if (!mine) nav.push(Screen.OtherProfile, c.id) })
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                NameRow(if (mine) Me.name else c.name, !mine && c.verified)
                Text("${p.time} · ${if (p.locked) "Super Fans" else "Public"}", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
            }
            Icon(Icons.Rounded.MoreHoriz, null, tint = InkTertiary)
        }
        Spacer(Modifier.height(10.dp))
        Text(p.text, style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.padding(horizontal = 16.dp).clickable(onClick = onOpen))
        if (p.image != null || p.media != null) {
            Spacer(Modifier.height(10.dp))
            Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(18.dp)).background(SurfaceMuted).clickable { if (p.locked) unlockSheet = true else onOpen() }) {
                AsyncImage(p.media ?: photo(p.image ?: 0), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().then(if (p.locked) Modifier.blur(28.dp) else Modifier))
                if (p.video && !p.locked) Icon(Icons.Rounded.PlayCircle, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(56.dp))
                if (p.locked) LockedOverlay { unlockSheet = true }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            PostAction(if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, (p.likes + if (liked) 1 else 0).compact(), if (liked) BrandPink else InkSecondary, onLike)
            PostAction(Icons.Outlined.ChatBubbleOutline, p.comments.compact(), InkSecondary, onOpen)
            if (!mine) PostAction(Icons.Outlined.CardGiftcard, "Gift", InkSecondary) { giftSheet = true }
            Spacer(Modifier.weight(1f))
            PostAction(Icons.Outlined.Share, "", InkSecondary) {
                shareText(ctx, "${p.text.take(80)} — ${if (mine) Me.name else c.name} on ishqexpress ${AppLinks.post(p.id)}")
            }
        }
    }
    if (unlockSheet) FanTierSheet(c, onDismiss = { unlockSheet = false })
    if (giftSheet) PostGiftSheet(c.name, onDismiss = { giftSheet = false })
}

@Composable
private fun LockedOverlay(onUnlock: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Lock, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text("Exclusive for Super Fans", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(10.dp))
            SmallGradientButton("Unlock from 999 coins/mo", onUnlock)
        }
    }
}

@Composable
private fun PostAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Row(Modifier.clip(CircleShape).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        if (label.isNotEmpty()) { Spacer(Modifier.width(5.dp)); Text(label, style = MaterialTheme.typography.labelMedium, color = tint) }
    }
}

/* ============================== 56 · Fan subscription tiers (shared) ============================== */

@Composable
fun FanTierSheet(creator: LiveCreator, onDismiss: () -> Unit) {
    val toast = LocalToast.current
    var sel by remember { mutableIntStateOf(1) }
    IshqSheet(onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar(creator.imageUrl, 64.dp, ring = true)
            Spacer(Modifier.height(8.dp))
            Text("Become ${creator.name.substringBefore(' ')}'s fan", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text("Billed monthly from your coin balance. Cancel anytime.", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        Spacer(Modifier.height(14.dp))
        fanTiers.forEachIndexed { i, t ->
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .then(if (sel == i) Modifier.background(Brush.linearGradient(t.colors)) else Modifier.background(SurfaceMuted))
                    .clickable { sel = i }.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(t.name, style = MaterialTheme.typography.titleMedium, color = if (sel == i) Color.White else Ink, modifier = Modifier.weight(1f))
                    if (i == 1) Text("POPULAR", style = MaterialTheme.typography.labelSmall, color = if (sel == i) BrandViolet else Color.White, modifier = Modifier.clip(CircleShape).background(if (sel == i) Color.White else BrandViolet).padding(horizontal = 8.dp, vertical = 3.dp))
                    Spacer(Modifier.width(8.dp))
                    CoinAmount(t.coins, color = if (sel == i) Color.White else Ink, style = MaterialTheme.typography.titleMedium)
                    Text("/mo", style = MaterialTheme.typography.bodySmall, color = if (sel == i) Color.White.copy(alpha = 0.8f) else InkTertiary)
                }
                AnimatedVisibility(sel == i) {
                    Column(Modifier.padding(top = 8.dp)) {
                        t.perks.forEach { perk ->
                            Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(perk, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.padding(horizontal = 20.dp)) {
            PrimaryButton("Subscribe · ${fanTiers[sel].coins.grouped()} coins", { onDismiss(); toast("You're now a ${fanTiers[sel].name}! 🎉") })
        }
    }
}

/* ============================== 40 · Create post ============================== */

@Composable
fun CreatePostScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    var text by rememberSaveable { mutableStateOf("") }
    val media = remember { mutableStateListOf<String>() }
    val pickMedia = rememberMediaPicker(max = 9) { picked -> media.addAll(picked.take(9 - media.size)) }
    var fansOnly by rememberSaveable { mutableStateOf(false) }
    var tier by rememberSaveable { mutableIntStateOf(1) }
    var allowComments by rememberSaveable { mutableStateOf(true) }
    var location by rememberSaveable { mutableStateOf<String?>("Jaipur, Rajasthan") }
    var pickLocation by remember { mutableStateOf(false) }

    ScreenScaffold(
        "New post", onBack,
        actions = {
            Box(Modifier.padding(end = 8.dp)) {
                SmallGradientButton("Post", {
                    if (text.isBlank() && media.isEmpty()) toast("Write something or add a photo")
                    else {
                        MyPosts.items.add(0, Post(
                            id = 10_000 + MyPosts.items.size, creator = 0, text = text.trim() + (location?.let { "\n📍 $it" } ?: ""), image = null,
                            likes = 0, comments = 0, time = "now", locked = fansOnly, media = media.firstOrNull(),
                        ))
                        toast("Posted to your feed"); onBack()
                    }
                }, Modifier.alpha(if (text.isNotBlank() || media.isNotEmpty()) 1f else 0.4f))
            }
        }
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).imePadding()) {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
                Avatar(Me.avatarUrl, 42.dp)
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f).heightIn(min = 120.dp).padding(top = 10.dp)) {
                    if (text.isEmpty()) Text("Share something with your fans…", style = MaterialTheme.typography.bodyLarge, color = InkTertiary)
                    BasicTextField(text, { text = it }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink), cursorBrush = androidx.compose.ui.graphics.SolidColor(BrandPink), modifier = Modifier.fillMaxWidth())
                }
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(media.toList()) { m ->
                    Box(Modifier.size(96.dp).clip(RoundedCornerShape(14.dp))) {
                        AsyncImage(m, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Icon(Icons.Rounded.Close, "Remove", tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)).clickable { media.remove(m) }.padding(3.dp))
                    }
                }
                item {
                    Column(
                        Modifier.size(96.dp).clip(RoundedCornerShape(14.dp)).border(1.5.dp, Hairline, RoundedCornerShape(14.dp)).clickable { if (media.size >= 9) toast("Up to 9 photos per post") else pickMedia() },
                        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, null, tint = BrandPink)
                        Text("Photo / video", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Hairline)
            ToggleRow(Icons.Rounded.Lock, "Subscribers only", fansOnly, { fansOnly = it }, subtitle = "Lock this post behind a fan tier")
            AnimatedVisibility(fansOnly) {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fanTiers.forEachIndexed { i, t -> PillChip("${t.name}+", tier == i, { tier = i }) }
                }
            }
            ToggleRow(Icons.Outlined.ChatBubbleOutline, "Allow comments", allowComments, { allowComments = it })
            MenuRow(Icons.Rounded.LocationOn, if (location == null) "Add location" else location!!, trailingText = if (location == null) null else "Change", onClick = { pickLocation = true })
            if (pickLocation) com.live.ishqexpress.ui.more.LocationPickerSheet(location, onDismiss = { pickLocation = false }, onPick = { location = it; pickLocation = false })
            MenuRow(Icons.Rounded.Tag, "Add hashtags", onClick = { text += " #DiwaliLive" })
        }
    }
}

/* ============================== 41 · Post detail ============================== */

@Composable
fun PostDetailScreen(postId: Int, onBack: () -> Unit) {
    val post = MyPosts.byId(postId) ?: samplePosts.first()
    var liked by remember { mutableStateOf(false) }
    val focus = remember { androidx.compose.ui.focus.FocusRequester() }
    val comments = remember { mutableStateListOf(*sampleComments.toTypedArray()) }
    var draft by remember { mutableStateOf("") }
    val likedComments = remember { mutableStateListOf<Int>() }

    ScreenScaffold(
        "Post", onBack,
        bottomBar = {
            Column {
                HorizontalDivider(color = Hairline)
                Row(Modifier.fillMaxWidth().imePadding().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(Me.avatarUrl, 34.dp)
                    Spacer(Modifier.width(10.dp))
                    Row(Modifier.weight(1f).height(42.dp).clip(CircleShape).background(SurfaceMuted).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            if (draft.isEmpty()) Text("Add a comment…", style = MaterialTheme.typography.bodyMedium, color = InkTertiary)
                            BasicTextField(draft, { draft = it }, singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink), cursorBrush = androidx.compose.ui.graphics.SolidColor(BrandPink), modifier = Modifier.fillMaxWidth().then(Modifier.focusRequester(focus)))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Post", style = MaterialTheme.typography.titleSmall, color = if (draft.isBlank()) InkTertiary else BrandPink, modifier = Modifier.clickable(enabled = draft.isNotBlank()) {
                        comments.add(0, Comment(Me.name, Me.photo, draft.trim(), "now", 0)); draft = ""
                    })
                }
            }
        }
    ) {
        LazyColumn {
            item { PostCard(post, liked, { liked = !liked }, { runCatching { focus.requestFocus() } }) }
            item { HorizontalDivider(color = Hairline); SectionLabel("${comments.size} comments") }
            itemsIndexed(comments) { i, c ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Avatar(avatar(c.img), 36.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.user, style = MaterialTheme.typography.titleSmall, color = Ink)
                            Text("  ${c.time}", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
                        }
                        Text(c.text, style = MaterialTheme.typography.bodyMedium, color = Ink)
                        Text("Reply", style = MaterialTheme.typography.labelMedium, color = InkTertiary, modifier = Modifier.padding(top = 4.dp).clickable { draft = "@${c.user} " })
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { if (i in likedComments) likedComments.remove(i) else likedComments.add(i) }) {
                        Icon(if (i in likedComments) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (i in likedComments) BrandPink else InkTertiary, modifier = Modifier.size(18.dp))
                        Text("${c.likes + if (i in likedComments) 1 else 0}", style = MaterialTheme.typography.labelSmall, color = InkTertiary)
                    }
                }
            }
        }
    }
}

/* ============================== 43 · News Center ============================== */

@Composable
fun NewsScreen(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("All", "Event", "Update", "Trending")
    ScreenScaffold("News Center", onBack) {
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(tabs) { i, t -> PillChip(t, tab == i, { tab = i }) }
        }
        val items = sampleNews.filter { tab == 0 || it.tag == tabs[tab] }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            itemsIndexed(items) { i, n ->
                if (i == 0 && tab == 0) {
                    Box(Modifier.fillMaxWidth().aspectRatio(1.7f).clip(RoundedCornerShape(22.dp))) {
                        AsyncImage(photo(n.image, 800, 470), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.3f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.8f))))
                        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            Text(n.tag.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(BrandBrush.primary).padding(horizontal = 6.dp, vertical = 2.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(n.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Text(n.body, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 2)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(photo(n.image, 200, 200), null, contentScale = ContentScale.Crop, modifier = Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${n.tag} · ${n.time}", style = MaterialTheme.typography.labelMedium, color = BrandPink)
                            Text(n.title, style = MaterialTheme.typography.titleSmall, color = Ink)
                            Text(n.body, style = MaterialTheme.typography.bodySmall, color = InkSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

/* ============================== 44 · Notifications ============================== */

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val notifs = remember { mutableStateListOf(*sampleNotifs.toTypedArray()) }
    ScreenScaffold("Notifications", onBack, actions = {
        TopAction(Icons.Rounded.DoneAll, "Mark all read") { notifs.replaceAll { it.copy(unread = false) } }
    }) {
        UnderlineTabs(listOf("All", "Live", "Gifts", "Social"), tab, { tab = it })
        val list = notifs.filter {
            when (tab) {
                1 -> it.kind == NotifKind.Live
                2 -> it.kind == NotifKind.Gift
                3 -> it.kind in setOf(NotifKind.Follow, NotifKind.Like, NotifKind.Comment)
                else -> true
            }
        }
        LazyColumn {
            item { SectionLabel("System", action = "News Center", onAction = { nav.push(Screen.News) }) }
            items(list) { n ->
                val c = creatorById(n.who)
                Row(
                    Modifier.fillMaxWidth().background(if (n.unread) PinkTint.copy(alpha = 0.5f) else Canvas).clickable {
                        notifs[notifs.indexOf(n)] = n.copy(unread = false)
                        when (n.kind) {
                            NotifKind.Live -> nav.push(Screen.LiveViewer, c.id)
                            NotifKind.System -> nav.push(Screen.UserLevel)
                            else -> nav.push(Screen.OtherProfile, c.id)
                        }
                    }.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        if (n.kind == NotifKind.System) {
                            Box(Modifier.size(46.dp).clip(CircleShape).background(BrandBrush.deep), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.EmojiEvents, null, tint = Color.White) }
                        } else Avatar(c.imageUrl, 46.dp)
                        val (icon, bg) = when (n.kind) {
                            NotifKind.Follow -> Icons.Rounded.PersonAdd to BrandViolet
                            NotifKind.Gift -> Icons.Rounded.CardGiftcard to Gold
                            NotifKind.Live -> Icons.Rounded.Videocam to LiveRed
                            NotifKind.Like -> Icons.Rounded.Favorite to BrandPink
                            NotifKind.Comment -> Icons.Rounded.ChatBubble to BattleBlue
                            NotifKind.System -> null to Color.Transparent
                        }
                        if (icon != null) Icon(icon, null, tint = Color.White, modifier = Modifier.align(Alignment.BottomEnd).offset(4.dp, 4.dp).size(20.dp).clip(CircleShape).background(bg).border(2.dp, Canvas, CircleShape).padding(3.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(n.text, style = MaterialTheme.typography.bodyMedium, color = Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(n.time, style = MaterialTheme.typography.bodySmall, color = InkTertiary)
                    }
                    when (n.kind) {
                        NotifKind.Follow -> {
                            var followed by rememberSaveable(n.text) { mutableStateOf(false) }
                            if (followed) SmallOutlineButton("Following", { followed = false }, height = 30.dp)
                            else SmallGradientButton("Follow back", { followed = true }, height = 30.dp)
                        }
                        NotifKind.Live -> LivePill()
                        else -> if (n.unread) Box(Modifier.size(8.dp).clip(CircleShape).background(BrandPink))
                    }
                }
            }
            if (list.isEmpty()) item { EmptyState("🔔", "Nothing here yet", "We'll let you know when something happens.") }
        }
    }
}

/** Quick gift on a post: a few small gifts paid from your coins. */
@Composable
private fun PostGiftSheet(creatorName: String, onDismiss: () -> Unit) {
    val toast = LocalToast.current
    val nav = LocalNav.current
    val gifts = sampleGifts.filter { it.tab == GiftTab.Popular }.take(4)
    IshqSheet(onDismiss, title = "Send a gift to ${creatorName.substringBefore(' ')}") {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            gifts.forEach { g ->
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(SurfaceMuted).clickable {
                        if (DemoWallet.coins < g.coins) { onDismiss(); toast("Not enough coins"); nav.push(Screen.CoinStore) }
                        else { DemoWallet.coins -= g.coins; onDismiss(); toast("Sent ${g.name} ${g.emoji}") }
                    }.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(g.emoji, fontSize = 30.sp)
                    FitText(g.name, MaterialTheme.typography.labelMedium, color = Ink)
                    CoinAmount(g.coins)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Balance", style = MaterialTheme.typography.bodySmall, color = InkSecondary, modifier = Modifier.weight(1f))
            CoinAmount(DemoWallet.coins)
        }
    }
}
