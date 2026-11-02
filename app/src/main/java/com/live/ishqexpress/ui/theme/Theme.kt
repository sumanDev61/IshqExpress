package com.live.ishqexpress.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IshqColorScheme = lightColorScheme(
    primary = BrandPink,
    onPrimary = Color.White,
    primaryContainer = PinkTint,
    onPrimaryContainer = BrandPinkDark,
    secondary = BrandViolet,
    onSecondary = Color.White,
    secondaryContainer = VioletTint,
    onSecondaryContainer = BrandViolet,
    tertiary = BrandOrchid,
    background = Canvas,
    onBackground = Ink,
    surface = Canvas,
    onSurface = Ink,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = InkSecondary,
    surfaceContainer = Canvas,
    surfaceContainerHigh = Canvas,
    surfaceContainerHighest = SurfaceMuted,
    outline = Hairline,
    outlineVariant = Hairline,
    error = LiveRed,
)

/** ishqexpress is a light-first brand: white canvas, logo colours as accents. */
@Composable
fun IshqExpressTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IshqColorScheme,
        typography = Typography,
        content = content
    )
}
