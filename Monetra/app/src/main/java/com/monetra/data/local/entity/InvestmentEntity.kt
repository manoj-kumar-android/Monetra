package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.Investment
import com.monetra.domain.model.InvestmentType

import kotlinx.serialization.Serializable
import com.monetra.data.local.util.LocalDateSerializer

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
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity

fun InvestmentEntity.toDomain(): Investment {
    val decodedSteps = stepChanges.split("|")
        .filter { it.isNotBlank() }
        .map {
            val parts = it.split(";")
            com.monetra.domain.model.StepChange(parts[0].toDouble(), java.time.LocalDate.parse(parts[1]))
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
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}
