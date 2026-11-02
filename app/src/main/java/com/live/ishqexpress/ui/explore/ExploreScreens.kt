package com.live.ishqexpress.ui.explore

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
import androidx.compose.runtime.saveable.rememberSaveable
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

/* ============================== 8 · Explore ============================== */

@Composable
fun ExploreScreen() {
    val nav = LocalNav.current
    var category by rememberSaveable { mutableIntStateOf(-1) }

    Column(Modifier.fillMaxSize().background(Canvas).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 10.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Explore", style = MaterialTheme.typography.headlineSmall, color = Ink, modifier = Modifier.weight(1f))
            TopAction(Icons.Outlined.EmojiEvents, "Leaderboards") { nav.push(Screen.Leaderboards) }
        }
        // Search entry
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceMuted)
                .clickable { nav.push(Screen.Search) }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Search, null, tint = InkTertiary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Search creators, hashtags, ID", style = MaterialTheme.typography.bodyMedium, color = InkTertiary)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { ShortcutRow() }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("Categories", style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.padding(top = 6.dp))
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(170.dp)
                ) {
                    itemsIndexed(sampleCategories) { i, c ->
                        CategoryTile(c, selected = category == i) { category = if (category == i) -1 else i }
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (category >= 0) "${sampleCategories[category].emoji} ${sampleCategories[category].label} live" else "Recommended live",
                        style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f)
                    )
                    if (category >= 0) Text("Clear", style = MaterialTheme.typography.titleSmall, color = BrandPink, modifier = Modifier.clickable { category = -1 })
                }
            }
            val list = if (category < 0) sampleCreators else sampleCreators.shuffled(kotlin.random.Random(category)).take(6)
            items(list, key = { "explore-${it.id}" }) { c -> MiniLiveTile(c) { nav.push(Screen.LiveViewer, c.id) } }
        }
    }
}

@Composable
private fun ShortcutRow() {
    val nav = LocalNav.current
    data class Shortcut(val emoji: String, val label: String, val colors: List<Color>, val go: () -> Unit)
    val items = listOf(
        Shortcut("🎙️", "Chat party", listOf(Color(0xFF8D5BEA), Color(0xFFC07BF0))) { nav.push(Screen.AudioRooms) },
        Shortcut("🎥", "Video party", listOf(Color(0xFFFF9A8B), Color(0xFFC236D9))) { nav.push(Screen.LiveViewer, 3) },
        Shortcut("💘", "Match", listOf(Color(0xFFFF6FA5), Color(0xFFFF2E8B))) { nav.push(Screen.Match) },
        Shortcut("⚔️", "PK", listOf(Color(0xFF3B6FE0), Color(0xFFFF2E8B))) { nav.push(Screen.LiveViewer, 2) },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { s ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(SurfaceMuted).clickable(onClick = s.go).padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(Brush.linearGradient(s.colors)), contentAlignment = Alignment.Center) {
                    Text(s.emoji, fontSize = 20.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(s.label, style = MaterialTheme.typography.labelMedium, color = Ink, maxLines = 1)
            }
        }
    }
}

@Composable
private fun CategoryTile(c: Category, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(width = 150.dp, height = 80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(c.colors))
            .then(if (selected) Modifier.border(3.dp, Ink, RoundedCornerShape(16.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(c.emoji, fontSize = 34.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = 6.dp))
        Column {
            Text(c.label, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("${c.live.compact()} live", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

/** Compact live tile reused by Explore, Search and profiles. */
@Composable
fun MiniLiveTile(c: LiveCreator, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(c.tint))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(c.imageUrl, c.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.7f))))
        Row(Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LivePill()
            Spacer(Modifier.weight(1f))
            GlassPill {
                Icon(Icons.Rounded.Visibility, null, tint = Color.White, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(3.dp))
                Text(c.viewers.compact(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
        Column(Modifier.align(Alignment.BottomStart).padding(10.dp)) {
            NameRow(c.name, c.verified, color = Color.White)
            Text("${c.flag} ${c.tag}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color.White.copy(alpha = 0.85f))
        }
    }
}

/* ============================== 9 · Search ============================== */

@Composable
fun SearchScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    var query by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val recent = remember { mutableStateListOf("Aanya", "#Shayari", "PK battle") }
    val following = remember { mutableStateListOf<Int>() }

    Column(Modifier.fillMaxSize().background(Canvas).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            TopAction(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onClick = onBack)
            Row(
                Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceMuted).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, null, tint = InkTertiary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) Text("Search creators, hashtags, ID", style = MaterialTheme.typography.bodyMedium, color = InkTertiary)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(BrandPink),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    Icon(Icons.Rounded.Cancel, "Clear", tint = InkTertiary, modifier = Modifier.size(18.dp).clickable { query = "" })
                }
            }
        }

        if (query.isBlank()) {
            LazyColumn(Modifier.fillMaxSize()) {
                if (recent.isNotEmpty()) {
                    item { SectionLabel("Recent", action = "Clear all", onAction = { recent.clear() }) }
                    items(recent.toList()) { r ->
                        Row(Modifier.fillMaxWidth().clickable { query = r }.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.History, null, tint = InkTertiary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(r, style = MaterialTheme.typography.bodyLarge, color = Ink, modifier = Modifier.weight(1f))
                            Icon(Icons.Rounded.Close, "Remove", tint = InkTertiary, modifier = Modifier.size(18.dp).clickable { recent.remove(r) })
                        }
                    }
                }
                item { SectionLabel("Trending hashtags") }
                item {
                    FlowRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        trendingTags.forEachIndexed { i, t ->
                            Row(
                                Modifier.clip(CircleShape).background(if (i < 3) PinkTint else SurfaceMuted).clickable { query = t }.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (i < 3) Text("🔥 ", fontSize = 12.sp)
                                Text(t, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp), color = if (i < 3) BrandPinkDark else Ink)
                            }
                        }
                    }
                }
                item { SectionLabel("Suggested creators") }
                items(sampleCreators.take(6), key = { it.id }) { c ->
                    CreatorRow(c, followed = c.id in following, onFollow = { if (c.id in following) following.remove(c.id) else following.add(c.id) }) {
                        nav.push(Screen.OtherProfile, c.id)
                    }
                }
            }
        } else {
            UnderlineTabs(listOf("Creators", "Live", "Hashtags"), tab, { tab = it })
            val q = query.trim().removePrefix("#").lowercase()
            val creators = sampleCreators.filter { it.name.lowercase().contains(q) || it.tag.lowercase().contains(q) || it.city.lowercase().contains(q) }
            when (tab) {
                0 -> if (creators.isEmpty()) EmptyState("🔍", "No creators found", "Try a different name or their ID.") else
                    LazyColumn { items(creators, key = { it.id }) { c ->
                        CreatorRow(c, followed = c.id in following, onFollow = { if (c.id in following) following.remove(c.id) else following.add(c.id) }) { nav.push(Screen.OtherProfile, c.id) }
                    } }
                1 -> if (creators.isEmpty()) EmptyState("📺", "Nobody live for \"$query\"", "Check back later or follow to get notified.") else
                    LazyVerticalGrid(GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(creators, key = { it.id }) { c -> MiniLiveTile(c) { nav.push(Screen.LiveViewer, c.id) } }
                    }
                else -> {
                    val tags = trendingTags.filter { it.lowercase().contains(q) }.ifEmpty { listOf("#$q") }
                    LazyColumn {
                        items(tags) { t ->
                            Row(Modifier.fillMaxWidth().clickable { tab = 1 }.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(42.dp).clip(CircleShape).background(PinkTint), contentAlignment = Alignment.Center) {
                                    Text("#", style = MaterialTheme.typography.titleLarge, color = BrandPink)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(t, style = MaterialTheme.typography.titleSmall, color = Ink)
                                    Text("${(t.length * 137).compact()} live · ${(t.length * 9_211).compact()} posts", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorRow(c: LiveCreator, followed: Boolean, onFollow: () -> Unit, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(c.imageUrl, 50.dp, live = c.id % 2 == 1)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            NameRow(c.name, c.verified)
            Text("${c.flag} ${c.city} · ${(c.viewers * 7).compact()} followers", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        if (followed) SmallOutlineButton("Following", onFollow, color = InkSecondary)
        else SmallGradientButton("Follow", onFollow, icon = Icons.Rounded.Add)
    }
}
