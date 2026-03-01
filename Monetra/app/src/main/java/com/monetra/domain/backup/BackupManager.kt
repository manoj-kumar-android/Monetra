package com.monetra.domain.backup

import android.net.Uri

interface BackupManager {
    suspend fun exportEncryptedBackup(uri: Uri, password: String): Result<Unit>
}
