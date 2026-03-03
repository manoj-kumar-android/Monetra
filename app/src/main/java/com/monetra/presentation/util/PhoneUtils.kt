package com.monetra.presentation.util

object PhoneUtils {
    /**
     * Extracts the core 10-digit Indian mobile number from any input string.
     * Removes all non-digit characters and strips +91 or 91 if present for numbers > 10 digits.
     */
    fun sanitizeToTenDigits(input: String): String {
        // Remove all non-digit characters
        val digitsOnly = input.filter { it.isDigit() }
        
        return when {
            // If it starts with 91 and has correctly captured more digit(s)+number, strip 91
            digitsOnly.startsWith("91") && digitsOnly.length > 10 -> {
                digitsOnly.drop(2).take(10)
            }
            // Handle 0-prefixed manual entries
            digitsOnly.startsWith("0") && digitsOnly.length > 10 -> {
                digitsOnly.drop(1).take(10)
            }
            // Otherwise return what we have (up to 10 digits). 
            // Using take(10) restricts additional typing once 10 digits are reached.
            else -> digitsOnly.take(10)
        }
    }

    /**
     * Validates if a string is a valid 10-digit Indian mobile number.
     * - Exactly 10 digits
     * - Starts with 6, 7, 8, or 9
     */
    fun isValidIndianMobile(number: String): Boolean {
        if (number.length != 10) return false
        val firstChar = number.firstOrNull() ?: return false
        return firstChar in '6'..'9'
    }

    /**
     * Handles normalization for pasted text or contact picker results.
     */
    fun normalize(input: String): String {
        return sanitizeToTenDigits(input)
    }
}
