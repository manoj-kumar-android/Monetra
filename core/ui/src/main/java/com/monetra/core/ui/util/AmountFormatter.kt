package com.monetra.core.ui.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Common amount formatting utility for displaying currencies (Indian Rupee format)
 * cleanly and readably in the UI.
 */
object AmountFormatter {

    private val indianLocale = Locale.forLanguageTag("en-IN")
    private val numberFormat = NumberFormat.getNumberInstance(indianLocale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

    /**
     * Formats a double amount into a readable Indian grouping format (e.g. 1,50,000).
     */
    fun format(amount: Double): String {
        return try {
            numberFormat.format(amount)
        } catch (e: Exception) {
            amount.toInt().toString()
        }
    }

    /**
     * Formats a string value representing a number into a readable Indian grouping format.
     */
    fun format(amountStr: String): String {
        val amount = amountStr.toDoubleOrNull() ?: return amountStr
        return format(amount)
    }

    /**
     * Formats a double amount and prepends the Rupee currency symbol (e.g. ₹1,50,000).
     */
    fun formatWithSymbol(amount: Double): String {
        return "₹${format(amount)}"
    }

    /**
     * Formats a string amount and prepends the Rupee currency symbol (e.g. ₹1,50,000).
     */
    fun formatWithSymbol(amountStr: String): String {
        val amount = amountStr.toDoubleOrNull() ?: return amountStr
        return "₹${format(amount)}"
    }
}
