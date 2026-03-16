package com.monetra.domain.usecase.transaction

import com.monetra.domain.repository.TransactionRepository
import javax.inject.Inject

class GetLastBalanceUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(accountName: String): Double? = 
        repository.getLastBalanceForAccount(accountName)
}
