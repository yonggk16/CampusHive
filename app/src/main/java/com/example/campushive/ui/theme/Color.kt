package com.example.campushive.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==============================================================================
// DYNAMIC APP MAIN BACKGROUND TOKEN — Change this SINGLE color to update all screens!
// ==============================================================================
val AppMainBackground = Color(0xFFFAF8F2)

// Core Design Tokens
val Ink = Color(0xFF14171E)
val HoneyTint = Color(0xFFFFF1C9)   // Warm Honey Tint for listing category icon boxes
val Accent = Color(0xFF14171E)
val SurfaceWhite = Color(0xFFFFFFFF)
val Hairline = Color(0xFFE2DFD4)
val Muted = Color(0xFF6B6860)

// Dynamic Color Aliases
val Honey = Color(0xFF14171E)       // Charcoal for buttons & primary actions
val Charcoal = Color(0xFF14171E)    // Charcoal text, titles, and buttons
val Cream = AppMainBackground       // Dynamically bound to AppMainBackground
val CreamTint = Color(0xFFEFECE2)
val CardWhite = Color(0xFFFFFFFF)   // Cardboard White for cards, boxes, pills, inputs
val MutedText = Color(0xFF6B6860)   // Neutral muted text
val VerifiedGreen = Color(0xFF14171E) // High contrast Charcoal verified badge
val OnlineGreen = Color(0xFF22C55E)  // Green indicator when online
val OfflineGrey = Color(0xFFB0ADA6)  // Grey indicator when offline
val VerifiedGreenBg = Color(0xFFFFF1C9)
val AlertRed = Color(0xFFD32F2F)

// Dynamic Gradient Tokens (evaluates AppMainBackground automatically)
val PageGradient: Brush get() = Brush.verticalGradient(colors = listOf(AppMainBackground, AppMainBackground))
val AppBackgroundGradient: Brush get() = PageGradient
val HoneyBottomGradient: Brush get() = PageGradient
