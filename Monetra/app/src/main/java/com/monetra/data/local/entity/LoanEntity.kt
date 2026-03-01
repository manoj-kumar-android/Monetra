package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monetra.domain.model.Loan
import java.time.LocalDate

import kotlinx.serialization.Serializable
import com.monetra.data.local.util.LocalDateSerializer

@Serializable
@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val totalPrincipal: Double,
    val annualInterestRate: Double = 0.0,
    val monthlyEmi: Double,
    @Serializable(with = LocalDateSerializer::class)
    val startDate: LocalDate,
    val tenureMonths: Int,
    val remainingTenure: Int,
    val category: String
)

fun LoanEntity.toDomainModel() = Loan(
    id = id,
    name = name,
    totalPrincipal = totalPrincipal,
    annualInterestRate = annualInterestRate,
    monthlyEmi = monthlyEmi,
    startDate = startDate,
    tenureMonths = tenureMonths,
    remainingTenure = remainingTenure,
    category = category
)

fun Loan.toEntity() = LoanEntity(
    id = id,
    name = name,
    totalPrincipal = totalPrincipal,
    annualInterestRate = annualInterestRate,
    monthlyEmi = monthlyEmi,
    startDate = startDate,
    tenureMonths = tenureMonths,
    remainingTenure = remainingTenure,
    category = category
)
