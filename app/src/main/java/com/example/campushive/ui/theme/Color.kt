package com.example.campushive.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Minimal Theme Palette: Honey Tint Gradient Background + Monotone Charcoal & White UI
val Honey = Color(0xFF14171E)           // Charcoal Black replacing Yellow for buttons & primary actions
val HoneyTint = Color(0xFFFFF1C9)       // Soft Warm Honey Tint for service icons & background gradient
val Charcoal = Color(0xFF14171E)        // Deep Charcoal Black for text, headers & primary buttons
val Cream = Color(0xFFFAF7EC)           // Warm Cream App Background
val CreamTint = Color(0xFFEFECE2)       // Soft Container & Pill Background
val CardWhite = Color(0xFFFFFFFF)       // Pure Crisp White for all cards & surfaces
val Hairline = Color(0xFFE2DFD4)        // Subtle Monotone Card Borders
val MutedText = Color(0xFF6B6860)       // High-Legibility Neutral Subtitle Grey

// Verified & Status Indicators
val VerifiedGreen = Color(0xFF14171E)   // Monotone Charcoal for verified badges
val VerifiedGreenBg = Color(0xFFEFECE2) // Soft Monotone Background
val AlertRed = Color(0xFF14171E)

// App Gradient Background: White Top -> Warm Honey Tint Bottom (Matches Prompt Screenshot)
val AppBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF), // Crisp White Top
        Color(0xFFFFF9EA), // Soft Honey Cream Center
        Color(0xFFFFF1C9)  // Warm Honey Tint Bottom
    )
)
