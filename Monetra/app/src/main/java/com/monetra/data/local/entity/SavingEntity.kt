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
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val bankName: String,
    val amount: Double,
    val interestRate: Double?,
    val note: String,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity {
    fun toSaving(): Saving = Saving(
        id = id,
        remoteId = remoteId,
        bankName = bankName,
        amount = amount,
        interestRate = interestRate,
        note = note,
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}

fun Saving.toSavingEntity(): SavingEntity = SavingEntity(
    id = id,
    remoteId = remoteId,
    bankName = bankName,
    amount = amount,
    interestRate = interestRate,
    note = note,
    updatedAt = updatedAt,
    deviceId = deviceId,
    isSynced = isSynced
)

fun Savings.toSavingEntity(): SavingEntity = SavingEntity(
    id = id,
    remoteId = remoteId,
    bankName = bankName,
    amount = amount,
    interestRate = interestRate,
    note = note,
    updatedAt = updatedAt,
    deviceId = deviceId,
    isSynced = isSynced
)
