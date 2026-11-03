package com.live.ishqexpress.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IshqColorScheme = darkColorScheme(
    primary = BrandPink,
    onPrimary = Color.White,
    primaryContainer = PinkTint,
    onPrimaryContainer = Color(0xFFFFB3D6),
    secondary = BrandViolet,
    onSecondary = Color.White,
    secondaryContainer = VioletTint,
    onSecondaryContainer = Color(0xFFD8C2FF),
    tertiary = BrandOrchid,
    background = Canvas,
    onBackground = Ink,
    surface = Canvas,
    onSurface = Ink,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = InkSecondary,
    surfaceContainerLowest = NightDeep,
    surfaceContainerLow = Canvas,
    surfaceContainer = Canvas,
    surfaceContainerHigh = SurfaceMuted,
    surfaceContainerHighest = SurfaceMuted,
    outline = Hairline,
    outlineVariant = Hairline,
    error = LiveRed,
)

/** ishqexpress is dark-first: a night canvas with the logo colours glowing as neon accents. */
@Composable
fun IshqExpressTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IshqColorScheme,
        typography = Typography,
        content = content
    )
}
