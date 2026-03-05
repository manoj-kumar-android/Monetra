package com.monetra.domain.usecase

import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.SyncRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class BackupValidationResult {
    object Success : BackupValidationResult()
    object NotSignedIn : BackupValidationResult()
    object PermissionMissing : BackupValidationResult()
    data class AccountMismatch(val currentEmail: String, val syncedEmail: String) :
        BackupValidationResult()

    data class BackupExistsConfirmation(val email: String) : BackupValidationResult()
    object NoBackupFound : BackupValidationResult()
}

class ValidateBackupUseCase @Inject constructor(
    private val cloudBackupRepository: CloudBackupRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(ignoreBackupCheck: Boolean = false): BackupValidationResult {
        // 1. Check if user is signed in
        val currentEmail =
            cloudBackupRepository.accountName.first() ?: return BackupValidationResult.NotSignedIn

        // 2. Check Drive permission
        val hasPermission = cloudBackupRepository.checkDrivePermission()
        if (!hasPermission) {
            return BackupValidationResult.PermissionMissing
        }

        // 3. Check email match
        val syncedEmail = syncRepository.getLastSyncedEmail().first()
        if (syncedEmail != null && currentEmail != syncedEmail) {
            return BackupValidationResult.AccountMismatch(currentEmail, syncedEmail)
        }

        // 4. First time sync case (syncedEmail is null)
        if (syncedEmail == null) {
            if (ignoreBackupCheck) return BackupValidationResult.Success

            val backupExists = cloudBackupRepository.isBackupAvailable()
            return if (backupExists) {
                BackupValidationResult.BackupExistsConfirmation(currentEmail)
            } else {
                BackupValidationResult.NoBackupFound
            }
        }

        return BackupValidationResult.Success
    }
}
