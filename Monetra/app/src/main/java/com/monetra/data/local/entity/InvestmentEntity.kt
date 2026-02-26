package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.Investment
import com.monetra.domain.model.InvestmentType

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: InvestmentType,
    val startDate: java.time.LocalDate,
    val amount: Double,
    val monthlyAmount: Double,
    val interestRate: Double,
    val currentValue: Double,
    val frequency: com.monetra.domain.model.ContributionFrequency
)

fun InvestmentEntity.toDomain(): Investment = Investment(
    id = id,
    name = name,
    type = type,
    startDate = startDate,
    amount = amount,
    monthlyAmount = monthlyAmount,
    interestRate = interestRate,
    currentValue = currentValue,
    frequency = frequency
)

fun Investment.toEntity(): InvestmentEntity = InvestmentEntity(
    id = id,
    name = name,
    type = type,
    startDate = startDate,
    amount = amount,
    monthlyAmount = monthlyAmount,
    interestRate = interestRate,
    currentValue = currentValue,
    frequency = frequency
)
