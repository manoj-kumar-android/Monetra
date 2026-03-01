package com.monetra.drivebackup.api

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Public interface for managing Google Drive backups.
 * Fully decoupled from implementation details.
 */
interface DriveBackupManager {

    /**
     * A flow of the authenticated Google account name.
     * Returns null if the user is not authenticated.
     */
    val accountName: Flow<String?>

    /**
     * A flow of the last successful backup timestamp (epoch milliseconds).
     * Returns null if no backup has been performed yet.
     */
    val lastBackupTime: Flow<Long?>

    /**
     * Authenticate the user with Google using Credential Manager.
     * @param activity The host activity for the credential request.
     * @return True if authentication was successful, false otherwise.
     */
    suspend fun authenticate(activity: Activity): Boolean

    /**
     * Schedule an automatic background sync of the specified database file.
     * @param databaseFile The database file to back up.
     */
    fun scheduleBackup(databaseFile: File)

    /**
     * Performs an immediate, manual backup of the specified database file.
     * This is a suspend function that waits for the backup to complete.
     * @param databaseFile The database file to back up.
     */
    suspend fun performManualBackup(databaseFile: File): Result<Unit>

    /**
     * Attempts to restore the database from Google Drive.
     * @return Result containing the restored File if successful, or null if no backup exists.
     */
    suspend fun restore(): Result<File?>

    /**
     * Checks if a backup exists in the user's Google Drive AppData folder.
     */
    suspend fun isBackupAvailable(): Boolean
}
