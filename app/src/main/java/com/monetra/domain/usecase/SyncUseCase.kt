package com.monetra.domain.usecase

import com.monetra.data.sync.SyncManager
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val syncManager: SyncManager,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) {

    operator fun invoke() {
        syncRepository.requestSync()
    }

    suspend fun runSync(): Result<Unit> {
        return try {
            syncManager.runSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
