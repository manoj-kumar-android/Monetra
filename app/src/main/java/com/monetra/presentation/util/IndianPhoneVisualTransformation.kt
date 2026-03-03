package com.monetra.presentation.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Visual Transformation for Indian Phone numbers.
 * Formats a 10-digit number into XXXXX XXXXX format.
 * (Note: +91 prefix is typically handled by the TextField's prefix property)
 */
class IndianPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 10) text.text.substring(0, 10) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 4 && trimmed.length > 5) {
                out += " "
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 10) {
                    return if (trimmed.length > 5) offset + 1 else offset
                }
                return if (trimmed.length > 5) 11 else 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 5) return offset
                if (offset <= 11) return offset - 1
                return 10
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}
