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

// ==============================================================================
// SEMANTIC STATUS TOKENS
// Every status pill / banner in the app resolves through these. Do not inline
// `Color(0xFF...)` for a state colour — add or reuse a token here instead.
// Each status has a paired surface (bg) + foreground (fg) so contrast is fixed.
// ==============================================================================

// Warning / pending (amber family — belongs in the warm palette)
val WarningBg = Color(0xFFFFF3C4)
val WarningBgSoft = Color(0xFFFFF7DB)
val WarningBorder = Color(0xFFE5C158)
val WarningBorderSoft = Color(0xFFFFE8A3)
val WarningFg = Color(0xFF7A5800)   // accessible on WarningBg
val WarningFgStrong = Color(0xFF8C6B10) // accents / count numbers on warning surfaces
val WarningFgVivid = Color(0xFFD97706)  // actionable "mark read" style links

// Success / resolved (green family)
val SuccessBg = Color(0xFFEBFBF3)
val SuccessBgSoft = Color(0xFFE3FCEF)
val SuccessBorder = Color(0xFFA6E9C5)
val SuccessFg = Color(0xFF006644)
val StarGold = Color(0xFFFFB800)      // rating stars
val StarGoldDeep = Color(0xFFD4A017)  // review header star

// Danger / destructive / flagged (red family)
val DangerBg = Color(0xFFFFEBEE)
val DangerBgSoft = Color(0xFFFFF0F0)
val DangerBorder = Color(0xFFFFCDD2)
val DangerBorderSoft = Color(0xFFEF9A9A)
val DangerFg = Color(0xFFC62828)
val DangerFgStrong = Color(0xFFD32F2F) // primary destructive action

// Neutral / closed / info
val NeutralBg = Color(0xFFF1F2F4)

// -----------------------------------------------------------------------------
// ON-DARK tints — content sitting on a Charcoal surface (e.g. own chat bubbles).
// Never inline these; they must step consistently to stay legible on Ink.
// -----------------------------------------------------------------------------
val OnDarkSurface = Color(0xFF2E2E2E) // nested card on a dark bubble
val OnDarkStrong = Color(0xFF333333)  // disabled / de-emphasised dark fill
val OnDarkBorder = Color(0xFF444444)  // hairline on dark
val OnDarkMuted = Color(0xFFBDBDBD)   // secondary text on dark

// -----------------------------------------------------------------------------
// AVATAR / ICON-TILE OUTLINE
// One outline for every identity mark (avatars, category icon tiles, glyph boxes)
// so initials circles and category tiles read as the same family instead of a
// mix of Charcoal 1dp / Hairline 1dp / no border at all.
// -----------------------------------------------------------------------------
val AvatarOutline = Color(0xFF14171E) // matches Charcoal; the "black outline"
val AvatarOutlineWidth = 1.5f          // dp, applied by CampusAvatar
val TileOutline = Color(0xFF14171E)

// Dynamic Gradient Tokens (evaluates AppMainBackground automatically)
val PageGradient: Brush get() = Brush.verticalGradient(colors = listOf(AppMainBackground, AppMainBackground))
val AppBackgroundGradient: Brush get() = PageGradient
val HoneyBottomGradient: Brush get() = PageGradient
