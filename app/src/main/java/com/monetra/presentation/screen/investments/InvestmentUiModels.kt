package com.monetra.presentation.screen.investments

import com.monetra.domain.model.Investment
import com.monetra.domain.model.InvestmentType
import com.monetra.domain.model.StepChange
import com.monetra.domain.model.WealthIntelligence
import java.time.LocalDate

data class InvestmentScreenState(
    val intelligence: WealthIntelligence? = null,
    val monthlyInvestments: List<InvestmentUiModel> = emptyList(),
    val oneTimeInvestments: List<InvestmentUiModel> = emptyList(),
    val sheetState: AddInvestmentSheetState = AddInvestmentSheetState(),
    val pendingDeleteMessage: String? = null
)

data class InvestmentUiModel(
    val id: Long,
    val name: String,
    val typeDisplayName: String,
    val typeEmoji: String,
    val typeColorHex: Long,
    val investedAmountStr: String,
    val currentValueStr: String,
    val returnsStr: String,
    val returnPercentStr: String,
    val isPositiveReturn: Boolean,
    val startDateStr: String,
    val yieldStr: String?,
    val frequencyStr: String,
    val raw: Investment
)

data class AddInvestmentSheetState(
    val isOpen: Boolean = false,
    val isEditing: Boolean = false,
    val editingId: Long? = null,
    val title: String = "",
    
    val name: String = "",
    val nameError: String? = null,
    
    val selectedType: InvestmentType = InvestmentType.SIP,
    val typeTagStr: String = "",
    
    val showMonthlyAmount: Boolean = true,
    val amount: String = "",
    val amountError: String? = null,
    val monthlyAmount: String = "",
    val monthlyAmountError: String? = null,
    
    val showInterestRate: Boolean = true,
    val interestRate: String = "0",
    val interestRateError: String? = null,
    
    val showCurrentValue: Boolean = false,
    val currentValue: String = "",
    val currentValueError: String? = null,
    
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val endDateStr: String = "Ongoing",
    
    val showStepChanges: Boolean = true,
    val stepChanges: List<StepChangeUiModel> = emptyList(),
    
    val previewInvested: Double = 0.0,
    val previewWealth: Double = 0.0
)

data class StepChangeUiModel(
    val displayStr: String,
    val raw: StepChange
)

sealed interface InvestmentEvent {
    object NavigateBack : InvestmentEvent
    object NavigateToHelp : InvestmentEvent
    
    object AddNewClick : InvestmentEvent
    data class EditClick(val investment: Investment) : InvestmentEvent
    data class DeleteRequest(val investment: Investment) : InvestmentEvent
    object UndoDelete : InvestmentEvent
    
    object DismissSheet : InvestmentEvent
    data class NameChanged(val name: String) : InvestmentEvent
    data class AmountChanged(val amount: String) : InvestmentEvent
    data class MonthlyAmountChanged(val amount: String) : InvestmentEvent
    data class InterestRateChanged(val rate: String) : InvestmentEvent
    data class CurrentValueChanged(val value: String) : InvestmentEvent
    data class StartDateChanged(val date: LocalDate) : InvestmentEvent
    data class EndDateChanged(val date: LocalDate?) : InvestmentEvent
    data class TypeChanged(val type: InvestmentType) : InvestmentEvent
    data class AddStepChange(val amount: Double, val date: LocalDate) : InvestmentEvent
    data class RemoveStepChange(val step: StepChange) : InvestmentEvent
    object SaveInvestment : InvestmentEvent
    
    data class SimulationRateChanged(val rate: Double) : InvestmentEvent
    data class SimulationYearsChanged(val years: Int) : InvestmentEvent
}
