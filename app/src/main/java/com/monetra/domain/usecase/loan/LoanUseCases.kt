package com.monetra.domain.usecase.loan

import com.monetra.domain.model.Loan
import com.monetra.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoansUseCase @Inject constructor(
    private val repository: LoanRepository
) {
    operator fun invoke(): Flow<List<Loan>> = repository.getAllLoans()
}

class AddLoanUseCase @Inject constructor(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(loan: Loan) = repository.insertLoan(loan)
}

class DeleteLoanUseCase @Inject constructor(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(loan: Loan) = repository.deleteLoan(loan.id)
}
