package com.monetra.presentation.screen.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.*
import com.monetra.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.pow
import javax.inject.Inject

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
            val totalLoanRemaining = activeLoans.sumOf { it.totalPrincipal * (it.remainingTenure.toDouble() / it.tenureMonths.toDouble()).coerceIn(0.0, 1.0) }
            val totalEmi = activeLoans.sumOf { it.monthlyEmi }

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
