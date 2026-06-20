package com.monetra.presentation.screen.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.ContributionFrequency
import com.monetra.domain.model.PortfolioData
import com.monetra.domain.model.PortfolioProjection
import com.monetra.domain.model.toFinancialScore
import com.monetra.domain.repository.InvestmentRepository
import com.monetra.domain.repository.LoanRepository
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.SavingRepository
import com.monetra.domain.repository.UserPreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val loanRepository: LoanRepository,
    private val investmentRepository: InvestmentRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository,
    private val savingRepository: SavingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PortfolioUiState>(PortfolioUiState.Loading)
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init {
        observePortfolio()
    }

    private fun observePortfolio() {
        combine(
            userPreferenceRepository.getUserPreferences(),
            loanRepository.getAllLoans(),
            investmentRepository.getInvestments(),
            monthlyExpenseRepository.getAllMonthlyExpenses(),
            savingRepository.getTotalSavingAmount()
        ) { prefs, loans, investments, expenses, saving ->
            val income = prefs.monthlyIncome
            
            // Only consider non-paid loans
            val activeLoans = loans.filter { it.remainingTenure > 0 }
            val totalLoanRemaining = activeLoans.sumOf { it.remainingBalance }
            val totalEmi = activeLoans.sumOf { it.monthlyEmi }

            val averageLoanInterestRate = if (totalLoanRemaining > 0) {
                activeLoans.sumOf { it.remainingBalance * it.annualInterestRate } / totalLoanRemaining
            } else {
                0.0
            }

            val totalInvestmentValue = investments.sumOf { it.calculateCurrentValue() }
            val totalMonthlyInvestment = investments.filter { it.frequency == ContributionFrequency.MONTHLY }.sumOf { it.currentMonthlyAmount() }
            val totalMonthlyExpenses = expenses.sumOf { it.amount }

            // Core calculations
            val netWorth = saving + totalInvestmentValue - totalLoanRemaining
            val freeMoney = income - totalMonthlyExpenses - totalEmi - totalMonthlyInvestment

            // Financial Score: Monthly Investment / Income
            val savingsRate = if (income > 0) totalMonthlyInvestment / income else 0.0
            val financialScore = savingsRate.toFinancialScore()

            // Wealth Projection
            val projYears = prefs.projectionYears.coerceAtLeast(1)
            val annualRate = prefs.projectionRate / 100.0
            val monthlyRate = annualRate / 12.0
            val months = projYears * 12
            val totalInvested = totalMonthlyInvestment * months
            val projectedValue = if (monthlyRate > 0 && totalMonthlyInvestment > 0) {
                totalMonthlyInvestment * (((1 + monthlyRate).pow(months.toDouble()) - 1) / monthlyRate) * (1 + monthlyRate)
            } else {
                totalInvested
            }
            val totalReturns = projectedValue - totalInvested

            val hasData = income > 0

            val portfolio = PortfolioData(
                monthlyIncome = income,
                currentSavings = saving,
                totalInvestmentValue = totalInvestmentValue,
                totalLoanRemaining = totalLoanRemaining,
                totalMonthlyEmi = totalEmi,
                totalMonthlyExpenses = totalMonthlyExpenses,
                totalMonthlyInvestment = totalMonthlyInvestment,
                netWorth = netWorth,
                freeMoney = freeMoney,
                financialScore = financialScore,
                wealthProjection = PortfolioProjection(
                    monthlyContribution = totalMonthlyInvestment,
                    years = projYears,
                    annualRatePercent = prefs.projectionRate,
                    totalInvested = totalInvested,
                    totalReturns = totalReturns,
                    projectedValue = projectedValue
                ),
                averageLoanInterestRate = averageLoanInterestRate,
                hasData = hasData
            )

            if (!hasData) PortfolioUiState.NeedsSetup else PortfolioUiState.Success(portfolio)
        }
        .catch { e -> _uiState.value = PortfolioUiState.Error(e.localizedMessage ?: "Error") }
        .onEach { _uiState.value = it }
        .launchIn(viewModelScope)
    }
}

sealed interface PortfolioUiState {
    data object Loading : PortfolioUiState
    data object NeedsSetup : PortfolioUiState
    data class Success(val data: PortfolioData) : PortfolioUiState
    data class Error(val message: String) : PortfolioUiState
}
