package com.monetra.core.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A VisualTransformation that formats digit inputs into the Indian numbering format
 * (e.g. 60,000 or 1,50,000) dynamically as the user types, without altering the raw text state.
 */
class IndianCurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = formatIndianNumber(raw)

        // Build the offset mapping array
        val originalToTransformedMap = IntArray(raw.length + 1)
        var transformedIndex = 0
        var originalIndex = 0

        while (originalIndex < raw.length && transformedIndex < formatted.length) {
            originalToTransformedMap[originalIndex] = transformedIndex
            if (formatted[transformedIndex] == ',') {
                transformedIndex++ // Skip the comma in mapped index
            } else {
                originalIndex++
                transformedIndex++
            }
        }
        originalToTransformedMap[raw.length] = formatted.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                return originalToTransformedMap[clamped]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                for (i in 0..raw.length) {
                    if (originalToTransformedMap[i] >= clamped) {
                        return i
                    }
                }
                return raw.length
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }

    private fun formatIndianNumber(text: String): String {
        val cleanText = text.filter { it.isDigit() }
        if (cleanText.isEmpty()) return ""

        val sb = StringBuilder()
        val len = cleanText.length

        var count = 0
        var isFirstGroup = true
        for (i in len - 1 downTo 0) {
            sb.append(cleanText[i])
            count++
            if (isFirstGroup && count == 3 && i > 0) {
                sb.append(',')
                count = 0
                isFirstGroup = false
            } else if (!isFirstGroup && count == 2 && i > 0) {
                sb.append(',')
                count = 0
            }
        }
        return sb.reverse().toString()
    }
}
