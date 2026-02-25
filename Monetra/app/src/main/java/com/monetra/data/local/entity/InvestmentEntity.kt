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
    val currentValuation: Double,
    val investedAmount: Double,
    val monthlyAmount: Double,
    val isMonthly: Boolean
)

fun InvestmentEntity.toDomain(): Investment = Investment(
    id = id,
    name = name,
    type = type,
    currentValuation = currentValuation,
    investedAmount = investedAmount,
    monthlyAmount = monthlyAmount,
    isMonthly = isMonthly
)

fun Investment.toEntity(): InvestmentEntity = InvestmentEntity(
    id = id,
    name = name,
    type = type,
    currentValuation = currentValuation,
    investedAmount = investedAmount,
    monthlyAmount = monthlyAmount,
    isMonthly = isMonthly
)
