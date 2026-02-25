package com.monetra.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Cupertino-inspired typography scale mapped to Material 3 type roles.
 *
 * Sizes follow the iOS HIG Dynamic Type hierarchy (default/Large setting).
 * Letter spacing approximates SF Pro's optical tracking in em units.
 *
 * Font: System sans-serif (Roboto on AOSP). To get a closer iOS feel,
 * add Inter to res/font/ and replace FontFamily.Default with your
 * Inter FontFamily definition — no other changes needed.
 */
val AppTypography = Typography(

    // ── Display ─── hero numbers, main balance ────────────────────────────

    // iOS "Large Title" — 34pt — main account balance, screen hero amounts
    displayLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 34.sp,
        lineHeight    = 41.sp,
        letterSpacing = (-0.012f).em,
    ),

    // iOS "Title 1" — 28pt — section-level hero amounts
    displayMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = 0f.em,
    ),

    // iOS "Title 2" — 22pt — sub-section amounts, chart axis labels (large)
    displaySmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0f.em,
    ),

    // ── Headline ─── screen titles, card headers ──────────────────────────

    // iOS "Title 3" — 20pt — card titles, sheet headers
    headlineLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 25.sp,
        letterSpacing = 0f.em,
    ),

    // iOS "Headline" — 17pt SemiBold — list section headers, emphasized labels
    headlineMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 17.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.024f).em,
    ),

    // supporting heading — 15pt SemiBold — grouped list section titles
    headlineSmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = (-0.012f).em,
    ),

    // ── Title ─── navigation bar, list primary text ───────────────────────

    // iOS "Navigation Bar Title" — 17pt SemiBold (reuse headlineMedium sizing)
    titleLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 17.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.024f).em,
    ),

    // iOS "Callout" — 16pt — list item primary label, emphasized body
    titleMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 21.sp,
        letterSpacing = (-0.020f).em,
    ),

    // iOS "Subhead" — 15pt — secondary list label, tab bar label
    titleSmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = (-0.012f).em,
    ),

    // ── Body ─── readable content ──────────────────────────────────────────

    // iOS "Body" — 17pt — descriptions, transaction notes
    bodyLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 17.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.024f).em,
    ),

    // iOS "Subhead" — 15pt — secondary body, list item description
    bodyMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 15.sp,
        lineHeight    = 20.sp,
        letterSpacing = (-0.012f).em,
    ),

    // iOS "Footnote" — 13pt — timestamps, helper text, disclaimer
    bodySmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 13.sp,
        lineHeight    = 18.sp,
        letterSpacing = (-0.006f).em,
    ),

    // ── Label ─── UI chrome, interactive elements ─────────────────────────

    // iOS "Button" size — 17pt Medium — primary button text, tab bar active
    labelLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Medium,
        fontSize      = 17.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.024f).em,
    ),

    // iOS "Caption 1" — 12pt — tags, chips, small metadata, badge text
    labelMedium = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0f.em,
    ),

    // iOS "Caption 2" — 11pt — micro labels, overline, unit suffixes
    labelSmall = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        lineHeight    = 13.sp,
        letterSpacing = (0.006f).em,
    ),
)
