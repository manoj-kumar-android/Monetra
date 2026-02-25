package com.monetra.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Elevation tokens — keep shadows subtle for a Cupertino-like feel.
 *
 * iOS achieves depth through colour contrast between layered surfaces,
 * not through heavy drop shadows. Follow these rules:
 *
 *  • Prefer surface colour layering (Background → Surface → SurfaceVariant)
 *    over elevation to express hierarchy.
 *  • Cards on a grouped background use [card] (1dp) — barely perceptible.
 *  • Never use elevation above [dialog] (3dp) for persistent UI elements.
 *  • Top app bar and navigation bar are always flat (0dp); use a
 *    [OutlineVariant]-coloured divider when a separator is needed instead.
 */
object Elevation {

    /** 0dp — page root, flat backgrounds, navigation bar, top app bar */
    val none         = 0.dp

    /** 1dp — standard cards sitting on a grouped background */
    val card         = 1.dp

    /** 2dp — cards in a pressed/active state, or cards needing emphasis */
    val raisedCard   = 2.dp

    /** 3dp — dialogs, popovers, context menus */
    val dialog       = 3.dp

    /** 4dp — bottom sheets, side navigation drawers */
    val bottomSheet  = 4.dp

    // ── Semantic zero-elevation aliases (named for intent, not value) ─────

    /** Top app bar — always flat; no shadow on scroll (iOS style) */
    val topAppBar      = none

    /** Bottom navigation / tab bar — flat; use a divider line instead */
    val navigationBar  = none

    /** Search bar — flat when embedded in a surface */
    val searchBar      = none
}
