package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import java.time.LocalDate

import kotlinx.serialization.Serializable
import com.monetra.data.local.util.LocalDateSerializer

@Serializable
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date", "type"]),
        Index(value = ["remoteId"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val note: String,
    val linkedBillId: Long? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity

fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        remoteId = remoteId,
        title = title,
        amount = amount,
        type = type,
        category = category,
        date = date,
        note = note,
        linkedBillId = linkedBillId,
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        remoteId = remoteId,
        title = title,
        amount = amount,
        type = type,
        category = category,
        date = date,
        note = note,
        linkedBillId = linkedBillId,
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}
