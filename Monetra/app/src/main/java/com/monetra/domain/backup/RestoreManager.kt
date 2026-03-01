package com.monetra.domain.backup

import android.net.Uri

interface RestoreManager {
    suspend fun restoreFromEncryptedUri(uri: Uri, password: String): Result<Unit>
}
