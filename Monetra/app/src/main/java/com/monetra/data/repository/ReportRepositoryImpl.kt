package com.monetra.data.repository

import com.monetra.data.local.dao.MonthlyReportDao
import com.monetra.data.local.entity.MonthlyReportEntity
import com.monetra.domain.model.FinancialBalanceStatus
import com.monetra.domain.model.MonthlyFinancialReport
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val dao: MonthlyReportDao,
    private val cloudBackupRepository: CloudBackupRepository
) : ReportRepository {

    override suspend fun saveReport(report: MonthlyFinancialReport) {
        dao.insertReport(
            MonthlyReportEntity(
                month = report.month.toString(),
                income = report.income,
                expenses = report.totalExpenses,
                emis = report.totalEmis,
                investments = report.totalInvestments,
                actualSavings = report.actualSavings,
                targetSavings = report.targetSavings,
                status = report.status.name
            )
        )
        cloudBackupRepository.scheduleBackup()
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
                status = FinancialBalanceStatus.valueOf(entity.status)
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
                    status = FinancialBalanceStatus.valueOf(entity.status)
                )
            }
        }
    }
}
