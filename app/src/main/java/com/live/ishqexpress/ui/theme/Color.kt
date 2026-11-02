package com.live.ishqexpress.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand — sampled from the ishqexpress logo (hot pink → orchid → deep violet)
val BrandPink = Color(0xFFFF2E8B)
val BrandPinkDark = Color(0xFFE0167A)
val BrandOrchid = Color(0xFFC236D9)
val BrandViolet = Color(0xFF7A2BE8)
val BrandNight = Color(0xFF2A0B4A)

// Soft brand tints for surfaces on a white canvas
val PinkTint = Color(0xFFFFEEF5)
val PinkTintStrong = Color(0xFFFFD9E9)
val VioletTint = Color(0xFFF3ECFF)

// Neutrals
val Canvas = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFF7F5FA)
val Hairline = Color(0xFFEEEAF3)
val Ink = Color(0xFF1A1029)
val InkSecondary = Color(0xFF686179)
val InkTertiary = Color(0xFFA29BB2)
val DisabledFill = Color(0xFFECE8F1)

// Accents
val LiveRed = Color(0xFFFF2D55)
val Gold = Color(0xFFFFB020)
val Success = Color(0xFF12B76A)

object BrandBrush {
    val primary = Brush.horizontalGradient(listOf(BrandPink, BrandOrchid))
    val deep = Brush.linearGradient(listOf(BrandPink, BrandOrchid, BrandViolet))
    val night = Brush.linearGradient(listOf(BrandViolet, BrandNight))
    val text = Brush.horizontalGradient(listOf(BrandPink, BrandOrchid))
}
