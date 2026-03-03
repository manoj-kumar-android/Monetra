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
        _syncState.value = SyncState.Syncing("Syncing...", 1, 10)

        try {
            // 1. Fetch Remote State
            val fetchResult = driveDataSource.fetchRemoteData()
            if (fetchResult.isFailure) {
                throw fetchResult.exceptionOrNull() ?: Exception("Failed to fetch remote data")
            }
            val remoteData = fetchResult.getOrNull() ?: BackupData()

            // 2. Merge Remote into Local (Filtering with current local tombstones)
            // This brings in changes from other devices and respects local pending deletions.
            localDataSource.mergeRemoteData(remoteData)

            // 3. Collect local dirty records AFTER merging remote changes
            // (Note: some previously dirty items might have been resolved by remote newer versions)
            val dirtyRecords = localDataSource.getDirtyRecords()

            // 4. If there are local changes (edits or deletes), merge and push to Drive
            if (!isEmpty(dirtyRecords)) {
                _syncState.value = SyncState.Syncing("Pushing changes...", 5, 10)
                val mergedDataForDrive = mergeForPush(remoteData, dirtyRecords)
                
                val commitResult = driveDataSource.commitRemoteData(mergedDataForDrive)
                if (commitResult.isSuccess) {
                    markLocalSynced(dirtyRecords)
                    syncRepository.setDirty(false)
                } else {
                    throw commitResult.exceptionOrNull() ?: Exception("Upload failed")
                }
            } else {
                syncRepository.setDirty(false)
            }

            syncRepository.setLastSyncedAt(System.currentTimeMillis())
            _syncState.value = SyncState.Success
        } catch (e: Exception) {
            handleError(e)
            _syncState.value = SyncState.Error(e.message ?: "Unknown error")
        }
    }

    // pushLocalChanges and pullRemoteChanges are now integrated into runSync for better atomicity

    private fun mergeForPush(remote: BackupData, localDirty: BackupData): BackupData {
        // IDs of entities deleted either locally or remotely.
        // Consolidation here ensures "Delete wins" across the board.
        val allDeletedIds = (remote.deletedEntities.map { it.remoteId } + 
                            localDirty.deletedEntities.map { it.remoteId }).toSet()
        
        return BackupData(
            transactions = mergeEntities(remote.transactions, localDirty.transactions, allDeletedIds),
            savings = mergeEntities(remote.savings, localDirty.savings, allDeletedIds),
            goals = mergeEntities(remote.goals, localDirty.goals, allDeletedIds),
            categoryBudgets = mergeEntities(remote.categoryBudgets, localDirty.categoryBudgets, allDeletedIds),
            investments = mergeEntities(remote.investments, localDirty.investments, allDeletedIds),
            loans = mergeEntities(remote.loans, localDirty.loans, allDeletedIds),
            monthlyExpenses = mergeEntities(remote.monthlyExpenses, localDirty.monthlyExpenses, allDeletedIds),
            billInstances = mergeEntities(remote.billInstances, localDirty.billInstances, allDeletedIds),
            refundables = mergeEntities(remote.refundables, localDirty.refundables, allDeletedIds),
            userPreferences = mergeEntities(remote.userPreferences, localDirty.userPreferences, allDeletedIds),
            deletedEntities = (remote.deletedEntities + localDirty.deletedEntities)
                .associateBy { it.remoteId }
                .values.toList(),
            createdAt = System.currentTimeMillis()
        )
    }

    private fun <T : SyncableEntity> mergeEntities(
        remote: List<T>, 
        local: List<T>, 
        allDeletedIds: Set<String>
    ): List<T> {
        // Filter out any entity that is supposed to be deleted
        val map = remote.filterNot { it.remoteId in allDeletedIds }
            .associateBy { it.remoteId }.toMutableMap()
            
        local.filterNot { it.remoteId in allDeletedIds }.forEach { localItem ->
            val remoteItem = map[localItem.remoteId]
            
            // CONFLICT RESOLUTION: Gold Standard Logic
            // 1. Highest Version wins
            // 2. If Versions equal, Newest Timestamp wins
            // 3. If Timestamps equal, higher deviceId wins (Tie-Breaker)
            val shouldOverwrite = when {
                remoteItem == null -> true
                localItem.version > remoteItem.version -> true
                localItem.version < remoteItem.version -> false
                localItem.updatedAt > remoteItem.updatedAt -> true
                localItem.updatedAt < remoteItem.updatedAt -> false
                else -> localItem.deviceId > remoteItem.deviceId
            }

            if (shouldOverwrite) {
                map[localItem.remoteId] = localItem
            }
        }
        return map.values.toList()
    }

    private fun isEmpty(data: BackupData): Boolean {
        return data.transactions.isEmpty() && 
               data.savings.isEmpty() && 
               data.goals.isEmpty() && 
               data.categoryBudgets.isEmpty() &&
               data.investments.isEmpty() && 
               data.loans.isEmpty() && 
               data.monthlyExpenses.isEmpty() &&
               data.billInstances.isEmpty() &&
               data.refundables.isEmpty() &&
               data.monthlyReports.isEmpty() &&
               data.userPreferences.isEmpty() &&
               data.deletedEntities.isEmpty()
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
