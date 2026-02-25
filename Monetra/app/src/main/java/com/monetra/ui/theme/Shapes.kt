package com.monetra.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Cupertino-inspired shape scale.
 *
 * Material 3 components automatically resolve their shape from these slots:
 *
 *   extraSmall (8dp)  → assist/filter/input chips, small FAB, snackbar
 *   small      (10dp) → FilledTextField container, chip (M3 default override)
 *   medium     (14dp) → Card, menus, navigation drawer items
 *   large      (18dp) → FAB, ExtendedFAB, date input field
 *   extraLarge (24dp) → BottomSheet top corners, AlertDialog, DatePicker
 *
 * Shapes registered here flow into MaterialTheme.shapes and are picked up
 * automatically by every M3 component — no per-call overrides needed for
 * the standard cases. Per-component exceptions are in ComponentDefaults.kt.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
