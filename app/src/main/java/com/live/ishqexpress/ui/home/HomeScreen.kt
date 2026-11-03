package com.live.ishqexpress.ui.home

import androidx.compose.material.icons.rounded.SlowMotionVideo

import com.live.ishqexpress.ui.theme.*

import com.live.ishqexpress.ui.theme.BrandViolet
import com.live.ishqexpress.ui.common.IshqSheet
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.CheckCircle
import com.live.ishqexpress.ui.theme.appBackground
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.live.ishqexpress.ui.common.CoinIcon
import com.live.ishqexpress.ui.common.grouped
import com.live.ishqexpress.ui.nav.LocalNav
import com.live.ishqexpress.ui.nav.Screen
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.components.BrandLogo
import com.live.ishqexpress.ui.components.Wordmark
import com.live.ishqexpress.ui.theme.BrandBrush
import com.live.ishqexpress.ui.theme.BrandOrchid
import com.live.ishqexpress.ui.theme.BrandPink
import com.live.ishqexpress.ui.theme.Canvas
import com.live.ishqexpress.ui.theme.Gold
import com.live.ishqexpress.ui.theme.Hairline
import com.live.ishqexpress.ui.theme.Ink
import com.live.ishqexpress.ui.theme.InkSecondary
import com.live.ishqexpress.ui.theme.InkTertiary
import com.live.ishqexpress.ui.theme.LiveRed
import com.live.ishqexpress.ui.theme.PinkTint
import com.live.ishqexpress.ui.theme.SurfaceMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class FeedTab(val label: String) { ForYou("For You"), Following("Following"), Popular("Popular"), Nearby("Nearby") }

@Composable
fun HomeScreen() {
    val nav = LocalNav.current
    val scope = rememberCoroutineScope()
    val onAction: (String) -> Unit = { what ->
        when (what) {
            "Wallet" -> nav.push(Screen.CoinStore)
            "Search" -> nav.push(Screen.Search)
            "Notifications" -> nav.push(Screen.Notifications)
            "Me" -> nav.push(Screen.Me)
        }
    }

    var region by rememberSaveable { mutableIntStateOf(0) }
    var showRegions by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .appBackground()
            .statusBarsPadding()
    ) {
        HomeTopBar(onAction = onAction)
        val pager = rememberPagerState { FeedTab.entries.size }
        FeedTabs(selected = pager.currentPage, onSelect = { scope.launch { pager.animateScrollToPage(it) } })
        HorizontalDivider(color = Hairline, thickness = 0.5.dp)
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(pager, Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
                FeedPage(
                    FeedTab.entries[page],
                    region = region,
                    onRegion = { showRegions = true },
                    onCreator = { nav.push(Screen.LiveViewer, it.id) },
                    onShort = { nav.push(Screen.Shorts, it) },
                    onPromo = { i ->
                        when (i) {
                            0 -> nav.push(Screen.News)
                            1 -> nav.push(Screen.GoLiveSetup)
                            else -> nav.push(Screen.DailyRewards)
                        }
                    },
                    onMidPromo = { i ->
                        when (i) {
                            0 -> nav.push(Screen.LiveViewer, sampleCreators.first { it.mode == LiveMode.Pk }.id)
                            1 -> nav.push(Screen.Leaderboards)
                            else -> nav.push(Screen.Agency)
                        }
                    }
                )
            }
            // Followed creators who are live, as a floating pile (not on the Following tab, which lists them).
            val followedLive = remember { sampleCreators.filter { it.verified } }
            androidx.compose.animation.AnimatedVisibility(
                visible = pager.currentPage != FeedTab.Following.ordinal && followedLive.isNotEmpty(),
                enter = androidx.compose.animation.slideInHorizontally { it } + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutHorizontally { it } + androidx.compose.animation.fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 14.dp)
            ) {
                FollowLivePile(followedLive) { nav.push(Screen.LiveViewer, followedLive.first().id) }
            }
        }
    }

    if (showRegions) {
        IshqSheet(onDismiss = { showRegions = false }, title = "Show lives from") {
            feedRegions.forEachIndexed { i, (flag, name) ->
                Row(
                    Modifier.fillMaxWidth().clickable { region = i; showRegions = false }.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(flag, fontSize = 22.sp)
                    Spacer(Modifier.width(14.dp))
                    Text(name, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                    if (i == region) Icon(Icons.Rounded.CheckCircle, null, tint = BrandPink)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ------------------------------ Top bar ------------------------------ */

@Composable
private fun HomeTopBar(onAction: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandLogo(34.dp)
        Spacer(Modifier.width(8.dp))
        Wordmark(21.sp)
        Spacer(Modifier.weight(1f))
        TopIcon(Icons.Rounded.Search, "Search") { onAction("Search") }
        Spacer(Modifier.width(6.dp))
        // Your profile (coins, wallet and settings live there)
        Box(
            Modifier.size(38.dp).shadow(10.dp, CircleShape, ambientColor = NeonPink, spotColor = NeonViolet)
                .neonBorder(CircleShape, 2.dp).padding(2.5.dp).clip(CircleShape).clickable { onAction("Me") }
        ) {
            AsyncImage(com.live.ishqexpress.ui.common.Me.avatarUrl, "My profile", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().background(SurfaceMuted))
        }
    }
}

@Composable
private fun TopIcon(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, desc, tint = Ink, modifier = Modifier.size(24.dp)) }
}

/* ------------------------------ Tabs ------------------------------ */

@Composable
private fun FeedTabs(selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(FeedTab.entries.size) { i ->
            val active = i == selected
            val color by animateColorAsState(if (active) Ink else InkTertiary, label = "tab")
            val barWidth by animateDpAsState(if (active) 18.dp else 0.dp, label = "bar")
            Column(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(i) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    FeedTab.entries[i].label,
                    style = if (active) MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp)
                    else MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    color = color
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .width(barWidth)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(BrandBrush.primary)
                )
            }
        }
    }
}

/* ------------------------------ Feed ------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedPage(
    tab: FeedTab,
    region: Int,
    onRegion: () -> Unit,
    onCreator: (LiveCreator) -> Unit,
    onShort: (Int) -> Unit,
    onPromo: (Int) -> Unit,
    onMidPromo: (Int) -> Unit,
) {
    var refreshing by remember { mutableStateOf(false) }
    LaunchedEffect(refreshing) { if (refreshing) { delay(1200); refreshing = false } }

    val creators = remember(tab) {
        when (tab) {
            FeedTab.ForYou -> sampleCreators
            FeedTab.Following -> sampleCreators.filter { it.verified } + sampleCreators.take(2)
            FeedTab.Popular -> sampleCreators.sortedByDescending { it.viewers }
            FeedTab.Nearby -> sampleCreators.sortedBy { it.distanceKm }
        }.distinctBy { it.id }
    }

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = { refreshing = true }, modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (tab) {
                FeedTab.ForYou -> {
                    fullSpan { PromoCarousel(samplePromos, onPromo) }
                    fullSpan { GlobalHeader(region, onRegion) }
                }
                FeedTab.Following -> fullSpan { FollowingHeader(creators.size) }
                FeedTab.Popular -> {
                    fullSpan { GlobalHeader(region, onRegion) }
                    fullSpan { SectionTitle("Top live this hour", "👑", action = null) }
                }
                FeedTab.Nearby -> fullSpan { NearbyHeader() }
            }
            // The second poster slider sits after the first two rows, so it shows once you scroll a little.
            val midAt = if (tab == FeedTab.ForYou && creators.size > 4) 4 else -1
            fun tile(index: Int, creator: LiveCreator) = item(key = "${tab.name}-${creator.id}") {
                LiveTile(
                    creator = creator,
                    rank = if (tab == FeedTab.Popular && index < 3) index + 1 else null,
                    showDistance = tab == FeedTab.Nearby,
                    onClick = { onCreator(creator) }
                )
            }
            // Short videos mixed in: one tile after every 3 lives, and a Shorts rail after the 6th live.
            val shorts = com.live.ishqexpress.ui.shorts.ShortsStore.videos.filter { it.creator != 0 }
            val mixShorts = tab != FeedTab.Following && shorts.isNotEmpty()
            var shortIx = (tab.ordinal * 3) % shorts.size.coerceAtLeast(1)
            creators.forEachIndexed { index, creator ->
                if (index == midAt) fullSpan { PromoCarousel(sampleMidPromos, onMidPromo) }
                if (mixShorts && index == 6) fullSpan { ShortsRail(shorts, onShort) }
                tile(index, creator)
                if (mixShorts && index % 3 == 2) {
                    val v = shorts[shortIx % shorts.size]; shortIx++
                    item(key = "${tab.name}-short-${v.id}-$index") {
                        com.live.ishqexpress.ui.shorts.ShortTile(v) { onShort(com.live.ishqexpress.ui.shorts.ShortsStore.videos.indexOf(v).coerceAtLeast(0)) }
                    }
                }
            }
            // Room so the last row is not hidden behind the floating follow pile.
            fullSpan { Spacer(Modifier.height(64.dp)) }
        }
    }
}

/** Horizontal strip of short videos inside the Home feed. */
@Composable
private fun ShortsRail(shorts: List<com.live.ishqexpress.ui.shorts.ShortVideo>, onShort: (Int) -> Unit) {
    Column {
        Row(Modifier.padding(top = 6.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.SlowMotionVideo, null, tint = NeonCyan, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(6.dp))
            GradientText("Shorts for you", MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            Text("See all", style = MaterialTheme.typography.titleSmall, color = BrandPink, modifier = Modifier.clickable { onShort(0) })
            Icon(Icons.Rounded.ChevronRight, null, tint = BrandPink, modifier = Modifier.size(18.dp))
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(shorts.size) { i ->
                val v = shorts[i]
                Box(Modifier.width(118.dp)) {
                    com.live.ishqexpress.ui.shorts.ShortTile(v) { onShort(com.live.ishqexpress.ui.shorts.ShortsStore.videos.indexOf(v).coerceAtLeast(0)) }
                }
            }
        }
    }
}

private fun LazyGridScope.fullSpan(content: @Composable () -> Unit) =
    item(span = { GridItemSpan(maxLineSpan) }) { content() }

@Composable
private fun SectionTitle(title: String, emoji: String, action: String? = "See all") {
    Row(Modifier.padding(top = 6.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$title ", style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        if (action != null) {
            Text(action, style = MaterialTheme.typography.titleSmall, color = BrandPink)
            Icon(Icons.Rounded.ChevronRight, null, tint = BrandPink, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PromoCarousel(promos: List<Promo>, onPromo: (Int) -> Unit) {
    val pager = rememberPagerState { promos.size }
    LaunchedEffect(pager) {
        while (true) {
            delay(4000)
            pager.animateScrollToPage((pager.currentPage + 1) % promos.size)
        }
    }
    Column {
        HorizontalPager(pager, pageSpacing = 10.dp) { i ->
            val p = promos[i]
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(p.colors))
                    .clickable { onPromo(i) }
            ) {
                // decorative orbs
                Box(Modifier.offset(x = 230.dp, y = (-40).dp).size(150.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)))
                Box(Modifier.offset(x = 280.dp, y = 50.dp).size(110.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)))
                Text(p.emoji, fontSize = 52.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 22.dp))
                Column(
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 18.dp, end = 110.dp)
                ) {
                    Text(p.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(p.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.88f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        p.cta,
                        style = MaterialTheme.typography.labelMedium,
                        color = p.colors.first(),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.align(Alignment.CenterHorizontally), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(promos.size) { i ->
                val active = pager.currentPage == i
                val w by animateDpAsState(if (active) 16.dp else 6.dp, label = "dot")
                Box(
                    Modifier
                        .width(w)
                        .height(6.dp)
                        .clip(CircleShape)
                        .then(if (active) Modifier.background(BrandBrush.primary) else Modifier.background(Hairline))
                )
            }
        }
    }
}

@Composable
private fun FollowingHeader(count: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PinkTint)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveDot()
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("$count people you follow are live", style = MaterialTheme.typography.titleSmall, color = Ink)
            Text("Jump in before the PK battle ends", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = BrandPink)
    }
}

@Composable
private fun NearbyHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .clip(CircleShape)
                .background(SurfaceMuted)
                .padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.LocationOn, null, tint = BrandPink, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Jaipur, Rajasthan", style = MaterialTheme.typography.titleSmall, color = Ink)
            Icon(Icons.Rounded.KeyboardArrowDown, null, tint = InkTertiary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.weight(1f))
        Text("Within 25 km", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
    }
}

/* ------------------------------ Live tile ------------------------------ */

@Composable
private fun LiveTile(creator: LiveCreator, rank: Int?, showDistance: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .tilt3D()
            .shadow(14.dp, shape, ambientColor = creator.tint.last(), spotColor = creator.tint.last())
            .clip(shape)
            .background(Brush.linearGradient(creator.tint))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = creator.imageUrl,
            contentDescription = creator.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Scrims for legibility
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.28f),
                        0.22f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.72f)
                    )
                )
        )

        // Top row: LIVE + mode, viewers
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LivePill()
            if (creator.mode != LiveMode.Solo) {
                Spacer(Modifier.width(4.dp))
                Text(
                    creator.mode.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.32f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Visibility, null, tint = Color.White, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(3.dp))
                Text(creator.viewers.compact(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }

        if (rank != null) {
            val medal = when (rank) { 1 -> Gold; 2 -> Color(0xFFB8C1CC); else -> Color(0xFFE59A62) }
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(y = (-18).dp)
                    .padding(start = 8.dp)
                    .size(28.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(medal)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("$rank", style = MaterialTheme.typography.labelMedium, color = Color.White) }
        }

        // Glass edge (neon for hot lives) and a light sweep across hot tiles
        Box(
            Modifier.fillMaxSize()
                .then(if (creator.hot) Modifier.neonBorder(shape, 2.dp).shine(3_200, 0.18f)
                      else Modifier.border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))), shape))
        )
        // Bottom: name + meta
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        ) {
            if (creator.hot) {
                Text(
                    "🔥 Hot",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandBrush.primary)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    creator.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (creator.verified) {
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Rounded.Verified, "Verified", tint = Color(0xFFFF7AB6), modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                if (showDistance) "${creator.flag} ${"%.1f".format(creator.distanceKm)} km · ${creator.city}"
                else "${creator.flag} ${creator.tag}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LivePill() {
    Row(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BrandBrush.primary)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveDot(size = 5, color = Color.White)
        Spacer(Modifier.width(4.dp))
        Text("LIVE", style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

@Composable
private fun LiveDot(size: Int = 8, color: Color = LiveRed) {
    val t = rememberInfiniteTransition(label = "live")
    val a by t.animateFloat(0.35f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a")
    Box(
        Modifier
            .size(size.dp)
            .alpha(a)
            .clip(CircleShape)
            .background(color)
    )
}

/* ------------------------------ Global header ------------------------------ */

/** "🌏 Global" bar under the banner; the filter icon picks which region's lives the feed shows. */
@Composable
private fun GlobalHeader(region: Int, onFilter: () -> Unit) {
    val (flag, name) = feedRegions[region]
    Row(
        Modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(16.dp), elevation = 8.dp)
            .clickable(onClick = onFilter)
            .padding(start = 12.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Text(name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, brush = BrandBrush.text), modifier = Modifier.weight(1f))
        Text("${sampleCreators.size} live", style = MaterialTheme.typography.labelMedium, color = InkTertiary)
        Spacer(Modifier.width(6.dp))
        Box(Modifier.size(34.dp).clip(CircleShape).background(PinkTint), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.FilterList, "Filter region", tint = BrandPink, modifier = Modifier.size(20.dp))
        }
    }
}

/* ------------------------------ Follow pile ------------------------------ */

/**
 * Floating card, bottom-right: a fanned pile of the followed creators who are live, a live
 * equaliser and the count. Tapping it opens the first of those lives.
 */
@Composable
private fun FollowLivePile(creators: List<LiveCreator>, onClick: () -> Unit) {
    val t = rememberInfiniteTransition(label = "pile")
    val bob by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1_600), RepeatMode.Reverse), label = "bob")
    val shape = RoundedCornerShape(30.dp)
    Row(
        Modifier
            .graphicsLayer { translationY = -3.dp.toPx() * bob }
            .shadow(14.dp, shape, ambientColor = BrandPink.copy(alpha = 0.5f), spotColor = BrandViolet)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFFFF4F9A), Color(0xFFB03CE6), Color(0xFF6E3BF0))))
            .drawWithContent {
                drawContent()
                drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = 0.28f), 0.45f to Color.Transparent))
            }
            .clickable(onClick = onClick)
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fanned pile of the top three
        Box(Modifier.size(width = 58.dp, height = 46.dp)) {
            val pile = creators.take(3)
            pile.reversed().forEachIndexed { i, c ->
                val depth = pile.size - 1 - i // 0 = front
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (depth * 7).dp)
                        .graphicsLayer { rotationZ = depth * 9f; scaleX = 1f - depth * 0.08f; scaleY = scaleX }
                        .size(42.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(c.tint))
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(c.imageUrl, c.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
        }
        Spacer(Modifier.width(6.dp))
        Column {
            Text("${creators.size} follow talent", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color.White.copy(alpha = 0.9f), maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                EqualizerBars()
                Spacer(Modifier.width(5.dp))
                Text("Live", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Color.White)
            }
        }
    }
}

@Composable
private fun EqualizerBars() {
    val t = rememberInfiniteTransition(label = "eq")
    Row(Modifier.height(14.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        listOf(420, 300, 520).forEach { ms ->
            val h by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(ms), RepeatMode.Reverse), label = "bar")
            Box(Modifier.width(3.dp).fillMaxHeight(h).clip(RoundedCornerShape(2.dp)).background(Color.White))
        }
    }
}
