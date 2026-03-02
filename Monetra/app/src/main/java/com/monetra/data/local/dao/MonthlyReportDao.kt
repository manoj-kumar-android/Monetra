package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.MonthlyReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyReportDao {
    @Query("SELECT * FROM monthly_reports WHERE month = :month")
    suspend fun getReportByMonth(month: String): MonthlyReportEntity?

    @Query("SELECT * FROM monthly_reports ORDER BY month DESC")
    fun getAllReports(): Flow<List<MonthlyReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: MonthlyReportEntity)

    @Query("SELECT * FROM monthly_reports WHERE isSynced = 0")
    suspend fun getUnsyncedReports(): List<MonthlyReportEntity>

    @Query("SELECT * FROM monthly_reports WHERE remoteId = :remoteId")
    suspend fun getReportByRemoteId(remoteId: String): MonthlyReportEntity?

    @Query("UPDATE monthly_reports SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSynced(remoteIds: List<String>)

    suspend fun upsertSync(entity: MonthlyReportEntity) {
        val existing = getReportByRemoteId(entity.remoteId)
        if (existing == null) {
            insertReport(entity.copy(isSynced = true))
        } else if (entity.updatedAt > existing.updatedAt) {
            insertReport(entity.copy(isSynced = true))
        }
    }

    @Query("SELECT * FROM monthly_reports")
    suspend fun getAllMonthlyReportsList(): List<MonthlyReportEntity>

    @Query("DELETE FROM monthly_reports")
    suspend fun deleteAllMonthlyReports()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMonthlyReports(reports: List<MonthlyReportEntity>)
}
