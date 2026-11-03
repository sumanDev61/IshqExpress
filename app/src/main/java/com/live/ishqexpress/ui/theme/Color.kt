package com.live.ishqexpress.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand — sampled from the ishqexpress logo (hot pink → orchid → deep violet)
val BrandPink = Color(0xFFFF2E8B)
val BrandPinkDark = Color(0xFFE0167A)
val BrandOrchid = Color(0xFFC236D9)
val BrandViolet = Color(0xFF7A2BE8)
val BrandNight = Color(0xFF2A0B4A)

// Dark, neon-lit surfaces. ishqexpress is dark-first: night canvas, logo colours glow on top.
val PinkTint = Color(0xFF3A1238)
val PinkTintStrong = Color(0xFF55184C)
val VioletTint = Color(0xFF261747)

// Neutrals (names kept from the light theme so every screen picks up the dark palette)
val Canvas = Color(0xFF130A22)
val SurfaceMuted = Color(0xFF221536)
val Hairline = Color(0xFF33244D)
val Ink = Color(0xFFF8F3FF)
val InkSecondary = Color(0xFFBCB0D6)
val InkTertiary = Color(0xFF8679A3)
val DisabledFill = Color(0xFF2B1F42)

// Extra neon accents for glow effects
val NeonCyan = Color(0xFF39D5FF)
val NeonPink = Color(0xFFFF4FB8)
val NeonViolet = Color(0xFF9B5CFF)
val NightDeep = Color(0xFF07030F)

// Accents
val LiveRed = Color(0xFFFF2D55)
val Gold = Color(0xFFFFB020)
val Success = Color(0xFF12B76A)

object BrandBrush {
    val primary = Brush.horizontalGradient(listOf(BrandPink, BrandOrchid))
    val deep = Brush.linearGradient(listOf(BrandPink, BrandOrchid, BrandViolet))
    val night = Brush.linearGradient(listOf(BrandViolet, BrandNight))
    val text = Brush.horizontalGradient(listOf(Color(0xFFFF6FB5), Color(0xFFD27BFF)))
    /** Pink → violet → cyan, used for neon borders and highlight text. */
    val neon = Brush.linearGradient(listOf(NeonPink, NeonViolet, NeonCyan))
    /** Raised glass surface on the night canvas. */
    val glass = Brush.verticalGradient(listOf(Color(0xFF2A1B45).copy(alpha = 0.78f), Color(0xFF170D2A).copy(alpha = 0.72f)))
}
