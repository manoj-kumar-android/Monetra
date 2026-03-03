package com.monetra.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing tokens — always use these rather than raw dp literals.
 *
 * All values sit on a 4dp baseline grid. Reference the semantic aliases
 * (screenHorizontal, cardPadding, etc.) in screen files so that global
 * layout adjustments can be made from a single location.
 */
object Spacing {

    // ── Base scale ────────────────────────────────────────────────────────

    /** 4dp  — icon/badge micro-gaps, tight row gaps */
    val xs   = 4.dp

    /** 8dp  — between related items (icon + label, tag row gap) */
    val sm   = 8.dp

    /** 12dp — list row internal vertical padding, compact card padding */
    val md   = 12.dp

    /** 16dp — standard horizontal screen margin, list item padding */
    val lg   = 16.dp

    /** 20dp — card internal padding, section header bottom gap */
    val xl   = 20.dp

    /** 24dp — between major layout sections */
    val xxl  = 24.dp

    /** 32dp — top/bottom breathing room on scrollable pages */
    val xxxl = 32.dp

    // ── Semantic aliases ──────────────────────────────────────────────────

    /** Left and right margin from the screen edge (= lg = 16dp) */
    val screenHorizontal = lg

    /** Top and bottom padding on a scrollable page (= xl = 20dp) */
    val screenVertical   = xl

    /** Internal padding inside a card (= lg = 16dp) */
    val cardPadding      = lg

    /** Horizontal padding inside a list row (= lg = 16dp) */
    val listItemHorizontal = lg

    /** Vertical padding inside a list row (= md = 12dp) */
    val listItemVertical   = md

    /** Vertical gap between list section groups (= xxl = 24dp) */
    val sectionGap         = xxl

    /** Gap between a section header label and its first row (= xs = 4dp) */
    val sectionHeaderBottom = xs

    /** Spacing between stacked cards in a feed (= sm = 8dp) */
    val cardStackGap        = sm

    /** Minimum touch target side length per iOS HIG (44dp) */
    val minTouchTarget      = 44.dp
}
