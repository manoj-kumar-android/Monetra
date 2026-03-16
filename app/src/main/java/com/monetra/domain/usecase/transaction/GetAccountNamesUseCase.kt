package com.monetra.domain.usecase.transaction

import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountNamesUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<String>> = repository.getAccountNames()
}
