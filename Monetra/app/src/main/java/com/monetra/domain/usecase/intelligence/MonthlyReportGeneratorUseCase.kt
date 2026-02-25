package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.*
import com.monetra.domain.repository.ReportRepository
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import javax.inject.Inject

/**
 * Monthly Report Generator
 * 
 * Generates a comprehensive monthly report including breakdowns, comparison with 
 * previous months, and financial stress analysis.
 */
class MonthlyReportGeneratorUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val reportRepository: ReportRepository,
    private val generateFinancialReport: GenerateMonthlyFinancialReportUseCase,
    private val generateSuggestions: GenerateSavingSuggestionsUseCase
) {
    suspend fun generate(month: YearMonth): ComprehensiveMonthlyReport {
        // 1. Generate/Fetch current data
        val report = generateFinancialReport(month).first()
        
        // 2. Persist for future comparisons
        reportRepository.saveReport(report)
        
        // 3. Get previous month for comparison
        val prevMonth = month.minusMonths(1)
        val prevReport = reportRepository.getReportForMonth(prevMonth)
        
        // 4. Analyze top spending categories
        val categorySums = transactionRepository.getExpenseSumByCategory(month).first()
        val topCategories = categorySums.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { CategorySpending(it.key, it.value) }
            
        // 5. Get suggestions
        val suggestions = generateSuggestions(month).first()
        
        // 6. Calculate stress and comparison
        val comparison = prevReport?.let { prev ->
            PreviousMonthComparison(
                incomeChangePercent = if (prev.income > 0) ((report.income - prev.income) / prev.income) * 100 else 0.0,
                expenseChangePercent = if (prev.totalExpenses > 0) ((report.totalExpenses - prev.totalExpenses) / prev.totalExpenses) * 100 else 0.0,
                savingsChangePercent = if (prev.actualSavings != 0.0) ((report.actualSavings - prev.actualSavings) / Math.abs(prev.actualSavings)) * 100 else 0.0,
                investmentChangePercent = if (prev.totalInvestments > 0) ((report.totalInvestments - prev.totalInvestments) / prev.totalInvestments) * 100 else 0.0
            )
        }
        
        // 6. All features unlocked for all users
        
        return ComprehensiveMonthlyReport(
            month = month,
            income = report.income,
            expenses = report.totalExpenses,
            emis = report.totalEmis,
            investments = report.totalInvestments,
            actualSavings = report.actualSavings,
            targetSavings = report.targetSavings,
            topCategories = topCategories,
            suggestions = suggestions,
            status = report.status,
            emiStressLevel = when {
                report.emiToIncomeRatio > 50 -> "CRITICAL"
                report.emiToIncomeRatio > 35 -> "HIGH"
                report.emiToIncomeRatio > 20 -> "MODERATE"
                else -> "HEALTHY"
            },
            comparison = comparison
        )
    }

    /**
     * Placeholder for Premium Export Feature
     */
    fun exportToCSV(report: ComprehensiveMonthlyReport): String {
        val builder = StringBuilder()
        builder.append("Month,Income,Expenses,EMI,Investments,Savings,Status\n")
        builder.append("${report.month},${report.income},${report.expenses},${report.emis},${report.investments},${report.actualSavings},${report.status}\n")
        return builder.toString()
    }

    /**
     * Placeholder for Premium PDF feature
     */
    fun exportToPDF(report: ComprehensiveMonthlyReport): ByteArray {
        // In a real app, use a PDF library like iText or PdfBox
        return "PDF Content for ${report.month}".toByteArray()
    }
}
