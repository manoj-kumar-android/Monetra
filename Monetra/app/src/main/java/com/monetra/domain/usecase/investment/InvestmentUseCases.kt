package com.monetra.domain.usecase.investment

import com.monetra.domain.model.Investment
import com.monetra.domain.repository.InvestmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInvestmentsUseCase @Inject constructor(
    private val repository: InvestmentRepository
) {
    operator fun invoke(): Flow<List<Investment>> = repository.getInvestments()
}

class AddInvestmentUseCase @Inject constructor(
    private val repository: InvestmentRepository
) {
    suspend operator fun invoke(investment: Investment) = repository.upsertInvestment(investment)
}

class DeleteInvestmentUseCase @Inject constructor(
    private val repository: InvestmentRepository
) {
    suspend operator fun invoke(investment: Investment) = repository.deleteInvestment(investment.id)
}
