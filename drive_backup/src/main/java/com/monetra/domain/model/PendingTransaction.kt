package com.monetra.domain.model

data class PendingTransaction(
    val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val senderReceiver: String,
    val sourceApp: String,
    val rawText: String,
    val timestamp: Long,
    val referenceId: String? = null
)
