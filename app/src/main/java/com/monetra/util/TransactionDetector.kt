package com.monetra.util

import com.monetra.domain.repository.PendingTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionDetector @Inject constructor(
    private val repository: PendingTransactionRepository
) {
    /**
     * Shared logic to process incoming text data from SMS or Notifications.
     * It parses the text, handles duplicates across ALL sources, and saves suggestions.
     */
    suspend fun detectAndSave(text: String, source: String) = withContext(Dispatchers.IO) {
        // Step 1: Parse the raw text into a standard Transaction object
        val pending = NotificationParser.parse(text, source) ?: return@withContext

        // Step 2: Ensure consistency and prevent duplicates across sources (SMS vs Notification)
        // because bank notifications often arrive via both at the same time.
        if (!repository.isDuplicate(pending.referenceId, pending.rawText)) {
            repository.insertPending(pending)
        }
    }
}
