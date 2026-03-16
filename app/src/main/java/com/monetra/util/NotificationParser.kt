package com.monetra.util

import com.monetra.domain.model.PendingTransaction
import com.monetra.domain.model.TransactionType
import java.util.regex.Pattern

object NotificationParser {

    private val amountPattern =
        Pattern.compile("(?:Rs\\.?|INR|₹)\\s?([0-9,]+(?:\\.[0-9]{2})?)", Pattern.CASE_INSENSITIVE)

    private val debitKeywords = listOf("debited", "spent", "paid", "sent", "transferred to")
    private val creditKeywords = listOf("credited", "received", "added", "transferred from")

    // Ref ID patterns
    private val refIdPattern =
        Pattern.compile("(?:Ref|UPIRef|id|no)\\s?:?\\s?([0-9]{10,12})", Pattern.CASE_INSENSITIVE)

    fun parse(text: String, packageName: String): PendingTransaction? {
        val amount = extractAmount(text) ?: return null
        val type = detectType(text) ?: return null

        val senderReceiver = extractSenderReceiver(text, type) ?: packageName.split(".").last()
        val refId = extractRefId(text)

        return PendingTransaction(
            amount = amount,
            type = type,
            senderReceiver = senderReceiver,
            sourceApp = packageName,
            rawText = text,
            timestamp = System.currentTimeMillis(),
            referenceId = refId
        )
    }

    private fun extractAmount(text: String): Double? {
        val matcher = amountPattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        }
        return null
    }

    private fun detectType(text: String): TransactionType? {
        val lowercase = text.lowercase()
        if (debitKeywords.any { lowercase.contains(it) }) return TransactionType.EXPENSE
        if (creditKeywords.any { lowercase.contains(it) }) return TransactionType.INCOME
        return null
    }

    private fun extractSenderReceiver(text: String, type: TransactionType): String? {
        // Very basic extraction, can be improved with specific app patterns
        val lowercase = text.lowercase()

        return when (type) {
            TransactionType.EXPENSE -> {
                // Try to find after "to" or "at"
                val toIdx = lowercase.lastIndexOf(" to ")
                val atIdx = lowercase.lastIndexOf(" at ")
                val startIdx = if (toIdx != -1) toIdx + 4 else if (atIdx != -1) atIdx + 4 else -1

                if (startIdx != -1) {
                    val candidate =
                        text.substring(startIdx).trim().split(" ").take(2).joinToString(" ")
                    candidate.takeWhile { it.isLetterOrDigit() || it == ' ' }.trim()
                } else null
            }

            TransactionType.INCOME -> {
                val fromIdx = lowercase.lastIndexOf(" from ")
                if (fromIdx != -1) {
                    val candidate =
                        text.substring(fromIdx + 6).trim().split(" ").take(2).joinToString(" ")
                    candidate.takeWhile { it.isLetterOrDigit() || it == ' ' }.trim()
                } else null
            }
        }
    }

    private fun extractRefId(text: String): String? {
        val matcher = refIdPattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }
}
