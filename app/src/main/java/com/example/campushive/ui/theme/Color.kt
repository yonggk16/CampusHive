package com.example.campushive.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Design Tokens — Charcoal Buttons, Card White Boxes, Honey Tint Listing Icons
val Ink = Color(0xFF14171E)
val HoneyTint = Color(0xFFFFF1C9) // Warm Honey Tint for listing category icon boxes
val Accent = Color(0xFF14171E)
val SurfaceWhite = Color(0xFFFFFFFF)
val Hairline = Color(0xFFE2DFD4)
val Muted = Color(0xFF6B6860)

// Aliases
val Honey = Color(0xFF14171E)       // Charcoal for buttons & primary actions
val Charcoal = Color(0xFF14171E)    // Charcoal text, titles, and buttons
val Cream = Color(0xFFFAF7EC)       // Soft Cream page background
val CreamTint = Color(0xFFEFECE2)
val CardWhite = Color(0xFFFFFFFF)   // Cardboard White for cards, boxes, pills, inputs
val MutedText = Color(0xFF6B6860)   // Neutral muted text
val VerifiedGreen = Color(0xFF14171E) // High contrast Charcoal verified badge
val OnlineGreen = Color(0xFF22C55E)  // Green indicator when online
val OfflineGrey = Color(0xFFB0ADA6)  // Grey indicator when offline
val VerifiedGreenBg = Color(0xFFFFF1C9)
val AlertRed = Color(0xFFD32F2F)

val PageGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFF9EA),
        Color(0xFFFFF1C9)
    )
)
val AppBackgroundGradient = PageGradient
val HoneyBottomGradient = PageGradient
