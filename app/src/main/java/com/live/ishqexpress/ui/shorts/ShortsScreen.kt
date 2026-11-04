package com.live.ishqexpress.ui.shorts

import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.common.*
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.gifts.GiftDrawer
import com.live.ishqexpress.ui.gifts.GiftEffectsLayer
import com.live.ishqexpress.ui.gifts.GiftEffectsState
import com.live.ishqexpress.ui.home.compact
import com.live.ishqexpress.ui.nav.LocalNav
import com.live.ishqexpress.ui.nav.LocalToast
import com.live.ishqexpress.ui.nav.Screen
import com.live.ishqexpress.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.roundToInt

/*
 * Short videos: full-screen vertical feed (swipe up/down), one shared ExoPlayer that plays the
 * settled page on loop. Tap = pause/play, double tap = like with a heart burst. Right rail:
 * profile + follow, like, comments, gift, share, save, spinning music disc. "+" uploads your own
 * video from the gallery and puts it at the top of the feed.
 */

/** Robolectric/JVM tests have no codecs or network: show thumbnails only. */
private val canPlay = Build.FINGERPRINT != "robolectric"

/**
 * @param startIndex video to open first (from a Home tile)
 * @param onBack shows a back arrow when opened as its own screen; null when it's the bottom tab
 */
@kotlin.OptIn(ExperimentalFoundationApi::class)
@OptIn(UnstableApi::class)
@Composable
fun ShortsScreen(startIndex: Int = 0, onBack: (() -> Unit)? = null) {
    DarkSystemBars()
    val nav = LocalNav.current
    val toast = LocalToast.current
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(1) } // 0 Following, 1 For you
    val list = if (tab == 0) ShortsStore.videos.filter { it.creator in ShortsStore.followed || it.creator == 0 } else ShortsStore.videos.toList()
    val pager = rememberPagerState(initialPage = startIndex.coerceIn(0, (list.size - 1).coerceAtLeast(0))) { list.size }
    var sheet by remember { mutableStateOf<Pair<String, ShortVideo>?>(null) } // "comments" / "gifts"
    var posting by remember { mutableStateOf<String?>(null) }
    val giftFx = remember { GiftEffectsState() }

    // One player for the whole feed
    val player = remember { if (canPlay) ExoPlayer.Builder(context).build().apply { repeatMode = Player.REPEAT_MODE_ONE; volume = 1f } else null }
    var paused by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var firstFrame by remember { mutableStateOf(false) }
    DisposableEffect(player) {
        val l = object : Player.Listener { override fun onRenderedFirstFrame() { firstFrame = true } }
        player?.addListener(l)
        onDispose { player?.removeListener(l); player?.release() }
    }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, player) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_PAUSE) player?.pause()
            if (e == Lifecycle.Event.ON_RESUME && !paused) player?.play()
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    val current = list.getOrNull(pager.settledPage)
    LaunchedEffect(current?.id, current?.url) {
        firstFrame = false; paused = false; progress = 0f
        val p = player ?: return@LaunchedEffect
        if (current == null) { p.stop(); return@LaunchedEffect }
        p.setMediaItem(MediaItem.fromUri(current.url))
        p.prepare(); p.play()
    }
    LaunchedEffect(player) {
        while (player != null) {
            val d = player.duration
            if (d > 0) progress = (player.currentPosition.toFloat() / d).coerceIn(0f, 1f)
            delay(150)
        }
    }

    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) posting = copyVideo(context, uri)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (list.isEmpty()) {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎬", fontSize = 48.sp)
                Text("No shorts from people you follow yet", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(Modifier.height(12.dp))
                SmallGradientButton("Watch For you", { tab = 1 })
            }
        }
        VerticalPager(pager, Modifier.fillMaxSize(), key = { list[it].id }, beyondViewportPageCount = 1) { page ->
            val v = list[page]
            val active = page == pager.settledPage
            ShortPage(
                v, active = active, player = if (active) player else null, showVideo = active && firstFrame,
                paused = active && paused, progress = if (active) progress else 0f,
                onTogglePause = { paused = !paused; if (paused) player?.pause() else player?.play() },
                onComments = { sheet = "comments" to v },
                onGift = { sheet = "gifts" to v },
                onProfile = { if (v.creator > 0) nav.push(Screen.OtherProfile, v.creator) else nav.push(Screen.Me) },
                onLive = { nav.push(Screen.LiveViewer, v.creator) },
                onShare = { shareText(context, "${v.caption}\n${AppLinks.post(v.id)}"); toast("Opening share") },
                insetBottom = onBack != null,
            )
        }

        // Top bar: back / upload · Following | For you · search
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) GlassButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack, size = 38.dp)
            else GlassButton(Icons.Rounded.AddCircleOutline, "Upload a short", {
                pickVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            }, size = 38.dp)
            Spacer(Modifier.weight(1f))
            listOf("Following", "For you").forEachIndexed { i, t ->
                val on = tab == i
                Column(Modifier.clickable { tab = i }.padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t, style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.5f), blurRadius = 8f)), color = if (on) Color.White else Color.White.copy(alpha = 0.6f))
                    Spacer(Modifier.height(3.dp))
                    Box(Modifier.width(if (on) 22.dp else 0.dp).height(3.dp).clip(CircleShape).background(BrandBrush.neon))
                }
            }
            Spacer(Modifier.weight(1f))
            GlassButton(Icons.Rounded.Search, "Search", { nav.push(Screen.Search) }, size = 38.dp)
        }
        GiftEffectsLayer(giftFx, faceAnchor = Offset(0.5f, 0.35f))
    }

    when (val s = sheet) {
        null -> {}
        else -> if (s.first == "comments") CommentsSheet(s.second) { sheet = null }
        else GiftDrawer(
            giftFx, listOf(GiftTarget(s.second.creator, shortAuthor(s.second).first, shortAuthor(s.second).second)),
            onDismiss = { sheet = null }, onRecharge = { sheet = null; nav.push(Screen.CoinStore) },
        )
    }
    posting?.let { uri ->
        var caption by remember { mutableStateOf("") }
        IshqSheet({ posting = null }, title = "New short") {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.Top) {
                Box(Modifier.size(width = 84.dp, height = 140.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceMuted), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Movie, null, tint = BrandPink, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f).height(140.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceMuted).padding(12.dp)) {
                    if (caption.isEmpty()) Text("Write a caption… #hashtags", style = MaterialTheme.typography.bodyMedium, color = InkTertiary)
                    BasicTextField(caption, { if (it.length <= 150) caption = it }, textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink), cursorBrush = SolidColor(BrandPink), modifier = Modifier.fillMaxSize())
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.padding(horizontal = 20.dp)) {
                PrimaryButton("Post short", {
                    ShortsStore.post(uri, caption); posting = null; tab = 1
                    toast("Your short is live 🎉")
                }, leadingIcon = Icons.Rounded.Upload)
            }
        }
    }
    LaunchedEffect(ShortsStore.videos.size) { if (tab == 1 && ShortsStore.videos.firstOrNull()?.creator == 0 && pager.currentPage != 0) pager.scrollToPage(0) }
}

/** Copies a picked video into app storage so it keeps playing after the picker's grant expires. */
private fun copyVideo(context: android.content.Context, uri: Uri): String = runCatching {
    val dir = File(context.filesDir, "shorts").apply { mkdirs() }
    val out = File(dir, "short_${System.currentTimeMillis()}.mp4")
    context.contentResolver.openInputStream(uri)!!.use { i -> out.outputStream().use { i.copyTo(it) } }
    Uri.fromFile(out).toString()
}.getOrElse { uri.toString() }

@OptIn(UnstableApi::class)
@Composable
private fun ShortPage(
    v: ShortVideo,
    active: Boolean,
    player: ExoPlayer?,
    showVideo: Boolean,
    paused: Boolean,
    progress: Float,
    onTogglePause: () -> Unit,
    onComments: () -> Unit,
    onGift: () -> Unit,
    onProfile: () -> Unit,
    onLive: () -> Unit,
    onShare: () -> Unit,
    insetBottom: Boolean = true,
) {
    val (name, avatar) = shortAuthor(v)
    val bottomInset = if (insetBottom) Modifier.navigationBarsPadding() else Modifier
    val liked = v.id in ShortsStore.liked
    val hearts = remember { mutableStateListOf<Pair<Long, Offset>>() }
    val density = LocalDensity.current

    Box(
        Modifier.fillMaxSize().clipToBounds().pointerInput(v.id) {
            detectTapGestures(
                onTap = { onTogglePause() },
                onDoubleTap = { pos ->
                    if (!liked) ShortsStore.toggleLike(v)
                    hearts.add(System.nanoTime() to pos)
                }
            )
        }
    ) {
        // Thumbnail with a slow Ken Burns zoom (also the stand-in while the clip buffers)
        val t = rememberInfiniteTransition(label = "kb")
        val z by t.animateFloat(1.05f, 1.18f, infiniteRepeatable(tween(9_000), RepeatMode.Reverse), label = "z")
        AsyncImage(v.thumb, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = z; scaleY = z })
        if (player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        this.player = player
                    }
                },
                update = { it.player = player },
                onRelease = { it.player = null },
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (showVideo) 1f else 0f }
            )
        }
        // Scrims
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.45f), 0.18f to Color.Transparent, 0.6f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.75f))))

        // Buffering shimmer
        if (active && !showVideo && player != null) {
            Box(Modifier.align(Alignment.Center).size(46.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = BrandPink, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            }
        }
        AnimatedVisibility(paused, Modifier.align(Alignment.Center), enter = scaleIn(initialScale = 1.6f) + fadeIn(), exit = fadeOut()) {
            Icon(Icons.Rounded.PlayArrow, "Play", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(84.dp))
        }

        // Double-tap hearts
        hearts.forEach { (key, pos) ->
            key(key) {
                val a = remember { Animatable(0f) }
                LaunchedEffect(Unit) { a.animateTo(1f, tween(900)); hearts.removeAll { it.first == key } }
                val px = with(density) { 90.dp.toPx() }
                Icon(
                    Icons.Rounded.Favorite, null, tint = NeonPink,
                    modifier = Modifier.offset { IntOffset((pos.x - px / 2).roundToInt(), (pos.y - px / 2 - a.value * px).roundToInt()) }
                        .size(90.dp).graphicsLayer {
                            val s = if (a.value < 0.2f) a.value / 0.2f * 1.2f else 1.2f - (a.value - 0.2f) * 0.3f
                            scaleX = s; scaleY = s; alpha = 1f - a.value * a.value; rotationZ = -12f + (key % 24).toFloat()
                        }
                )
            }
        }

        // Right rail
        Column(
            Modifier.align(Alignment.BottomEnd).then(bottomInset).padding(end = 10.dp, bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(Modifier.size(52.dp).shadow(10.dp, CircleShape, spotColor = NeonPink).neonBorder(CircleShape, 2.dp).padding(2.dp).clip(CircleShape).clickable(onClick = onProfile)) {
                    AsyncImage(avatar, name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                if (v.creator > 0) {
                    val following = v.creator in ShortsStore.followed
                    Icon(
                        if (following) Icons.Rounded.Check else Icons.Rounded.Add, if (following) "Following" else "Follow", tint = Color.White,
                        modifier = Modifier.offset(y = 10.dp).size(22.dp).clip(CircleShape).background(if (following) Success else BrandPink)
                            .clickable { if (following) ShortsStore.followed.remove(v.creator) else ShortsStore.followed.add(v.creator) }.padding(3.dp)
                    )
                }
            }
            if (v.creator > 0) Text(
                "LIVE", style = MaterialTheme.typography.labelSmall, color = Color.White,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(BrandBrush.primary).clickable(onClick = onLive).padding(horizontal = 6.dp, vertical = 2.dp)
            )
            val pop by animateFloatAsState(if (liked) 1.15f else 1f, spring(dampingRatio = 0.35f, stiffness = 600f), label = "pop")
            RailButton(Icons.Rounded.Favorite, ShortsStore.likes(v).compact(), tint = if (liked) NeonPink else Color.White, scale = pop) { ShortsStore.toggleLike(v) }
            RailButton(Icons.Rounded.ChatBubble, (ShortsStore.comments[v.id]?.size?.let { v.comments + it - 4 } ?: v.comments).coerceAtLeast(0).compact(), onClick = onComments)
            RailButton(Icons.Rounded.CardGiftcard, "Gift", tint = Gold, onClick = onGift)
            val saved = v.id in ShortsStore.saved
            RailButton(if (saved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, if (saved) "Saved" else "Save", tint = if (saved) Gold else Color.White) {
                if (saved) ShortsStore.saved.remove(v.id) else ShortsStore.saved.add(v.id)
            }
            RailButton(Icons.Rounded.Reply, v.shares.compact(), mirror = true, onClick = onShare)
            MusicDisc(avatar, spinning = active && !paused)
        }

        // Caption block
        Column(Modifier.align(Alignment.BottomStart).then(bottomInset).padding(start = 14.dp, end = 90.dp, bottom = 26.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onProfile)) {
                Text("@${name.lowercase().replace(' ', '_')}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                if (v.creator > 0 && creatorById(v.creator).verified) { Spacer(Modifier.width(4.dp)); Icon(Icons.Rounded.Verified, null, tint = NeonCyan, modifier = Modifier.size(16.dp)) }
            }
            Spacer(Modifier.height(6.dp))
            Text(captionStyled(v.caption), style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.3f)).padding(horizontal = 10.dp, vertical = 5.dp).widthIn(max = 220.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.MusicNote, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(v.music, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1, modifier = Modifier.basicMarquee())
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PlayArrow, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                Text("${v.views.compact()} views", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }

        // Progress
        Box(Modifier.align(Alignment.BottomCenter).then(bottomInset).fillMaxWidth().height(2.5.dp).background(Color.White.copy(alpha = 0.18f))) {
            Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(BrandBrush.neon))
        }
    }
}

private fun captionStyled(text: String) = androidx.compose.ui.text.buildAnnotatedString {
    text.split(' ').forEachIndexed { i, w ->
        if (i > 0) append(' ')
        if (w.startsWith('#')) { pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFFFFA8D9), fontWeight = FontWeight.SemiBold)); append(w); pop() } else append(w)
    }
}

@Composable
private fun RailButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color = Color.White, scale: Float = 1f, mirror: Boolean = false, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(44.dp).graphicsLayer { scaleX = scale * (if (mirror) -1f else 1f); scaleY = scale }, contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.Black.copy(alpha = 0.35f), modifier = Modifier.size(34.dp).offset(y = 2.dp))
            Icon(icon, label, tint = tint, modifier = Modifier.size(34.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium.copy(shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.6f), blurRadius = 6f)), color = Color.White)
    }
}

@Composable
private fun MusicDisc(cover: String, spinning: Boolean) {
    val rot = remember { Animatable(0f) }
    LaunchedEffect(spinning) {
        while (spinning) { rot.animateTo(rot.value + 360f, tween(4_000, easing = LinearEasing)) }
    }
    Box(
        Modifier.size(46.dp).graphicsLayer { rotationZ = rot.value }.clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFF3A3A3A), Color(0xFF0E0E0E))))
            .border(1.dp, Brush.sweepGradient(listOf(NeonPink, NeonViolet, NeonCyan, NeonPink)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(cover, null, contentScale = ContentScale.Crop, modifier = Modifier.size(26.dp).clip(CircleShape))
    }
}

@Composable
private fun CommentsSheet(v: ShortVideo, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val liked = remember { mutableStateListOf<Int>() }
    val list = ShortsStore.comments[v.id] ?: ShortsStore.commentsFor(v)
    IshqSheet(onDismiss, title = "${(v.comments + list.size - 4).coerceAtLeast(list.size).compact()} comments") {
        LazyColumn(Modifier.heightIn(max = 380.dp)) {
            items(list.size) { i ->
                val c = list[i]
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
                    AsyncImage(c.avatar, null, contentScale = ContentScale.Crop, modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceMuted))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${c.user} · ${c.time}", style = MaterialTheme.typography.labelMedium, color = InkTertiary)
                        Text(c.text, style = MaterialTheme.typography.bodyMedium, color = Ink)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { if (i in liked) liked.remove(i) else liked.add(i) }) {
                        Icon(if (i in liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Like", tint = if (i in liked) NeonPink else InkTertiary, modifier = Modifier.size(18.dp))
                        Text("${c.likes + if (i in liked) 1 else 0}", style = MaterialTheme.typography.labelSmall, color = InkTertiary)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f).height(46.dp).clip(CircleShape).background(SurfaceMuted).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    if (draft.isEmpty()) Text("Add a comment…", style = MaterialTheme.typography.bodyMedium, color = InkTertiary)
                    BasicTextField(draft, { draft = it }, singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink), cursorBrush = SolidColor(BrandPink), modifier = Modifier.fillMaxWidth())
                }
                listOf("😍", "🔥", "👏").forEach { e -> Text(e, fontSize = 18.sp, modifier = Modifier.clickable { draft += e }.padding(horizontal = 3.dp)) }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Rounded.Send, "Post", tint = Color.White,
                modifier = Modifier.size(44.dp).clip(CircleShape).background(if (draft.isBlank()) DisabledFill else BrandPink).clickable(enabled = draft.isNotBlank()) {
                    ShortsStore.addComment(v, draft.trim()); draft = ""
                }.padding(11.dp)
            )
        }
    }
}

/* ------------------------------ Home feed tile ------------------------------ */

/** Short video card mixed into the Home grid. */
@Composable
fun ShortTile(v: ShortVideo, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    val (name, avatar) = shortAuthor(v)
    val t = rememberInfiniteTransition(label = "tile")
    val z by t.animateFloat(1f, 1.12f, infiniteRepeatable(tween(6_000), RepeatMode.Reverse), label = "z")
    Box(
        Modifier.fillMaxWidth().aspectRatio(0.78f).tilt3D()
            .shadow(14.dp, shape, ambientColor = NeonViolet, spotColor = NeonCyan)
            .clip(shape).background(SurfaceMuted).clickable(onClick = onClick)
    ) {
        AsyncImage(v.thumb, v.caption, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = z; scaleY = z })
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.3f), 0.25f to Color.Transparent, 0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.78f))))
        Box(Modifier.fillMaxSize().border(1.5.dp, Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.8f), NeonViolet.copy(alpha = 0.4f), NeonPink.copy(alpha = 0.8f))), shape))
        Row(
            Modifier.padding(8.dp).clip(RoundedCornerShape(6.dp))
                .background(Brush.horizontalGradient(listOf(NeonCyan, NeonViolet))).padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.SlowMotionVideo, null, tint = Color.White, modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(3.dp))
            Text("SHORTS", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
        Box(
            Modifier.align(Alignment.Center).size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f))
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape).floating(3.dp),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(28.dp)) }
        Column(Modifier.align(Alignment.BottomStart).padding(10.dp)) {
            Text(v.caption, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(avatar, null, contentScale = ContentScale.Crop, modifier = Modifier.size(16.dp).clip(CircleShape))
                Spacer(Modifier.width(4.dp))
                Text(name.substringBefore(' '), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.weight(1f), maxLines = 1)
                Icon(Icons.Rounded.Favorite, null, tint = NeonPink, modifier = Modifier.size(11.dp))
                Text(" ${ShortsStore.likes(v).compact()}", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}
