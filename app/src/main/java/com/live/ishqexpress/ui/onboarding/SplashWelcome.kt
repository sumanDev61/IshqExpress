package com.live.ishqexpress.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.components.BrandGlyphs
import com.live.ishqexpress.ui.components.BrandLogo
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.components.Wordmark
import com.live.ishqexpress.ui.home.LiveCreator
import com.live.ishqexpress.ui.home.compact
import com.live.ishqexpress.ui.home.sampleCreators
import com.live.ishqexpress.ui.theme.BrandBrush
import com.live.ishqexpress.ui.theme.BrandOrchid
import com.live.ishqexpress.ui.theme.BrandPink
import com.live.ishqexpress.ui.theme.Canvas
import com.live.ishqexpress.ui.theme.Hairline
import com.live.ishqexpress.ui.theme.Ink
import com.live.ishqexpress.ui.theme.InkSecondary
import com.live.ishqexpress.ui.theme.InkTertiary
import com.live.ishqexpress.ui.theme.LiveRed
import com.live.ishqexpress.ui.theme.PinkTint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ============================== 1. Splash ============================== */

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.82f) }
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { logoScale.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        contentAlpha.animateTo(1f, tween(600, delayMillis = 200))
        delay(1500)
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Canvas)
    ) {
        // Soft brand glow so the white canvas doesn't feel empty
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(PinkTint, Canvas),
                        radius = 900f
                    )
                )
        )
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandLogo(
                120.dp,
                Modifier.scale(logoScale.value),
                elevation = 24.dp
            )
            Spacer(Modifier.height(28.dp))
            Column(Modifier.alpha(contentAlpha.value), horizontalAlignment = Alignment.CenterHorizontally) {
                Wordmark(36.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "NEW ERA OF DATING",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                    color = InkTertiary
                )
            }
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PulsingDots()
            Spacer(Modifier.height(16.dp))
            Text("Made with ❤ in India", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
        }
    }
}

@Composable
private fun PulsingDots() {
    val t = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { i ->
            val a by t.animateFloat(
                0.25f, 1f,
                infiniteRepeatable(tween(500, delayMillis = i * 160), RepeatMode.Reverse),
                label = "dot$i"
            )
            Box(
                Modifier
                    .size(8.dp)
                    .alpha(a)
                    .clip(CircleShape)
                    .background(BrandBrush.primary)
            )
        }
    }
}

/* ============================== 2. Welcome ============================== */

@Composable
fun WelcomeScreen(
    onPhone: () -> Unit,
    onSocial: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Canvas)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandLogo(32.dp)
            Spacer(Modifier.width(10.dp))
            Wordmark(20.sp)
        }

        HeroCollage(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Column(Modifier.padding(horizontal = 24.dp)) {
            Text(
                buildAnnotatedString {
                    append("Where every heartbeat\ngoes ")
                    withStyle(SpanStyle(brush = BrandBrush.text)) { append("live.") }
                },
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp, lineHeight = 38.sp),
                color = Ink
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Watch creators, send gifts and make real connections — all in one place.",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSecondary
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Continue with phone", onPhone, leadingIcon = Icons.Rounded.Phone)
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = Hairline)
                Text(
                    "or continue with",
                    Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = InkTertiary
                )
                HorizontalDivider(Modifier.weight(1f), color = Hairline)
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SocialButton(BrandGlyphs.Google, "Google", Color(0xFF4285F4), Modifier.weight(1f)) { onSocial("google") }
                SocialButton(BrandGlyphs.Apple, "Apple", Ink, Modifier.weight(1f)) { onSocial("apple") }
                SocialButton(BrandGlyphs.Facebook, "Facebook", Color(0xFF1877F2), Modifier.weight(1f)) { onSocial("facebook") }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                buildAnnotatedString {
                    append("By continuing you confirm you're 18+ and agree to our ")
                    withStyle(SpanStyle(color = BrandPink, fontWeight = FontWeight.SemiBold)) { append("Terms") }
                    append(" and ")
                    withStyle(SpanStyle(color = BrandPink, fontWeight = FontWeight.SemiBold)) { append("Privacy Policy") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = InkTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun SocialButton(icon: ImageVector, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Hairline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, "Continue with $label", tint = tint, modifier = Modifier.size(22.dp))
    }
}

/** Three overlapping live cards + floating gift/heart chips — sells the product at a glance. */
@Composable
private fun HeroCollage(modifier: Modifier) {
    val float = rememberInfiniteTransition(label = "float")
    val bob by float.animateFloat(
        -6f, 6f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val cardH = (maxHeight * 0.82f).coerceAtMost(300.dp)
        val cardW = cardH * 0.7f
        val side = cardW * 0.8f
        val c = sampleCreators

        HeroCard(c[2], side * 1.0f, side * 1.42f, Modifier.offset(x = -cardW * 0.62f, y = 12.dp).rotate(-9f))
        HeroCard(c[5], side * 1.0f, side * 1.42f, Modifier.offset(x = cardW * 0.62f, y = 12.dp).rotate(9f))
        HeroCard(c[0], cardW, cardH, Modifier.shadow(24.dp, RoundedCornerShape(24.dp), spotColor = BrandPink), showMeta = true)

        // Floating chips
        FloatingChip(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 8.dp)
                .graphicsLayer { translationY = bob * 2 }
        ) {
            Text("🌹", fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text("Rose ×99", style = MaterialTheme.typography.labelMedium, color = Ink)
        }
        FloatingChip(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 6.dp)
                .graphicsLayer { translationY = -bob * 2 }
        ) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(BrandBrush.primary),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.Favorite, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
            Spacer(Modifier.width(6.dp))
            Text("It's a match!", style = MaterialTheme.typography.labelMedium, color = Ink)
        }
    }
}

@Composable
private fun FloatingChip(modifier: Modifier, content: @Composable () -> Unit) {
    Row(
        modifier
            .shadow(12.dp, CircleShape, spotColor = BrandOrchid)
            .clip(CircleShape)
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { content() }
}

@Composable
private fun HeroCard(creator: LiveCreator, w: Dp, h: Dp, modifier: Modifier = Modifier, showMeta: Boolean = false) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier
            .size(w, h)
            .clip(shape)
            .background(Brush.linearGradient(creator.tint))
            .border(3.dp, Color.White, shape)
    ) {
        AsyncImage(
            model = creator.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.65f)))
        )
        if (showMeta) {
            Row(
                Modifier
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(LiveRed)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                Spacer(Modifier.width(4.dp))
                Text("LIVE", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            Row(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Visibility, null, tint = Color.White, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(3.dp))
                Text(creator.viewers.compact(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(creator.name, style = MaterialTheme.typography.titleSmall, color = Color.White)
                Text("${creator.flag} ${creator.tag}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
        }
    }
}
