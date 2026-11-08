package com.example.campushive.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Corner radius scale. Replaces inline `RoundedCornerShape(n.dp)` literals.
 *
 * The app previously carried 12 arbitrary radii (6,8,10,12,13,14,16,18,20,22,24,26).
 * Five steps are enough; pick the nearest semantic role, never a raw number.
 */
object Radius {
    /** Chips, tags, badges — nearly full roundness. */
    val Pill = RoundedCornerShape(50)

    /** Large cards, sheets, modals — the default container. */
    val Card = RoundedCornerShape(20.dp)

    /** Items inside a card, list rows, image holders. */
    val Inner = RoundedCornerShape(16.dp)

    /** Icon boxes, small tiles, input affixes. */
    val Tile = RoundedCornerShape(13.dp)

    /** Tiny labels and inline tags. */
    val Tag = RoundedCornerShape(6.dp)
}

/**
 * Spacing scale (4dp base). Replaces inline padding literals.
 */
object Spacing {
    /** 4dp — hairline gaps between tightly coupled elements. */
    val xs = 4.dp

    /** 8dp — inside a control. */
    val sm = 8.dp

    /** 12dp — between related items. */
    val md = 12.dp

    /** 16dp — the standard screen gutter. All screens use this. */
    val gutter = 16.dp

    /** 20dp — between sections. */
    val lg = 20.dp

    /** 24dp — large section breaks. */
    val xl = 24.dp

    /** 28dp — top of a screen below the status bar. */
    val topInset = 28.dp

    /** 96dp — bottom scroll padding so content clears the floating bottom bar. */
    val bottomBarClearance = 96.dp
}
