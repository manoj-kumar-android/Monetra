package com.monetra.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand Palette ─────────────────────────────────────────────────────────

// Blue (primary actions, links)
val PrimaryLight          = Color(0xFF007AFF)
val PrimaryDark           = Color(0xFF0A84FF)
val PrimaryContainerLight = Color(0xFFE5F1FF)
val PrimaryContainerDark  = Color(0xFF003D8F)

// Green (income / positive / success)
val SecondaryLight          = Color(0xFF34C759)
val SecondaryDark           = Color(0xFF30D158)
val SecondaryContainerLight = Color(0xFFE8F8EC)
val SecondaryContainerDark  = Color(0xFF0D4C1F)

// Orange (warnings / neutral accent)
val TertiaryLight          = Color(0xFFFF9500)
val TertiaryDark           = Color(0xFFFF9F0A)
val TertiaryContainerLight = Color(0xFFFFF3E0)
val TertiaryContainerDark  = Color(0xFF4C3000)

// Red (expenses / errors / destructive)
val ErrorLight          = Color(0xFFFF3B30)
val ErrorDark           = Color(0xFFFF453A)
val ErrorContainerLight = Color(0xFFFFE5E3)
val ErrorContainerDark  = Color(0xFF5C0A07)

// ─── Surfaces & Backgrounds ────────────────────────────────────────────────

// Light mode — mirrors iOS grouped background hierarchy
val BackgroundLight        = Color(0xFFF2F2F7)  // systemGroupedBackground
val SurfaceLight           = Color(0xFFFFFFFF)  // secondarySystemGroupedBackground (cards)
val SurfaceVariantLight    = Color(0xFFEFEFF4)  // tertiarySystemGroupedBackground

// Dark mode — mirrors iOS dark grouped background hierarchy
val BackgroundDark         = Color(0xFF000000)  // systemGroupedBackground (dark)
val SurfaceDark            = Color(0xFF1C1C1E)  // secondarySystemGroupedBackground (dark)
val SurfaceVariantDark     = Color(0xFF2C2C2E)  // tertiarySystemGroupedBackground (dark)

// ─── Text / Label Colors ───────────────────────────────────────────────────

// Light mode
val OnBackgroundLight        = Color(0xFF1C1C1E)  // iOS label
val OnSurfaceLight           = Color(0xFF1C1C1E)
val OnSurfaceVariantLight    = Color(0xFF8E8E93)  // iOS secondaryLabel
val OnPrimaryContainerLight  = Color(0xFF001A3D)
val OnErrorContainerLight    = Color(0xFF5C0A07)

// Dark mode
val OnBackgroundDark         = Color(0xFFFFFFFF)  // iOS label (dark)
val OnSurfaceDark            = Color(0xFFFFFFFF)
val OnSurfaceVariantDark     = Color(0xFF8E8E93)  // same in both modes
val OnPrimaryContainerDark   = Color(0xFFD6E8FF)
val OnErrorContainerDark     = Color(0xFFFFB4AE)

// ─── Borders & Separators ──────────────────────────────────────────────────

val OutlineLight        = Color(0xFFC6C6C8)  // iOS separator
val OutlineVariantLight = Color(0xFFE5E5EA)  // iOS opaqueSeparator

val OutlineDark         = Color(0xFF38383A)  // iOS separator (dark)
val OutlineVariantDark  = Color(0xFF48484A)  // iOS opaqueSeparator (dark)

// ─── Semantic Finance Colors ───────────────────────────────────────────────
// These are used directly in composables for income/expense indicators;
// do NOT assign them to MaterialTheme color roles.

val SemanticIncome        = Color(0xFF34C759)  // positive amount
val SemanticIncomeDark    = Color(0xFF30D158)
val SemanticIncomeSubtle  = Color(0xFFE8F8EC)  // background tint for income rows
val SemanticIncomeSubtleDark = Color(0xFF0D4C1F)

val SemanticExpense       = Color(0xFFFF3B30)  // negative amount
val SemanticExpenseDark   = Color(0xFFFF453A)
val SemanticExpenseSubtle = Color(0xFFFFE5E3)  // background tint for expense rows
val SemanticExpenseSubtleDark = Color(0xFF5C0A07)

val SemanticNeutral       = Color(0xFF8E8E93)  // uncategorized / gray amounts
