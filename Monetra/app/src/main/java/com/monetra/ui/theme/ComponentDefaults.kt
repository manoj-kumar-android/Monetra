package com.monetra.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Per-component styling rules for the Monetra design system.
 *
 * These values are passed explicitly to M3 component parameters when the
 * shape/padding deviates from what [AppShapes] provides automatically.
 *
 * ── Colour policy ─────────────────────────────────────────────────────────
 * Never hardcode colours here. Always derive from MaterialTheme.colorScheme
 * in screen files so dark mode works automatically.
 *
 * ── Variant guidance ──────────────────────────────────────────────────────
 *  • Filled button      → primary CTA (one per screen max)
 *  • FilledTonal button → secondary/confirmatory actions
 *  • Text button        → low-emphasis or inline actions
 *  • OutlinedButton     → not preferred; use FilledTonal instead
 *  • ElevatedCard       → content cards on grouped background
 *  • OutlinedCard       → prefer only on a Surface (white) background
 *  • FilledTextField    → all standard form inputs
 *  • OutlinedTextField  → only for search bars or inline edits
 */
object ComponentDefaults {

    // ── Button ─────────────────────────────────────────────────────────────

    /** Rounded-rect button shape — slightly tighter than pill to feel iOS-native */
    val buttonShape           = RoundedCornerShape(14.dp)
    val buttonContentPadding  = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

    /** Same shape for tonal and outlined variants */
    val tonalButtonShape      = RoundedCornerShape(14.dp)

    // ── Icon Button ────────────────────────────────────────────────────────

    /** Circular icon button — standard iOS control style */
    val iconButtonShape       = CircleShape

    /** 44×44dp — iOS HIG minimum touch target */
    val iconButtonMinSize     = Spacing.minTouchTarget

    // ── Card ───────────────────────────────────────────────────────────────

    /**
     * Standard content card.
     * Use ElevatedCard + [cardElevation] on [BackgroundLight/Dark].
     * Use flat Card + [OutlineVariant] border on a [SurfaceLight/Dark] parent.
     */
    val cardShape     = RoundedCornerShape(18.dp)
    val cardElevation = Elevation.card
    val cardPadding   = PaddingValues(Spacing.cardPadding)

    // ── Text Field ─────────────────────────────────────────────────────────

    /**
     * Use FilledTextField everywhere.
     * Set containerColor = MaterialTheme.colorScheme.surfaceVariant
     * and indicatorColor = Color.Transparent (both focused and unfocused)
     * for a clean iOS-style inset input.
     */
    val textFieldShape = RoundedCornerShape(12.dp)

    // ── Bottom Sheet ───────────────────────────────────────────────────────

    /** Only top corners are rounded; bottom edges stay flush with screen edge. */
    val bottomSheetShape     = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val bottomSheetElevation = Elevation.bottomSheet

    // ── Dialog ─────────────────────────────────────────────────────────────

    val dialogShape = RoundedCornerShape(20.dp)

    // ── FAB ────────────────────────────────────────────────────────────────

    /** Soft rounded-rect FAB — avoid the default circular FAB on finance UIs */
    val fabShape         = RoundedCornerShape(18.dp)
    val extendedFabShape = RoundedCornerShape(18.dp)
    val fabElevation     = Elevation.raisedCard

    // ── Chip ───────────────────────────────────────────────────────────────

    /** Category chips, filter chips, amount tags */
    val chipShape = RoundedCornerShape(10.dp)

    // ── Navigation Bar ─────────────────────────────────────────────────────

    /**
     * Tab bar is flat — no shadow, no tonal surface shift.
     * Use a single-pixel [OutlineVariant]-coloured Divider above it.
     */
    val navigationBarElevation = Elevation.none
    val navigationBarHeight    = 56.dp

    // ── List / Row ─────────────────────────────────────────────────────────

    /** Full-width transaction rows; shape is square (no rounding on individual rows) */
    val listRowShape             = RoundedCornerShape(0.dp)

    /** Container that wraps a group of rows (iOS grouped table section) */
    val listGroupContainerShape  = RoundedCornerShape(14.dp)

    val listRowHorizontalPadding = Spacing.listItemHorizontal
    val listRowVerticalPadding   = Spacing.listItemVertical

    // ── Search Bar ─────────────────────────────────────────────────────────

    val searchBarShape = RoundedCornerShape(12.dp)

    // ── Snackbar ───────────────────────────────────────────────────────────

    val snackbarShape = RoundedCornerShape(14.dp)

    // ── Top App Bar ────────────────────────────────────────────────────────

    /**
     * Top app bar is always flat with no scroll-driven colour change.
     * Set scrolledContainerColor == containerColor to suppress M3's default
     * tonal elevation effect on scroll.
     */
    val topAppBarElevation = Elevation.topAppBar
}
