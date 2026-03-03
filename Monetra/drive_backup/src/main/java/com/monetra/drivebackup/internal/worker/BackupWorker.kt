package com.monetra.drivebackup.internal.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.monetra.drivebackup.internal.drive.DriveService
import com.monetra.drivebackup.internal.security.EncryptionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.File

private val android.content.Context.dataStore by preferencesDataStore(name = "drive_backup_prefs")
private val lastBackupTimeKey = longPreferencesKey("last_backup_time")

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val driveService: DriveService,
    private val encryptionManager: EncryptionManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val databasePath = inputData.getString(KEY_DATABASE_PATH) ?: return Result.failure()
        val googleUserId = inputData.getString(KEY_GOOGLE_USER_ID) ?: return Result.failure()
        val accountName = inputData.getString(KEY_ACCOUNT_NAME) ?: return Result.failure()

        val dbFile = File(databasePath)
        if (!dbFile.exists()) return Result.failure()

        val encryptedFile = File(applicationContext.cacheDir, "temp_backup.enc")
        
        return try {
            if (accountName.isBlank()) {
                android.util.Log.e("DriveBackup", "BackupWorker: accountName is null or blank")
                return Result.failure()
            }
            // Ensure drive service is initialized
            driveService.initialize(accountName)

            // Encrypt
            encryptionManager.encrypt(googleUserId, dbFile, encryptedFile)

            // Upload
            driveService.uploadBackup(encryptedFile)

            // Update last backup time
            applicationContext.dataStore.edit { prefs ->
                prefs[lastBackupTimeKey] = System.currentTimeMillis()
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        } finally {
            encryptedFile.delete()
        }
    }

    companion object {
        const val KEY_DATABASE_PATH = "database_path"
        const val KEY_GOOGLE_USER_ID = "google_user_id"
        const val KEY_ACCOUNT_NAME = "account_name"
    }
}
