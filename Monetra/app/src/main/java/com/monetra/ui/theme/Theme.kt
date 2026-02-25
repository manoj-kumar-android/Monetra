package com.monetra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Light scheme ──────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    // Primary — iOS blue (actions, links, active states)
    primary             = PrimaryLight,
    onPrimary           = Color.White,
    primaryContainer    = PrimaryContainerLight,
    onPrimaryContainer  = OnPrimaryContainerLight,

    // Secondary — iOS green (income, positive amounts)
    secondary             = SecondaryLight,
    onSecondary           = Color.White,
    secondaryContainer    = SecondaryContainerLight,
    onSecondaryContainer  = OnBackgroundLight,

    // Tertiary — iOS orange (warnings, neutral accents)
    tertiary             = TertiaryLight,
    onTertiary           = Color.White,
    tertiaryContainer    = TertiaryContainerLight,
    onTertiaryContainer  = OnBackgroundLight,

    // Error — iOS red (expenses, destructive, negative)
    error             = ErrorLight,
    onError           = Color.White,
    errorContainer    = ErrorContainerLight,
    onErrorContainer  = OnErrorContainerLight,

    // Backgrounds — mirrors iOS grouped background hierarchy
    background    = BackgroundLight,
    onBackground  = OnBackgroundLight,

    // Surface — white cards / sheets sitting on the grouped background
    surface             = SurfaceLight,
    onSurface           = OnSurfaceLight,
    surfaceVariant      = SurfaceVariantLight,
    onSurfaceVariant    = OnSurfaceVariantLight,

    // Borders & separators
    outline        = OutlineLight,
    outlineVariant = OutlineVariantLight,

    // Modal scrim — 32% black (lighter than M3 default for a softer feel)
    scrim = Color(0x52000000),
)

// ── Dark scheme ───────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary             = PrimaryDark,
    onPrimary           = Color.White,
    primaryContainer    = PrimaryContainerDark,
    onPrimaryContainer  = OnPrimaryContainerDark,

    secondary             = SecondaryDark,
    onSecondary           = Color.White,
    secondaryContainer    = SecondaryContainerDark,
    onSecondaryContainer  = OnBackgroundDark,

    tertiary             = TertiaryDark,
    onTertiary           = Color.White,
    tertiaryContainer    = TertiaryContainerDark,
    onTertiaryContainer  = OnBackgroundDark,

    error             = ErrorDark,
    onError           = Color.White,
    errorContainer    = ErrorContainerDark,
    onErrorContainer  = OnErrorContainerDark,

    background    = BackgroundDark,
    onBackground  = OnBackgroundDark,

    surface             = SurfaceDark,
    onSurface           = OnSurfaceDark,
    surfaceVariant      = SurfaceVariantDark,
    onSurfaceVariant    = OnSurfaceVariantDark,

    outline        = OutlineDark,
    outlineVariant = OutlineVariantDark,

    // Modal scrim — 50% black in dark mode
    scrim = Color(0x80000000),
)

// ── Theme entry point ─────────────────────────────────────────────────────

/**
 * Root theme wrapper for the Monetra app.
 *
 * Dynamic colour is intentionally disabled — we rely on our hand-crafted
 * Cupertino-inspired palette for a consistent, brand-correct appearance
 * regardless of the user's wallpaper.
 */
@Composable
fun MonetraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content,
    )
}
