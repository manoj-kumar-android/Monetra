package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import java.time.LocalDate

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date", "type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: LocalDate,
    val note: String
)

fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        title = title,
        amount = amount,
        type = type,
        category = category,
        date = date,
        note = note
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        type = type,
        category = category,
        date = date,
        note = note
    )
}
