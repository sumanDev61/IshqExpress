package com.live.ishqexpress.ui.components

import com.live.ishqexpress.ui.theme.appBackground
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.live.ishqexpress.R
import com.live.ishqexpress.ui.theme.BrandBrush
import com.live.ishqexpress.ui.theme.BrandPink
import com.live.ishqexpress.ui.theme.Canvas
import com.live.ishqexpress.ui.theme.DisabledFill
import com.live.ishqexpress.ui.theme.Hairline
import com.live.ishqexpress.ui.theme.Ink
import com.live.ishqexpress.ui.theme.InkSecondary
import com.live.ishqexpress.ui.theme.InkTertiary
import com.live.ishqexpress.ui.theme.SurfaceMuted

/* ---------- Brand ---------- */

@Composable
fun BrandLogo(size: Dp, modifier: Modifier = Modifier, elevation: Dp = 0.dp) {
    val shape = RoundedCornerShape(size * 0.24f)
    Image(
        painter = painterResource(R.drawable.ishq_logo),
        contentDescription = "ishqexpress",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .shadow(elevation, shape, ambientColor = BrandPink, spotColor = BrandPink)
            .clip(shape)
    )
}

/** "ishq" in ink + "express" in the brand gradient, like the logo lockup. */
@Composable
fun Wordmark(fontSize: TextUnit, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            "ishq",
            style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
            color = Ink
        )
        Text(
            "express",
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                brush = BrandBrush.text
            )
        )
    }
}

/* ---------- Buttons ---------- */

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "press")
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .then(
                if (enabled) Modifier.shadow(10.dp, shape, ambientColor = BrandPink, spotColor = BrandPink)
                else Modifier
            )
            .clip(shape)
            .then(if (enabled) Modifier.background(BrandBrush.primary) else Modifier.background(DisabledFill))
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = if (enabled) Color.White else InkTertiary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Color.White else InkTertiary
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = InkSecondary)
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = Ink,
    background: Color = SurfaceMuted,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/* ---------- Onboarding scaffold ---------- */

/**
 * Shared frame for every onboarding step: back button + optional segmented progress,
 * title block, scrollable body, and a pinned footer that rides above the keyboard.
 */
@Composable
fun OnboardingFrame(
    title: String,
    subtitle: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    step: Int? = null,
    totalSteps: Int = 3,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .appBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
            } else {
                Spacer(Modifier.size(44.dp))
            }
            if (step != null) {
                Spacer(Modifier.width(16.dp))
                StepProgress(step, totalSteps, Modifier.weight(1f))
                Spacer(Modifier.width(16.dp))
                Text(
                    "$step/$totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkTertiary
                )
            }
        }
        Column(Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Ink)
            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = InkSecondary)
            }
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { content() }
        Column(
            Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = footer
        )
    }
}

@Composable
fun StepProgress(step: Int, total: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            val active = i < step
            val color by animateColorAsState(if (active) BrandPink else Hairline, label = "seg")
            Box(
                Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .then(
                        if (active) Modifier.background(BrandBrush.primary)
                        else Modifier.background(color)
                    )
            )
        }
    }
}

/* ---------- Misc ---------- */

@Composable
fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val border by animateColorAsState(if (selected) BrandPink else Hairline, label = "border")
    val bg by animateColorAsState(if (selected) com.live.ishqexpress.ui.theme.PinkTint else Canvas, label = "bg")
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(if (selected) 1.5.dp else 1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/* ---------- Brand glyphs (Material Design Icons community paths) ---------- */

private fun glyph(name: String, path: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
        .build()

object BrandGlyphs {
    val Google = glyph(
        "google",
        "M21.35,11.1H12.18V13.83H18.69C18.36,17.64 15.19,19.27 12.19,19.27C8.36,19.27 5,16.25 5,12C5,7.9 8.2,4.73 12.2,4.73C15.29,4.73 17.1,6.7 17.1,6.7L19,4.72C19,4.72 16.56,2 12.1,2C6.42,2 2.03,6.8 2.03,12C2.03,17.05 6.16,22 12.25,22C17.6,22 21.5,18.33 21.5,12.91C21.5,11.76 21.35,11.1 21.35,11.1V11.1Z"
    )
    val Apple = glyph(
        "apple",
        "M18.71,19.5C17.88,20.74 17,21.95 15.66,21.97C14.32,22 13.89,21.18 12.37,21.18C10.84,21.18 10.37,21.95 9.1,22C7.79,22.05 6.8,20.68 5.96,19.47C4.25,17 2.94,12.45 4.7,9.39C5.57,7.87 7.13,6.91 8.82,6.88C10.1,6.86 11.32,7.75 12.11,7.75C12.89,7.75 14.37,6.68 15.92,6.84C16.57,6.87 18.39,7.1 19.56,8.82C19.47,8.88 17.39,10.1 17.41,12.63C17.44,15.65 20.06,16.66 20.09,16.67C20.06,16.74 19.67,18.11 18.71,19.5M13,3.5C13.73,2.67 14.94,2.04 15.94,2C16.07,3.17 15.6,4.35 14.9,5.19C14.21,6.04 13.07,6.7 11.95,6.61C11.8,5.46 12.36,4.26 13,3.5Z"
    )
    val Facebook = glyph(
        "facebook",
        "M12,2.04C6.5,2.04 2,6.53 2,12.06C2,17.06 5.66,21.21 10.44,21.96V14.96H7.9V12.06H10.44V9.85C10.44,7.34 11.93,5.96 14.22,5.96C15.31,5.96 16.45,6.15 16.45,6.15V8.62H15.19C13.95,8.62 13.56,9.39 13.56,10.18V12.06H16.34L15.89,14.96H13.56V21.96A10,10 0 0,0 22,12.06C22,6.53 17.5,2.04 12,2.04Z"
    )
}
