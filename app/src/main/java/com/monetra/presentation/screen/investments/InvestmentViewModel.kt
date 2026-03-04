package com.monetra.presentation.screen.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.data.worker.PendingDeleteManager
import com.monetra.domain.model.ContributionFrequency
import com.monetra.domain.model.Investment
import com.monetra.domain.model.InvestmentType
import com.monetra.domain.usecase.intelligence.GetWealthIntelligenceUseCase
import com.monetra.domain.usecase.investment.AddInvestmentUseCase
import com.monetra.domain.usecase.investment.DeleteInvestmentUseCase
import com.monetra.domain.usecase.investment.GetInvestmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val getInvestments: GetInvestmentsUseCase,
    private val getWealthIntelligence: GetWealthIntelligenceUseCase,
    private val addInvestment: AddInvestmentUseCase,
    private val deleteInvestment: DeleteInvestmentUseCase,
    private val userPreferenceRepository: com.monetra.domain.repository.UserPreferenceRepository,
    private val pendingDeleteManager: PendingDeleteManager
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yy")

    private val _sheetState = MutableStateFlow(AddInvestmentSheetState())
    private val _pendingDeleteInvestment = MutableStateFlow<Investment?>(null)

    private val _rawInvestments = getInvestments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    private val _pendingDeleteIds = pendingDeleteManager.getPendingIds("INVESTMENT")

    private val wealthIntelligence = getWealthIntelligence()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<InvestmentScreenState> = combine(
        _rawInvestments,
        _pendingDeleteIds,
        wealthIntelligence,
        _sheetState,
        _pendingDeleteInvestment
    ) { rawList, pendingIds, intel, sheet, pendingDel ->
        val activeList = rawList.filter { it.id !in pendingIds }
        val today = LocalDate.now()

        val monthly = activeList.filter { it.frequency == ContributionFrequency.MONTHLY }
            .map { mapToUiModel(it, today) }
        val oneTime = activeList.filter { it.frequency == ContributionFrequency.ONE_TIME }
            .map { mapToUiModel(it, today) }

        InvestmentScreenState(
            intelligence = intel,
            monthlyInvestments = monthly,
            oneTimeInvestments = oneTime,
            sheetState = sheet,
            pendingDeleteMessage = pendingDel?.let { "1 investment deleted" }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvestmentScreenState())

    fun onEvent(event: InvestmentEvent) {
        when (event) {
            is InvestmentEvent.NavigateBack -> { /* Handled in UI */ }
            is InvestmentEvent.NavigateToHelp -> { /* Handled in UI */ }
            is InvestmentEvent.AddNewClick -> openSheetForAdd()
            is InvestmentEvent.EditClick -> openSheetForEdit(event.investment)
            is InvestmentEvent.DeleteRequest -> requestDeleteInvestment(event.investment)
            is InvestmentEvent.UndoDelete -> undoDeleteInvestment()
            
            is InvestmentEvent.DismissSheet -> _sheetState.update { AddInvestmentSheetState() }
            is InvestmentEvent.NameChanged -> updateSheetAndPreview { it.copy(name = event.name, nameError = null) }
            is InvestmentEvent.AmountChanged -> {
                val san = sanitizeNumber(event.amount)
                updateSheetAndPreview { it.copy(amount = san, amountError = null) }
            }
            is InvestmentEvent.MonthlyAmountChanged -> {
                val san = sanitizeNumber(event.amount)
                updateSheetAndPreview { it.copy(monthlyAmount = san, monthlyAmountError = null) }
            }
            is InvestmentEvent.InterestRateChanged -> {
                val san = sanitizeNumber(event.rate)
                val rate = san.toDoubleOrNull() ?: 0.0
                if (rate <= 30.0) {
                    updateSheetAndPreview { it.copy(interestRate = san, interestRateError = null) }
                }
            }
            is InvestmentEvent.CurrentValueChanged -> {
                val san = sanitizeNumber(event.value)
                updateSheetAndPreview { it.copy(currentValue = san, currentValueError = null) }
            }
            is InvestmentEvent.StartDateChanged -> {
                if (!event.date.isAfter(LocalDate.now())) {
                    updateSheetAndPreview { it.copy(startDate = event.date) }
                }
            }
            is InvestmentEvent.EndDateChanged -> updateSheetAndPreview { it.copy(endDate = event.date) }
            is InvestmentEvent.TypeChanged -> handleTypeChange(event.type)
            is InvestmentEvent.AddStepChange -> {
                updateSheetAndPreview { state ->
                    val updated = state.stepChanges.map { it.raw }.toMutableList()
                    updated.add(com.monetra.domain.model.StepChange(event.amount, event.date))
                    updated.sortBy { it.effectiveDate }
                    state.copy(stepChanges = updated.map { mapStepChange(it) })
                }
            }
            is InvestmentEvent.RemoveStepChange -> {
                updateSheetAndPreview { state ->
                    val updated = state.stepChanges.map { it.raw }.toMutableList()
                    updated.remove(event.step)
                    state.copy(stepChanges = updated.map { mapStepChange(it) })
                }
            }
            is InvestmentEvent.SaveInvestment -> saveInvestment()
            
            is InvestmentEvent.SimulationRateChanged -> onSimulationRateChange(event.rate)
            is InvestmentEvent.SimulationYearsChanged -> onSimulationYearsChange(event.years)
        }
    }

    private fun mapToUiModel(inv: Investment, today: LocalDate): InvestmentUiModel {
        val invested = inv.calculateTotalInvested(today)
        val currentVal = inv.calculateCurrentValue(today)
        val returns = inv.calculateTotalReturns(today)
        val returnPercent = inv.calculateReturnPercentage(today)
        val isPositive = returns >= 0

        val yieldStr = if (inv.type in listOf(InvestmentType.EPF, InvestmentType.PPF, InvestmentType.FIXED_DEPOSIT, InvestmentType.RECURRING_DEPOSIT)) {
            "${inv.interestRate}% P.A."
        } else null

        return InvestmentUiModel(
            id = inv.id,
            name = inv.name,
            typeDisplayName = inv.type.displayName,
            typeEmoji = inv.type.emoji,
            typeColorHex = inv.type.colorHex,
            investedAmountStr = "₹%,.0f".format(invested),
            currentValueStr = "₹%,.0f".format(currentVal),
            returnsStr = "${if (isPositive) "+" else ""}₹%,.0f".format(returns),
            returnPercentStr = "${if (isPositive) "+" else ""}${String.format("%.2f", returnPercent)}%",
            isPositiveReturn = isPositive,
            startDateStr = "Since ${inv.startDate.format(dateFormatter)}",
            yieldStr = yieldStr,
            frequencyStr = if (inv.frequency == ContributionFrequency.MONTHLY) "Monthly SIP" else "One-Time",
            raw = inv
        )
    }

    private fun handleTypeChange(type: InvestmentType) {
        val isMarketBased = type in listOf(
            InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.GOLD,
            InvestmentType.MUTUAL_FUND, InvestmentType.OTHER, InvestmentType.REAL_ESTATE
        )
        val needsInterest = !isMarketBased
        
        updateSheetAndPreview {
            it.copy(
                selectedType = type,
                typeTagStr = if (type.defaultFrequency == ContributionFrequency.MONTHLY) "Monthly Investment" else "One-Time Investment",
                showMonthlyAmount = type.defaultFrequency == ContributionFrequency.MONTHLY,
                showInterestRate = needsInterest,
                showCurrentValue = isMarketBased,
                showStepChanges = type.defaultFrequency == ContributionFrequency.MONTHLY
            )
        }
    }

    private fun updateSheetAndPreview(updater: (AddInvestmentSheetState) -> AddInvestmentSheetState) {
        _sheetState.update { old ->
            val state = updater(old)
            val amount = state.amount.toDoubleOrNull() ?: 0.0
            val monthlyAmount = state.monthlyAmount.toDoubleOrNull() ?: 0.0
            val interestRate = state.interestRate.toDoubleOrNull() ?: 0.0
            val currentValueInput = state.currentValue.toDoubleOrNull() ?: 0.0
            val frequency = state.selectedType.defaultFrequency

            val tempInvestment = Investment(
                name = "Preview",
                type = state.selectedType,
                startDate = state.startDate,
                endDate = state.endDate,
                amount = if (frequency == ContributionFrequency.ONE_TIME) amount else 0.0,
                monthlyAmount = if (frequency == ContributionFrequency.MONTHLY) monthlyAmount else 0.0,
                interestRate = interestRate,
                currentValue = if (currentValueInput > 0) currentValueInput else if (frequency == ContributionFrequency.ONE_TIME) amount else 0.0,
                frequency = frequency,
                stepChanges = state.stepChanges.map { it.raw }
            )

            val limitDate = state.endDate ?: LocalDate.now()
            val calcDate = if (limitDate.isAfter(LocalDate.now())) limitDate else LocalDate.now()

            state.copy(
                previewInvested = tempInvestment.calculateTotalInvested(calcDate),
                previewWealth = tempInvestment.calculateCurrentValue(calcDate)
            )
        }
    }

    private fun mapStepChange(step: com.monetra.domain.model.StepChange): StepChangeUiModel {
        return StepChangeUiModel(
            displayStr = "₹%,.0f from ${step.effectiveDate.format(dateFormatter)}".format(step.amount),
            raw = step
        )
    }

    private fun sanitizeNumber(v: String): String {
        val sanitized = v.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) {
            val idx = sanitized.indexOf('.')
            return sanitized.substring(0, idx + 1) + sanitized.substring(idx + 1).replace(".", "")
        }
        return sanitized
    }

    private fun openSheetForAdd() {
        _sheetState.update {
            AddInvestmentSheetState(
                isOpen = true,
                isEditing = false,
                title = "Add Investment",
                selectedType = InvestmentType.SIP,
                typeTagStr = "Monthly Investment",
                showMonthlyAmount = true,
                showInterestRate = false,
                showCurrentValue = true,
                showStepChanges = true
            )
        }
    }

    private fun openSheetForEdit(inv: Investment) {
        val isMarketBased = inv.type in listOf(
            InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.GOLD,
            InvestmentType.MUTUAL_FUND, InvestmentType.OTHER, InvestmentType.REAL_ESTATE
        )
        val needsInterest = !isMarketBased

        val state = AddInvestmentSheetState(
            isOpen = true,
            isEditing = true,
            editingId = inv.id,
            title = "Edit Investment",
            name = inv.name,
            selectedType = inv.type,
            typeTagStr = if (inv.frequency == ContributionFrequency.MONTHLY) "Monthly Investment" else "One-Time Investment",
            showMonthlyAmount = inv.frequency == ContributionFrequency.MONTHLY,
            amount = if (inv.frequency == ContributionFrequency.ONE_TIME) inv.amount.toString() else "",
            monthlyAmount = if (inv.frequency == ContributionFrequency.MONTHLY) inv.monthlyAmount.toString() else "",
            showInterestRate = needsInterest,
            interestRate = inv.interestRate.toString(),
            showCurrentValue = isMarketBased,
            currentValue = inv.currentValue.toString(),
            startDate = inv.startDate,
            endDate = inv.endDate,
            showStepChanges = inv.frequency == ContributionFrequency.MONTHLY,
            stepChanges = inv.stepChanges.map { mapStepChange(it) }
        )
        updateSheetAndPreview { state }
    }

    private fun saveInvestment() {
        val state = _sheetState.value
        var hasError = false

        if (state.name.isBlank()) {
            _sheetState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }

        val amount = state.amount.toDoubleOrNull() ?: 0.0
        val monthlyAmount = state.monthlyAmount.toDoubleOrNull() ?: 0.0
        val interestRate = state.interestRate.toDoubleOrNull() ?: 0.0
        val currentValueInput = state.currentValue.toDoubleOrNull() ?: 0.0
        val frequency = state.selectedType.defaultFrequency

        if (frequency == ContributionFrequency.MONTHLY) {
            if (monthlyAmount <= 0) {
                _sheetState.update { it.copy(monthlyAmountError = "Required") }
                hasError = true
            }
        } else {
            if (amount <= 0) {
                _sheetState.update { it.copy(amountError = "Required") }
                hasError = true
            }
        }

        if (hasError) return

        viewModelScope.launch {
            addInvestment(
                Investment(
                    id = state.editingId ?: 0L,
                    name = state.name,
                    type = state.selectedType,
                    startDate = state.startDate,
                    amount = if (frequency == ContributionFrequency.ONE_TIME) amount else 0.0,
                    monthlyAmount = if (frequency == ContributionFrequency.MONTHLY) monthlyAmount else 0.0,
                    interestRate = interestRate,
                    currentValue = if (currentValueInput > 0) currentValueInput else if (frequency == ContributionFrequency.ONE_TIME) amount else 0.0,
                    frequency = frequency,
                    endDate = state.endDate,
                    stepChanges = state.stepChanges.map { it.raw }
                )
            )
            _sheetState.update { AddInvestmentSheetState() } // Close sheet
        }
    }

    private fun requestDeleteInvestment(investment: Investment) {
        _pendingDeleteInvestment.value = investment
        viewModelScope.launch {
            pendingDeleteManager.requestDelete(investment.id, investment.remoteId, "INVESTMENT")
        }
    }

    private fun undoDeleteInvestment() {
        val investment = _pendingDeleteInvestment.value ?: return
        _pendingDeleteInvestment.value = null
        viewModelScope.launch {
            pendingDeleteManager.cancelDelete(investment.id, "INVESTMENT")
        }
    }

    private fun onSimulationRateChange(rate: Double) {
        viewModelScope.launch {
            val current = userPreferenceRepository.getUserPreferences().first()
            userPreferenceRepository.saveUserPreferences(current.copy(projectionRate = rate))
        }
    }

    private fun onSimulationYearsChange(years: Int) {
        viewModelScope.launch {
            val current = userPreferenceRepository.getUserPreferences().first()
            userPreferenceRepository.saveUserPreferences(current.copy(projectionYears = years))
        }
    }
}
