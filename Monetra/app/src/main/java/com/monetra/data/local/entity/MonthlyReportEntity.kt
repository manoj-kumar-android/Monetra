package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "monthly_reports")
data class MonthlyReportEntity(
    @PrimaryKey val month: String, // Format: YYYY-MM
    override val remoteId: String = "report_$month",
    val income: Double,
    val expenses: Double,
    val emis: Double,
    val investments: Double,
    val actualSavings: Double,
    val targetSavings: Double,
    val status: String,
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity
