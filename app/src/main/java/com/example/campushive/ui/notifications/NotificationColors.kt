package com.example.campushive.ui.notifications

import android.graphics.Color
import com.example.campushive.ui.theme.Charcoal

/**
 * Notification tints must be platform `Int` ARGB values — the Android
 * notification APIs do not accept Compose [androidx.compose.ui.graphics.Color].
 *
 * Kept separate from `ui/theme/Color.kt` so that file stays pure Compose tokens,
 * while still deriving from the same single source of truth (Charcoal).
 */
object NotificationColors {
    /** Accent applied to the status-bar icon and app-name row. */
    val BrandAccentArgb: Int = Color.parseColor("#14171E")

    /** Soft surface used for the runtime-drawn avatar monogram. */
    val AvatarBackgroundArgb: Int = Color.parseColor("#FFF1C9") // HoneyTint

    /** Avatar ring — matches [com.example.campushive.ui.theme.AvatarOutline]. */
    val AvatarOutlineArgb: Int = Color.parseColor("#14171E")

    /** Monogram text colour. */
    val AvatarTextArgb: Int = Color.parseColor("#14171E")
}

/** Convenience alias so the helper reads cleanly. */
val BrandAccentArgb: Int = NotificationColors.BrandAccentArgb
