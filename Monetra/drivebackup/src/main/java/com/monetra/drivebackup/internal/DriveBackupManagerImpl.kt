package com.monetra.drivebackup.internal

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.monetra.drivebackup.api.DriveBackupManager
import com.monetra.drivebackup.internal.drive.DriveService
import com.monetra.drivebackup.internal.security.EncryptionManager
import com.monetra.drivebackup.internal.worker.BackupWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "drive_backup_prefs")

@Singleton
class DriveBackupManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveService: DriveService,
    private val encryptionManager: EncryptionManager
) : DriveBackupManager {

    private val credentialManager = CredentialManager.create(context)
    private val googleUserIdKey = stringPreferencesKey("google_user_id")
    private val accountNameKey = stringPreferencesKey("account_name")
    private val lastBackupTimeKey = longPreferencesKey("last_backup_time")

    override val lastBackupTime: Flow<Long?> = context.dataStore.data.map { it[lastBackupTimeKey] }
    override val accountName: Flow<String?> = context.dataStore.data.map { it[accountNameKey] }
    override val googleUserId: Flow<String?> = context.dataStore.data.map { it[googleUserIdKey] }

    override suspend fun authenticate(activity: Activity): Boolean {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Allow new sign-ins
            .setServerClientId("911917456135-8tg9ij6vech1tjaivrbpijpscjbaqajs.apps.googleusercontent.com") // IMPORTANT: Replace with your Google Cloud Server Client ID
            .setAutoSelectEnabled(false) // Disable auto-select for testing
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            handleCredentialResponse(result)
        } catch (e: Exception) {
            e.printStackTrace()
            // Log full exception to see why account name might be empty
            android.util.Log.e("DriveBackup", "Authentication failed", e)
            false
        }
    }

    private suspend fun handleCredentialResponse(response: GetCredentialResponse): Boolean {
        val credential = response.credential
        if (credential is GoogleIdTokenCredential) {
            // .id is the user's email address if the idToken was requested with the email scope
            val email = credential.id
            val displayName = credential.displayName
            
            android.util.Log.d("DriveBackup", "Authenticated: Email='$email', Name='$displayName'")
            
            val accountName = if (!email.isNullOrBlank() && email.contains("@")) {
                email
            } else {
                android.util.Log.w("DriveBackup", "Credential ID is not an email: '$email'. Falling back to displayName.")
                displayName
            }

            if (accountName.isNullOrBlank()) {
                android.util.Log.e("DriveBackup", "Google credential returned no usable identifier (email or displayName)")
                return false
            }

            // Persist for background sync
            context.dataStore.edit { prefs ->
                prefs[googleUserIdKey] = email ?: ""
                prefs[accountNameKey] = accountName
            }
            
            android.util.Log.d("DriveBackup", "Saved to DataStore, initializing service with '$accountName'...")
            driveService.initialize(accountName)
            return true
        }
        android.util.Log.e("DriveBackup", "Received unsupported credential type: ${credential::class.java.simpleName}")
        return false
    }

    override fun scheduleBackup(databaseFile: File) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        kotlinx.coroutines.MainScope().launch {
            val prefs = context.dataStore.data.first()
            val googleUserId = prefs[googleUserIdKey]
            val accountName = prefs[accountNameKey]

            if (googleUserId.isNullOrBlank() || accountName.isNullOrBlank()) return@launch

            val inputData = Data.Builder()
                .putString(BackupWorker.KEY_DATABASE_PATH, databaseFile.absolutePath)
                .putString(BackupWorker.KEY_GOOGLE_USER_ID, googleUserId)
                .putString(BackupWorker.KEY_ACCOUNT_NAME, accountName)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<BackupWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "drive_backup",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    override suspend fun performManualBackup(databaseFile: File): Result<Unit> {
        val prefs = context.dataStore.data.first()
        val googleUserId = prefs[googleUserIdKey]
        val accountName = prefs[accountNameKey]
            
        android.util.Log.d("DriveBackup", "performManualBackup: START. Account='$accountName', DB exists=${databaseFile.exists()}")

        if (googleUserId.isNullOrBlank() || accountName.isNullOrBlank()) {
            val error = "Not authenticated properly. Account name is missing."
            android.util.Log.e("DriveBackup", "performManualBackup: $error")
            return Result.failure(Exception(error))
        }

        if (!databaseFile.exists()) {
            android.util.Log.e("DriveBackup", "performManualBackup: Database file not found at ${databaseFile.absolutePath}")
            return Result.failure(Exception("Database file not found"))
        }

        val encryptedFile = File(context.cacheDir, "manual_backup.enc")
        
        return try {
            android.util.Log.d("DriveBackup", "performManualBackup: Initializing DriveService for $accountName")
            driveService.initialize(accountName)
            
            android.util.Log.d("DriveBackup", "performManualBackup: Encrypting database...")
            encryptionManager.encrypt(googleUserId, databaseFile, encryptedFile)
            
            android.util.Log.d("DriveBackup", "performManualBackup: Uploading encrypted file...")
            driveService.uploadBackup(encryptedFile)
            
            android.util.Log.d("DriveBackup", "performManualBackup: SUCCESS. Updating last backup time.")
            context.dataStore.edit { it[lastBackupTimeKey] = System.currentTimeMillis() }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "performManualBackup: FAILED", e)
            Result.failure(e)
        } finally {
            if (encryptedFile.exists()) {
                val deleted = encryptedFile.delete()
                android.util.Log.d("DriveBackup", "performManualBackup: Cleaned up temporary file: $deleted")
            }
        }
    }

    override suspend fun uploadRawFile(file: File): Result<Unit> {
        val prefs = context.dataStore.data.first()
        val accountName = prefs[accountNameKey]

        if (accountName.isNullOrBlank()) {
            return Result.failure(Exception("Not authenticated"))
        }

        return try {
            driveService.initialize(accountName)
            driveService.uploadBackup(file)
            context.dataStore.edit { it[lastBackupTimeKey] = System.currentTimeMillis() }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadRawFile(outputFile: File): Result<Boolean> {
        val prefs = context.dataStore.data.first()
        val accountName = prefs[accountNameKey]

        if (accountName.isNullOrBlank()) {
            return Result.failure(Exception("Not authenticated"))
        }

        return try {
            driveService.initialize(accountName)
            val success = driveService.downloadBackup(outputFile)
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restore(): Result<File?> {
        val prefs = context.dataStore.data.first()
        val googleUserId = prefs[googleUserIdKey]
        val accountName = prefs[accountNameKey]

        android.util.Log.d("DriveBackup", "restore: START. Account='$accountName'")

        if (googleUserId.isNullOrBlank() || accountName.isNullOrBlank()) {
            val error = "Authentication data missing. Please sign in with Google again."
            android.util.Log.e("DriveBackup", "restore: $error")
            return Result.failure(Exception(error))
        }

        return try {
            android.util.Log.d("DriveBackup", "restore: Checking backup availability...")
            if (!isBackupAvailable()) {
                android.util.Log.d("DriveBackup", "restore: No backup available on Drive.")
                return Result.success(null)
            }
            
            val encryptedFile = File(context.cacheDir, "temp_restore.enc")
            val decryptedFile = File(context.cacheDir, "restored_database.db")
            
            android.util.Log.d("DriveBackup", "restore: Downloading backup...")
            val success = driveService.downloadBackup(encryptedFile)
            if (!success) {
                android.util.Log.e("DriveBackup", "restore: Download failed.")
                return Result.success(null)
            }

            android.util.Log.d("DriveBackup", "restore: Decrypting database...")
            encryptionManager.decrypt(googleUserId, encryptedFile, decryptedFile)
            
            android.util.Log.d("DriveBackup", "restore: SUCCESS.")
            Result.success(decryptedFile)
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "restore: FAILED", e)
            Result.failure(e)
        } finally {
            // temp_restore.enc should be cleaned up, but decryptedFile MUST stay for the caller
            File(context.cacheDir, "temp_restore.enc").delete()
        }
    }

    override suspend fun isBackupAvailable(): Boolean {
        val accountName = context.dataStore.data.map { it[accountNameKey] }.first()
        if (accountName.isNullOrBlank()) return false
        
        return try {
            driveService.initialize(accountName)
            val metadata = driveService.getBackupFileMetadata()
            if (metadata != null) {
                val (_, modifiedTime) = metadata
                val currentLocalTime = context.dataStore.data.map { it[lastBackupTimeKey] }.first()
                if (currentLocalTime == null || currentLocalTime == 0L) {
                    context.dataStore.edit { it[lastBackupTimeKey] = modifiedTime }
                }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
