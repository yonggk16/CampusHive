package com.example.campushive.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==============================================================================
// THEME MODE STATE — Updated by CampusHiveTheme (follows system or user toggle)
// ==============================================================================
enum class ThemePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

object CampusThemeState {
    private const val PREFS_NAME = "campushive_prefs"
    private const val KEY_THEME = "theme_preference"

    var isDark by mutableStateOf(false)
    var preference by mutableStateOf(ThemePreference.SYSTEM)
    private var loaded = false

    fun load(context: Context) {
        if (loaded) return
        loaded = true
        val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, ThemePreference.SYSTEM.name)
        preference = ThemePreference.entries.firstOrNull { it.name == saved } ?: ThemePreference.SYSTEM
    }

    fun setPreference(context: Context, newPreference: ThemePreference) {
        preference = newPreference
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, newPreference.name)
            .apply()
    }
}

// ==============================================================================
// UNIFIED 60-30-10 DESIGN SYSTEM (Monochrome High-Contrast + Restrained Honey)
// Light: Warm Cream (#FAF8F2) + White Cards + Charcoal (#14171E) + Soft Honey (#FFF1C9)
// Dark:  Pure OLED (#000000)  + Carbon Cards + Crisp White (#FAFAFA) + Subtle Honey (#F5C249)
// ==============================================================================
val AppMainBackground: Color
    get() = if (CampusThemeState.isDark) Color(0xFF000000) else Color(0xFFFAF8F2)

// Core Design Tokens
val Ink: Color
    get() = if (CampusThemeState.isDark) Color(0xFFFAFAFA) else Color(0xFF14171E)
val HoneyTint: Color
    get() = if (CampusThemeState.isDark) Color(0xFF202024) else Color(0xFFFFF1C9)   // Subtle badge/tile fill
val BrandHoney: Color
    get() = if (CampusThemeState.isDark) Color(0xFFF5C249) else Color(0xFFD4A017)   // Restrained accent for stars, glyphs, unread dots
val Accent: Color
    get() = Ink
val SurfaceWhite: Color
    get() = if (CampusThemeState.isDark) Color(0xFF141417) else Color(0xFFFFFFFF)
val Hairline: Color
    get() = if (CampusThemeState.isDark) Color(0xFF27272C) else Color(0xFFE2DFD4)
val Muted: Color
    get() = if (CampusThemeState.isDark) Color(0xFFA1A1AA) else Color(0xFF6B6860)

// Dynamic Color Aliases (Consistent Monochrome Primary Actions in Both Modes)
val Honey: Color
    get() = Ink                                             // Primary buttons, active chips, FAB (Charcoal in Light, Crisp White in Dark)
val OnHoneyAccent: Color
    get() = if (CampusThemeState.isDark) Color(0xFF09090B) else Color(0xFFFFFFFF)   // High-contrast ink on primary actions
val HeroBannerBg: Color
    get() = if (CampusThemeState.isDark) Color(0xFF141417) else Color(0xFF14171E)
val HeroBannerFg: Color
    get() = Color(0xFFFAFAFA)
val Charcoal: Color
    get() = Ink                                             // Primary text, titles, and buttons
val Cream: Color
    get() = AppMainBackground
val CreamTint: Color
    get() = if (CampusThemeState.isDark) Color(0xFF202024) else Color(0xFFEFECE2)
val CardWhite: Color
    get() = SurfaceWhite                                    // Cards, boxes, pills, inputs
val MutedText: Color
    get() = Muted
val VerifiedGreen: Color
    get() = Ink
val OnlineGreen = Color(0xFF22C55E)
val ReadReceiptBlue = Color(0xFF34B7F1)
val OfflineGrey: Color
    get() = if (CampusThemeState.isDark) Color(0xFF71717A) else Color(0xFFB0ADA6)
val VerifiedGreenBg: Color
    get() = HoneyTint
val AlertRed: Color
    get() = if (CampusThemeState.isDark) Color(0xFFF87171) else Color(0xFFD32F2F)

// ==============================================================================
// SEMANTIC STATUS TOKENS
// ==============================================================================

// Warning / pending
val WarningBg: Color
    get() = if (CampusThemeState.isDark) Color(0xFF262012) else Color(0xFFFFF3C4)
val WarningBgSoft: Color
    get() = if (CampusThemeState.isDark) Color(0xFF1E1A0F) else Color(0xFFFFF7DB)
val WarningBorder: Color
    get() = if (CampusThemeState.isDark) Color(0xFF63501E) else Color(0xFFE5C158)
val WarningBorderSoft: Color
    get() = if (CampusThemeState.isDark) Color(0xFF4A3C17) else Color(0xFFFFE8A3)
val WarningFg: Color
    get() = if (CampusThemeState.isDark) Color(0xFFFDE68A) else Color(0xFF7A5800)
val WarningFgStrong: Color
    get() = if (CampusThemeState.isDark) Color(0xFFF5C249) else Color(0xFF8C6B10)
val WarningFgVivid: Color
    get() = if (CampusThemeState.isDark) Color(0xFFF5C249) else Color(0xFFD97706)

// Success / resolved
val SuccessBg: Color
    get() = if (CampusThemeState.isDark) Color(0xFF0E261C) else Color(0xFFEBFBF3)
val SuccessBgSoft: Color
    get() = if (CampusThemeState.isDark) Color(0xFF0B1F17) else Color(0xFFE3FCEF)
val SuccessBorder: Color
    get() = if (CampusThemeState.isDark) Color(0xFF1F5C42) else Color(0xFFA6E9C5)
val SuccessFg: Color
    get() = if (CampusThemeState.isDark) Color(0xFF6EE7B7) else Color(0xFF006644)
val StarGold = Color(0xFFF5C249)
val StarGoldDeep = Color(0xFFD4A017)

// Danger / destructive / flagged
val DangerBg: Color
    get() = if (CampusThemeState.isDark) Color(0xFF2E1418) else Color(0xFFFFEBEE)
val DangerBgSoft: Color
    get() = if (CampusThemeState.isDark) Color(0xFF251114) else Color(0xFFFFF0F0)
val DangerBorder: Color
    get() = if (CampusThemeState.isDark) Color(0xFF7A2A32) else Color(0xFFFFCDD2)
val DangerBorderSoft: Color
    get() = if (CampusThemeState.isDark) Color(0xFF5E2127) else Color(0xFFEF9A9A)
val DangerFg: Color
    get() = if (CampusThemeState.isDark) Color(0xFFFCA5A5) else Color(0xFFC62828)
val DangerFgStrong: Color
    get() = if (CampusThemeState.isDark) Color(0xFFF87171) else Color(0xFFD32F2F)
val DangerButtonBg: Color
    get() = if (CampusThemeState.isDark) Color(0xFFDC2626) else Color(0xFFD32F2F)
val OnDangerButton: Color = Color(0xFFFFFFFF)

// Neutral / closed / info
val NeutralBg: Color
    get() = if (CampusThemeState.isDark) Color(0xFF202024) else Color(0xFFF1F2F4)

// -----------------------------------------------------------------------------
// ON-DARK tints — content sitting on a Charcoal surface (e.g. own chat bubbles).
// -----------------------------------------------------------------------------
val OnDarkSurface: Color
    get() = if (CampusThemeState.isDark) Color(0xFFE4E4E7) else Color(0xFF2E2E2E)
val OnDarkStrong: Color
    get() = if (CampusThemeState.isDark) Color(0xFFD4D4D8) else Color(0xFF333333)
val OnDarkBorder: Color
    get() = if (CampusThemeState.isDark) Color(0xFFA1A1AA) else Color(0xFF444444)
val OnDarkMuted: Color
    get() = if (CampusThemeState.isDark) Color(0xFF52525B) else Color(0xFFBDBDBD)

// -----------------------------------------------------------------------------
// AVATAR / ICON-TILE OUTLINE
// Crisp neutral zinc outline in Dark Mode, Charcoal outline in Light Mode
// -----------------------------------------------------------------------------
val AvatarOutline: Color
    get() = if (CampusThemeState.isDark) Color(0xFF3A3A42) else Color(0xFF14171E)
val AvatarOutlineWidth = 1.5f
val TileOutline: Color
    get() = if (CampusThemeState.isDark) Color(0xFF3A3A42) else Color(0xFF14171E)

// Dynamic Gradient Tokens (evaluates AppMainBackground automatically)
val PageGradient: Brush get() = Brush.verticalGradient(colors = listOf(AppMainBackground, AppMainBackground))
val AppBackgroundGradient: Brush get() = PageGradient
val HoneyBottomGradient: Brush get() = PageGradient
