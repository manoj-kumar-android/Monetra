package com.monetra.data.sync

import com.monetra.data.backup.model.BackupData
import com.monetra.data.local.MonetraDatabase
import com.monetra.domain.model.SyncState
import com.monetra.domain.repository.SyncRepository
import com.monetra.drivebackup.api.DriveBackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.monetra.data.local.entity.*

@Singleton
class SyncManager @Inject constructor(
    private val localDataSource: LocalSyncDataSource,
    private val driveDataSource: DriveSyncDataSource,
    private val syncRepository: SyncRepository,
    private val driveManager: DriveBackupManager,
    private val db: MonetraDatabase
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    suspend fun runSync() {
        if (_syncState.value is SyncState.Syncing) return
        _syncState.value = SyncState.Syncing("Starting sync...", 0, 10)

        try {
            // Phase 1: Push
            _syncState.value = SyncState.Syncing("Pushing local changes...", 1, 10)
            pushLocalChanges()
            
            // Phase 2: Pull
            _syncState.value = SyncState.Syncing("Pulling remote changes...", 5, 10)
            pullRemoteChanges()

            syncRepository.setLastSyncedAt(System.currentTimeMillis())
            syncRepository.setDirty(false)
            _syncState.value = SyncState.Success
        } catch (e: Exception) {
            handleError(e)
            _syncState.value = SyncState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun pushLocalChanges() {
        val dirtyRecords = localDataSource.getDirtyRecords()
        if (isEmpty(dirtyRecords)) return

        val remoteData = driveDataSource.fetchRemoteData() ?: BackupData()
        val mergedData = mergeForPush(remoteData, dirtyRecords)
        
        val result = driveDataSource.commitRemoteData(mergedData)
        if (result.isSuccess) {
            markLocalSynced(dirtyRecords)
        } else {
            throw result.exceptionOrNull() ?: Exception("Upload failed")
        }
    }

    private suspend fun pullRemoteChanges() {
        val remoteData = driveDataSource.fetchRemoteData() ?: return
        mergeIntoLocal(remoteData)
    }

    private fun mergeForPush(remote: BackupData, localDirty: BackupData): BackupData {
        return BackupData(
            transactions = mergeEntities(remote.transactions, localDirty.transactions),
            savings = mergeEntities(remote.savings, localDirty.savings),
            goals = mergeEntities(remote.goals, localDirty.goals),
            categoryBudgets = mergeEntities(remote.categoryBudgets, localDirty.categoryBudgets),
            investments = mergeEntities(remote.investments, localDirty.investments),
            loans = mergeEntities(remote.loans, localDirty.loans),
            monthlyExpenses = mergeEntities(remote.monthlyExpenses, localDirty.monthlyExpenses),
            billInstances = mergeEntities(remote.billInstances, localDirty.billInstances),
            refundables = mergeEntities(remote.refundables, localDirty.refundables),
            userPreferences = mergeEntities(remote.userPreferences, localDirty.userPreferences),
            createdAt = System.currentTimeMillis()
        )
    }

    private suspend fun mergeIntoLocal(remote: BackupData) {
        localDataSource.mergeRemoteData(remote)
    }

    private fun <T : SyncableEntity> mergeEntities(remote: List<T>, local: List<T>): List<T> {
        val map = remote.associateBy { it.remoteId }.toMutableMap()
        local.forEach { localItem ->
            val remoteItem = map[localItem.remoteId]
            if (remoteItem == null || localItem.updatedAt > remoteItem.updatedAt) {
                map[localItem.remoteId] = localItem
            }
        }
        return map.values.toList()
    }

    private fun isEmpty(data: BackupData): Boolean {
        return data.transactions.isEmpty() && data.savings.isEmpty() && data.goals.isEmpty() && 
               data.investments.isEmpty() && data.loans.isEmpty() && data.refundables.isEmpty()
    }

    private suspend fun markLocalSynced(data: BackupData) {
        localDataSource.markAsSynced(data)
    }

    private suspend fun handleError(e: Exception) {
        if (isAuthError(e)) {
            driveManager.signOut()
        }
    }

    private fun isAuthError(e: Exception): Boolean {
        val msg = e.message?.lowercase() ?: ""
        return msg.contains("auth") || msg.contains("credential") || msg.contains("sign-in")
    }
}
