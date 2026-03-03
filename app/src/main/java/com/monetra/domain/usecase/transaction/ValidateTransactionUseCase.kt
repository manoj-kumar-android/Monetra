package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.Transaction
import java.time.LocalDate
import javax.inject.Inject

class ValidateTransactionUseCase @Inject constructor() {

    data class ValidationResult(
        val titleError: String? = null,
        val amountError: String? = null,
        val genericError: String? = null
    ) {
        val isValid: Boolean
            get() = titleError == null && amountError == null && genericError == null
    }

    operator fun invoke(title: String, amount: String, date: LocalDate): ValidationResult {
        var titleError: String? = null
        var amountError: String? = null
        var genericError: String? = null

        if (title.isBlank()) {
            titleError = "Title cannot be empty."
        }
        
        val parsedAmount = amount.toDoubleOrNull()
        if (parsedAmount == null) {
            amountError = "Invalid amount format."
        } else if (parsedAmount <= 0) {
            amountError = "Amount must be strictly greater than 0."
        }
        
        if (date.isAfter(LocalDate.now())) {
            genericError = "Transaction date cannot be in the future."
        }

        return ValidationResult(
            titleError = titleError,
            amountError = amountError,
            genericError = genericError
        )
    }
}
