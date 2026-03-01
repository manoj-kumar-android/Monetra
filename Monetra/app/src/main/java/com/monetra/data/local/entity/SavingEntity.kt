package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.Saving
import com.monetra.domain.model.Savings

import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "savings")
data class SavingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,
    val amount: Double,
    val interestRate: Double?,
    val note: String
) {
    fun toSaving(): Saving = Saving(
        id = id,
        bankName = bankName,
        amount = amount,
        interestRate = interestRate,
        note = note
    )
}

fun Saving.toSavingEntity(): SavingEntity = SavingEntity(
    id = id,
    bankName = bankName,
    amount = amount,
    interestRate = interestRate,
    note = note
)

fun Savings.toSavingEntity(): SavingEntity = SavingEntity(
    id = id,
    bankName = bankName,
    amount = amount,
    interestRate = interestRate,
    note = note
)
