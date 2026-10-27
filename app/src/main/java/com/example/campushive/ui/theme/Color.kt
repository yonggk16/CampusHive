package com.example.campushive.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Design System Tokens (design.md)
val Ink = Color(0xFF14171E)
val HoneyTint = Color(0xFFFFF1C9)
val Accent = Color(0xFFE9A800)
val SurfaceWhite = Color(0xFFFFFFFF)
val Hairline = Color(0xFFE2DFD4)
val Muted = Color(0xFF6B6860)

// Aliases
val Honey = Color(0xFFF5B726)
val Charcoal = Color(0xFF14171E)
val Cream = Color(0xFFFAF7EC)
val CreamTint = Color(0xFFEFECE2)
val CardWhite = Color(0xFFFFFFFF)
val MutedText = Color(0xFF6B6860)
val VerifiedGreen = Color(0xFF059669)
val VerifiedGreenBg = Color(0xFFEBFBF3)
val AlertRed = Color(0xFFEF4444)

val PageGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFF9EA),
        Color(0xFFFFF1C9)
    )
)
val AppBackgroundGradient = PageGradient
