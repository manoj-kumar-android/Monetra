package com.monetra.drivebackup.internal.drive

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DriveService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val transport = NetHttpTransport()
    private val appDataFolder = "appDataFolder"
    private val backupFileName = "monetra_backup.enc"

    private var currentAccount: String? = null
    private var driveService: Drive? = null

    /**
     * Initializes the Drive service with the provided Google account name.
     */
    @Synchronized
    fun initialize(accountName: String?) {
        val sanitizedAccount = accountName?.trim()
        
        android.util.Log.d("DriveBackup", "DriveService.initialize called for account: '$sanitizedAccount'")

        if (sanitizedAccount.isNullOrBlank()) {
            android.util.Log.e("DriveBackup", "DriveService.initialize: CRITICAL - accountName is null or blank.")
            driveService = null
            currentAccount = null
            return
        }

        try {
            // Force recreation of the service and credential to avoid stale state/null account issues
            android.util.Log.d("DriveBackup", "Re-creating Drive service and credential for $sanitizedAccount")
            
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_APPDATA)
            )
            
            // Try to set using Account object which is sometimes more robust than String
            try {
                val account = android.accounts.Account(sanitizedAccount, "com.google")
                credential.selectedAccount = account
                android.util.Log.d("DriveBackup", "Set account via Account object for $sanitizedAccount")
            } catch (authEx: Exception) {
                android.util.Log.w("DriveBackup", "Failed to set via Account object, falling back to String: ${authEx.message}")
                credential.selectedAccountName = sanitizedAccount
            }
            
            android.util.Log.d("DriveBackup", "Credential selectedAccountName is now: ${credential.selectedAccountName}")

            if (credential.selectedAccountName == null) {
                android.util.Log.e("DriveBackup", "CRITICAL: selectedAccountName is STILL NULL after setting!")
                // Log all available accounts to debug
                val accounts = android.accounts.AccountManager.get(context).accounts
                android.util.Log.d("DriveBackup", "Available system accounts: ${accounts.map { "${it.name} (${it.type})" }}")
            }

            driveService = Drive.Builder(
                transport, jsonFactory, credential
            ).setApplicationName("Monetra").build()

            currentAccount = sanitizedAccount
            android.util.Log.d("DriveBackup", "DriveService successfully re-initialized.")
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "FAILED to initialize DriveService for '$sanitizedAccount'", e)
            driveService = null
            currentAccount = null
            throw e
        }
    }

    /**
     * Checks if a backup file exists in the AppData folder.
     */
    suspend fun getBackupFileId(): String? = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext null
        val result = service.files().list().setSpaces("appDataFolder")
            .setQ("name='$backupFileName' and 'appDataFolder' in parents and trashed=false")
            .setFields("files(id, name)").execute()

        result.files?.firstOrNull()?.id
    }

    /**
     * Uploads or updates the backup file.
     */
    suspend fun uploadBackup(inputFile: java.io.File) = withContext(Dispatchers.IO) {
        val service = driveService ?: throw IllegalStateException("Drive service not initialized")

        try {
            val fileId = getBackupFileId()
            val content = FileContent("application/octet-stream", inputFile)

            if (fileId != null) {
                android.util.Log.d("DriveBackup", "Updating existing backup file ID: $fileId")
                service.files().update(fileId, null, content).execute()
            } else {
                android.util.Log.d("DriveBackup", "Creating new backup file in AppData folder")
                val metadata = File().apply {
                    name = backupFileName
                    parents = listOf("appDataFolder")
                }
                service.files().create(metadata, content).execute()
            }
            android.util.Log.d("DriveBackup", "Backup upload completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "Upload failed", e)
            throw e
        }
    }

    /**
     * Downloads the backup file.
     */
    suspend fun downloadBackup(outputFile: java.io.File): Boolean = withContext(Dispatchers.IO) {
        val service = driveService ?: return@withContext false
        val fileId = getBackupFileId() ?: return@withContext false

        FileOutputStream(outputFile).use { output ->
            service.files().get(fileId).executeMediaAndDownloadTo(output)
        }
        true
    }
}
