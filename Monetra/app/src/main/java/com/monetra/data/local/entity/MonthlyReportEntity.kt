package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_reports")
data class MonthlyReportEntity(
    @PrimaryKey val month: String, // Format: YYYY-MM
    val income: Double,
    val expenses: Double,
    val emis: Double,
    val investments: Double,
    val actualSavings: Double,
    val targetSavings: Double,
    val status: String
)
