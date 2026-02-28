package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.Savings

@Entity(tableName = "savings")
data class SavingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,
    val amount: Double,
    val interestRate: Double?,
    val note: String
) {
    fun toSavings(): Savings = Savings(
        id = id,
        bankName = bankName,
        amount = amount,
        interestRate = interestRate,
        note = note
    )
}

fun Savings.toSavingsEntity(): SavingsEntity = SavingsEntity(
    id = id,
    bankName = bankName,
    amount = amount,
    interestRate = interestRate,
    note = note
)
