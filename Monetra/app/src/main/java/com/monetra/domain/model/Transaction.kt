package com.monetra.domain.model

import java.time.LocalDate

/**
 * Domain entity — pure Kotlin, no framework annotations.
 * This is the single model the rest of the app works with.
 * Room entities and API DTOs are always mapped to/from this.
 */
data class Transaction(
    val id: Long = 0L,
    val remoteId: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: LocalDate,
    val note: String = "",
    val linkedBillId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deviceId: String = "",
    val isSynced: Boolean = false
)
