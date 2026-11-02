package com.live.ishqexpress.ui.common

import android.app.Activity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.components.CircleIconButton
import com.live.ishqexpress.ui.theme.BrandBrush
import com.live.ishqexpress.ui.theme.BrandOrchid
import com.live.ishqexpress.ui.theme.BrandPink
import com.live.ishqexpress.ui.theme.BrandViolet
import com.live.ishqexpress.ui.theme.Canvas
import com.live.ishqexpress.ui.theme.Gold
import com.live.ishqexpress.ui.theme.Hairline
import com.live.ishqexpress.ui.theme.Ink
import com.live.ishqexpress.ui.theme.InkSecondary
import com.live.ishqexpress.ui.theme.InkTertiary
import com.live.ishqexpress.ui.theme.LiveRed
import com.live.ishqexpress.ui.theme.PinkTint
import com.live.ishqexpress.ui.theme.SurfaceMuted

/* ============================== System bars ============================== */

/** Dark-background screens (live, calls) flip status-bar icons to white while shown. */
@Composable
fun DarkSystemBars() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = true
            controller?.isAppearanceLightNavigationBars = true
        }
    }
}

/* ============================== Scaffolds ============================== */

/** White screen with back button, centred-left title and optional trailing actions. */
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
    ) {
        TopBar(title, onBack, actions)
        Column(Modifier.weight(1f).fillMaxWidth(), content = content)
        if (bottomBar != null) {
            Box(Modifier.navigationBarsPadding()) { bottomBar() }
        }
    }
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)?, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack, size = 40.dp, background = Color.Transparent)
            Spacer(Modifier.width(4.dp))
        } else Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = Ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        actions()
    }
}

@Composable
fun TopAction(icon: ImageVector, desc: String, tint: Color = Ink, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, desc, tint = tint, modifier = Modifier.size(24.dp)) }
}

/** Light-on-white bottom sheet used across the app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IshqSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    dark: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (dark) Color(0xFF17101F) else Canvas,
        scrimColor = if (dark) Color.Black.copy(alpha = 0.12f) else androidx.compose.material3.BottomSheetDefaults.ScrimColor,
        contentColor = if (dark) Color.White else Ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 12.dp)) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (dark) Color.White else Ink,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}

/* ============================== Avatars & badges ============================== */

@Composable
fun Avatar(
    url: String,
    size: Dp,
    modifier: Modifier = Modifier,
    ring: Boolean = false,
    online: Boolean = false,
    live: Boolean = false,
) {
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .then(if (ring || live) Modifier.background(BrandBrush.deep).padding(2.dp).clip(CircleShape).background(Canvas).padding(2.dp) else Modifier)
                .clip(CircleShape)
                .background(SurfaceMuted)
        ) {
            AsyncImage(url, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        if (live) {
            Text(
                "LIVE",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                color = Color.White,
                modifier = Modifier
                    .offset(y = 4.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(BrandBrush.primary)
                    .border(1.dp, Canvas, RoundedCornerShape(5.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        } else if (online) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.26f)
                    .clip(CircleShape)
                    .background(Canvas)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(com.live.ishqexpress.ui.theme.Success)
            )
        }
    }
}

/** Wealth level (1–50) pill shown next to names. */
@Composable
fun LevelBadge(level: Int, modifier: Modifier = Modifier) {
    val colors = when {
        level >= 40 -> listOf(Color(0xFFFF2E8B), Color(0xFF7A2BE8))
        level >= 25 -> listOf(Color(0xFFC236D9), Color(0xFF8D5BEA))
        level >= 10 -> listOf(Color(0xFF5B7BEA), Color(0xFF8D5BEA))
        else -> listOf(Color(0xFF5BC0BE), Color(0xFF3B9AE0))
    }
    Text(
        "Lv$level",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.horizontalGradient(colors))
            .padding(horizontal = 5.dp, vertical = 1.5.dp)
    )
}

@Composable
fun VipBadge(tier: Int, modifier: Modifier = Modifier) {
    val t = vipTiers[(tier - 1).coerceIn(0, vipTiers.lastIndex)]
    Text(
        "VIP$tier",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(t.color)
            .padding(horizontal = 5.dp, vertical = 1.5.dp)
    )
}

@Composable
fun FanBadge(modifier: Modifier = Modifier) {
    Text(
        "♥ Fan",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(BrandPink)
            .padding(horizontal = 5.dp, vertical = 1.5.dp)
    )
}

@Composable
fun NameRow(name: String, verified: Boolean, modifier: Modifier = Modifier, color: Color = Ink, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(name, style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        if (verified) {
            Spacer(Modifier.width(3.dp))
            Icon(Icons.Rounded.Verified, "Verified", tint = BrandPink, modifier = Modifier.size(15.dp))
        }
    }
}

/* ============================== Live indicators ============================== */

@Composable
fun LivePill(modifier: Modifier = Modifier, text: String = "LIVE") {
    Row(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BrandBrush.primary)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveDot(size = 5, color = Color.White)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

@Composable
fun LiveDot(size: Int = 8, color: Color = LiveRed) {
    val t = rememberInfiniteTransition(label = "live")
    val a by t.animateFloat(0.35f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a")
    Box(Modifier.size(size.dp).alpha(a).clip(CircleShape).background(color))
}

/* ============================== Currency ============================== */

/** Coins — what viewers buy and spend. */
@Composable
fun CoinIcon(size: Dp = 16.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFFFD25A), Gold, Color(0xFFF08A00)))),
        contentAlignment = Alignment.Center
    ) {
        Text("C", fontSize = (size.value * 0.55f).sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

/** Diamonds — what creators earn. */
@Composable
fun DiamondIcon(size: Dp = 16.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(BrandBrush.primary),
        contentAlignment = Alignment.Center
    ) { Icon(Icons.Rounded.Diamond, null, tint = Color.White, modifier = Modifier.size(size * 0.62f)) }
}

@Composable
fun CoinAmount(amount: Int, modifier: Modifier = Modifier, color: Color = Ink, diamond: Boolean = false, iconSize: Dp = 14.dp, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelMedium) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (diamond) DiamondIcon(iconSize) else CoinIcon(iconSize)
        Spacer(Modifier.width(4.dp))
        Text(amount.grouped(), style = style, color = color)
    }
}

fun Int.grouped(): String = "%,d".format(this)

/* ============================== Buttons & chips ============================== */

@Composable
fun SmallGradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, height: Dp = 36.dp) {
    Row(
        modifier
            .height(height)
            .clip(CircleShape)
            .background(BrandBrush.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp), color = Color.White)
    }
}

@Composable
fun SmallOutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, height: Dp = 36.dp, color: Color = Ink) {
    Row(
        modifier
            .height(height)
            .clip(CircleShape)
            .border(1.dp, Hairline, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp), color = color)
    }
}

@Composable
fun PillChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
        color = if (selected) Color.White else InkSecondary,
        modifier = modifier
            .clip(CircleShape)
            .then(if (selected) Modifier.background(BrandBrush.primary) else Modifier.background(SurfaceMuted))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

/** Underlined text tabs (Followers / Following, Daily / Weekly …). */
@Composable
fun UnderlineTabs(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, dark: Boolean = false) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            tabs.forEachIndexed { i, t ->
                val active = i == selected
                Column(
                    Modifier.weight(1f).clickable { onSelect(i) }.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        t,
                        style = MaterialTheme.typography.titleSmall,
                        color = when {
                            active && dark -> Color.White
                            active -> Ink
                            dark -> Color.White.copy(alpha = 0.5f)
                            else -> InkTertiary
                        }
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier.width(if (active) 20.dp else 0.dp).height(3.dp).clip(CircleShape).background(BrandBrush.primary)
                    )
                }
            }
        }
        if (!dark) HorizontalDivider(color = Hairline, thickness = 0.5.dp)
    }
}

/* ============================== Rows & cards ============================== */

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
        if (action != null) {
            Text(action, style = MaterialTheme.typography.titleSmall, color = BrandPink, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

@Composable
fun MenuRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = BrandPink,
    iconBg: Color = PinkTint,
    trailingText: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Ink)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        if (trailingText != null) {
            Text(trailingText, style = MaterialTheme.typography.bodySmall, color = InkTertiary)
            Spacer(Modifier.width(4.dp))
        }
        if (trailing != null) trailing()
        else if (onClick != null) Icon(Icons.Rounded.ChevronRight, null, tint = InkTertiary)
    }
}

@Composable
fun ToggleRow(icon: ImageVector, title: String, checked: Boolean, onChange: (Boolean) -> Unit, subtitle: String? = null) {
    MenuRow(icon, title, subtitle = subtitle, trailing = { IshqSwitch(checked, onChange) }, onClick = { onChange(!checked) })
}

@Composable
fun IshqSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = BrandPink,
            checkedThumbColor = Color.White,
            uncheckedTrackColor = Hairline,
            uncheckedBorderColor = Hairline,
            uncheckedThumbColor = Color.White,
        )
    )
}

@Composable
fun IshqCard(modifier: Modifier = Modifier, padding: Dp = 16.dp, background: Color = Canvas, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, Hairline, RoundedCornerShape(20.dp))
            .padding(padding),
        content = content
    )
}

@Composable
fun GradientCard(modifier: Modifier = Modifier, brush: Brush = BrandBrush.deep, padding: Dp = 18.dp, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(brush)
    ) {
        Box(Modifier.align(Alignment.TopEnd).offset(x = 40.dp, y = (-50).dp).size(160.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)))
        Box(Modifier.align(Alignment.BottomEnd).offset(x = (-30).dp, y = 60.dp).size(110.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.07f)))
        Box(Modifier.padding(padding), content = content)
    }
}

@Composable
fun Stat(value: String, label: String, modifier: Modifier = Modifier, color: Color = Ink, labelColor: Color = InkSecondary) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = labelColor)
    }
}

/** Gradient progress bar used by goals, levels, VIP and battles. */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, height: Dp = 8.dp, track: Color = Hairline, brush: Brush = BrandBrush.primary) {
    val f by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(600), label = "progress")
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(track)) {
        Box(Modifier.fillMaxWidth(f).height(height).clip(CircleShape).background(brush))
    }
}

@Composable
fun EmptyState(emoji: String, title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = InkSecondary, textAlign = TextAlign.Center)
    }
}

/* ============================== Dark (live) overlays ============================== */

/** Translucent round button for controls drawn over video. */
@Composable
fun GlassButton(icon: ImageVector, desc: String?, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 40.dp, tint: Color = Color.White, highlighted: Boolean = false) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .then(if (highlighted) Modifier.background(BrandBrush.primary) else Modifier.background(Color.Black.copy(alpha = 0.32f)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, desc, tint = tint, modifier = Modifier.size(size * 0.52f)) }
}

@Composable
fun GlassPill(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.32f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Full-bleed photo standing in for a video surface, with top/bottom scrims. */
@Composable
fun VideoBackdrop(url: String, tint: List<Color>, modifier: Modifier = Modifier, scrim: Boolean = true) {
    Box(modifier.fillMaxSize().background(Brush.linearGradient(tint))) {
        AsyncImage(url, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        if (scrim) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.45f),
                        0.2f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
        }
    }
}

val NightBrush = Brush.verticalGradient(listOf(Color(0xFF2A0B4A), Color(0xFF120818)))
val BattleBlue = Color(0xFF3B6FE0)
val brandPair = listOf(BrandPink, BrandOrchid)
val violetPair = listOf(BrandOrchid, BrandViolet)
