package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.PendingTransaction
import com.monetra.domain.model.TransactionType

@Entity(tableName = "pending_transactions")
data class PendingTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val type: String, // INCOME or EXPENSE
    val senderReceiver: String,
    val sourceApp: String,
    val rawText: String,
    val timestamp: Long,
    val referenceId: String? = null
)

fun PendingTransactionEntity.toDomainModel(): PendingTransaction {
    return PendingTransaction(
        id = id,
        amount = amount,
        type = TransactionType.valueOf(type),
        senderReceiver = senderReceiver,
        sourceApp = sourceApp,
        rawText = rawText,
        timestamp = timestamp,
        referenceId = referenceId
    )
}

fun PendingTransaction.toEntity(): PendingTransactionEntity {
    return PendingTransactionEntity(
        id = id,
        amount = amount,
        type = type.name,
        senderReceiver = senderReceiver,
        sourceApp = sourceApp,
        rawText = rawText,
        timestamp = timestamp,
        referenceId = referenceId
    )
}
