package com.monetra.data.sync

import android.content.Context
import com.monetra.data.backup.model.BackupData
import com.monetra.drivebackup.api.DriveBackupManager
import com.monetra.drivebackup.internal.security.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveSyncDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveManager: DriveBackupManager,
    private val encryptionManager: EncryptionManager
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    suspend fun fetchRemoteData(): Result<BackupData?> = withContext(Dispatchers.IO) {
        val googleUserId = driveManager.googleUserId.first() 
            ?: return@withContext Result.failure(Exception("No Google account"))
        
        val tempFile = File(context.cacheDir, "remote_sync.enc")
        
        val downloadResult = driveManager.downloadRawFile(tempFile)
        if (downloadResult.isFailure) {
            return@withContext Result.failure(downloadResult.exceptionOrNull() ?: Exception("Download failed"))
        }

        val exists = downloadResult.getOrDefault(false)
        if (!exists) return@withContext Result.success(null)
        
        return@withContext try {
            val encryptedBytes = tempFile.readBytes()
            val decryptedJson = encryptionManager.decrypt(googleUserId, encryptedBytes).decodeToString()
            Result.success(json.decodeFromString<BackupData>(decryptedJson))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tempFile.delete()
        }
    }

    suspend fun commitRemoteData(data: BackupData): Result<Unit> = withContext(Dispatchers.IO) {
        val googleUserId = driveManager.googleUserId.first() 
            ?: return@withContext Result.failure(Exception("No Google account"))
            
        return@withContext try {
            val jsonString = json.encodeToString(data)
            val encryptedBytes = encryptionManager.encrypt(googleUserId, jsonString.toByteArray())
            
            val tempFile = File(context.cacheDir, "upload_sync.enc")
            tempFile.writeBytes(encryptedBytes)
            
            val result = driveManager.uploadRawFile(tempFile)
            tempFile.delete()
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
