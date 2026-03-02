package com.monetra.data.repository

import com.monetra.data.local.dao.MonthlyReportDao
import com.monetra.data.local.entity.MonthlyReportEntity
import com.monetra.domain.model.FinancialBalanceStatus
import com.monetra.domain.model.MonthlyFinancialReport
import com.monetra.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val dao: MonthlyReportDao,
    private val syncManager: com.monetra.data.sync.SyncManager,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) : ReportRepository {

    override suspend fun saveReport(report: MonthlyFinancialReport) {
        val deviceId = syncRepository.getDeviceId()
        val existing = dao.getReportByMonth(report.month.toString())
        val nextVersion = if (existing == null) 1L else existing.version + 1L
        
        dao.insertReport(
            MonthlyReportEntity(
                month = report.month.toString(),
                income = report.income,
                expenses = report.totalExpenses,
                emis = report.totalEmis,
                investments = report.totalInvestments,
                actualSavings = report.actualSavings,
                targetSavings = report.targetSavings,
                status = report.status.name,
                version = nextVersion,
                updatedAt = System.currentTimeMillis(),
                deviceId = deviceId,
                isSynced = false
            )
        )
        syncRepository.setDirty(true)
        syncManager.runSync()
    }

    override suspend fun getReportForMonth(month: YearMonth): MonthlyFinancialReport? {
        return dao.getReportByMonth(month.toString())?.let { entity ->
            MonthlyFinancialReport(
                month = YearMonth.parse(entity.month),
                income = entity.income,
                totalExpenses = entity.expenses,
                totalEmis = entity.emis,
                totalInvestments = entity.investments,
                targetSavings = entity.targetSavings,
                actualSavings = entity.actualSavings,
                savingsGap = (entity.targetSavings - entity.actualSavings).coerceAtLeast(0.0),
                expenseToIncomeRatio = if (entity.income > 0) (entity.expenses / entity.income) * 100 else 0.0,
                emiToIncomeRatio = if (entity.income > 0) (entity.emis / entity.income) * 100 else 0.0,
                investmentRatio = if (entity.income > 0) (entity.investments / entity.income) * 100 else 0.0,
                status = FinancialBalanceStatus.valueOf(entity.status),
                remoteId = entity.remoteId,
                version = entity.version,
                updatedAt = entity.updatedAt,
                deviceId = entity.deviceId,
                isSynced = entity.isSynced
            )
        }
    }

    override fun getAllReports(): Flow<List<MonthlyFinancialReport>> {
        return dao.getAllReports().map { entities ->
            entities.map { entity ->
                MonthlyFinancialReport(
                    month = YearMonth.parse(entity.month),
                    income = entity.income,
                    totalExpenses = entity.expenses,
                    totalEmis = entity.emis,
                    totalInvestments = entity.investments,
                    targetSavings = entity.targetSavings,
                    actualSavings = entity.actualSavings,
                    savingsGap = (entity.targetSavings - entity.actualSavings).coerceAtLeast(0.0),
                    expenseToIncomeRatio = if (entity.income > 0) (entity.expenses / entity.income) * 100 else 0.0,
                    emiToIncomeRatio = if (entity.income > 0) (entity.emis / entity.income) * 100 else 0.0,
                    investmentRatio = if (entity.income > 0) (entity.investments / entity.income) * 100 else 0.0,
                    status = FinancialBalanceStatus.valueOf(entity.status),
                    remoteId = entity.remoteId,
                    version = entity.version,
                    updatedAt = entity.updatedAt,
                    deviceId = entity.deviceId,
                    isSynced = entity.isSynced
                )
            }
        }
    }
}
