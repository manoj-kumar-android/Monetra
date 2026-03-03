package com.monetra.data.local.entity

import android.os.Build
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.data.local.util.LocalDateSerializer
import com.monetra.domain.model.Investment
import com.monetra.domain.model.InvestmentType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: InvestmentType,
    @Serializable(with = LocalDateSerializer::class)
    val startDate: java.time.LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    val endDate: java.time.LocalDate? = null,
    val amount: Double,
    val monthlyAmount: Double,
    val interestRate: Double,
    val currentValue: Double,
    val frequency: com.monetra.domain.model.ContributionFrequency,
    val stepChanges: String = "",
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity

@androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
fun InvestmentEntity.toDomain(): Investment {
    val decodedSteps = stepChanges.split("|")
        .filter { it.isNotBlank() }
        .map {
            val parts = it.split(";")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                com.monetra.domain.model.StepChange(parts[0].toDouble(), java.time.LocalDate.parse(parts[1]))
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        }
    return Investment(
        id = id,
        remoteId = remoteId,
        name = name,
        type = type,
        startDate = startDate,
        endDate = endDate,
        amount = amount,
        monthlyAmount = monthlyAmount,
        interestRate = interestRate,
        currentValue = currentValue,
        frequency = frequency,
        stepChanges = decodedSteps,
        version = version,
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}

fun Investment.toEntity(): InvestmentEntity {
    val encodedSteps = stepChanges.joinToString("|") { "${it.amount};${it.effectiveDate}" }
    return InvestmentEntity(
        id = id,
        remoteId = remoteId,
        name = name,
        type = type,
        startDate = startDate,
        endDate = endDate,
        amount = amount,
        monthlyAmount = monthlyAmount,
        interestRate = interestRate,
        currentValue = currentValue,
        frequency = frequency,
        stepChanges = encodedSteps,
        version = version,
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}
