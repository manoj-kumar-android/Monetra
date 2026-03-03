package com.monetra.domain.usecase

import com.monetra.data.sync.SyncManager
import com.monetra.domain.model.SyncState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val syncManager: SyncManager,
    private val syncRepository: com.monetra.domain.repository.SyncRepository
) {
    val syncState: StateFlow<SyncState> = syncManager.syncState

    operator fun invoke() {
        syncRepository.requestSync()
    }
}
