package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUsedCategoriesUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(type: TransactionType?): Flow<List<String>> {
        return repository.getUsedCategories(type)
    }
}
