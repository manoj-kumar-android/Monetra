package com.monetra.domain.repository

import com.monetra.domain.model.MonthlyFinancialReport
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface ReportRepository {
    suspend fun saveReport(report: MonthlyFinancialReport)
    suspend fun getReportForMonth(month: YearMonth): MonthlyFinancialReport?
    fun getAllReports(): Flow<List<MonthlyFinancialReport>>
}
