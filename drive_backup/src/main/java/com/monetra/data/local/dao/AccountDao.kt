package com.monetra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monetra.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsList(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE remoteId = :remoteId")
    suspend fun getAccountByRemoteId(remoteId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE name = :name")
    suspend fun getAccountByName(name: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(account: AccountEntity)

    suspend fun upsertSync(account: AccountEntity) {
        val existing = getAccountByRemoteId(account.remoteId)
        val shouldOverwrite = when {
            existing == null -> true
            account.version > existing.version -> true
            account.version < existing.version -> false
            account.updatedAt > existing.updatedAt -> true
            account.updatedAt < existing.updatedAt -> false
            else -> account.deviceId > existing.deviceId
        }

        if (shouldOverwrite) {
            val id = existing?.id ?: 0
            insertAllAccounts(listOf(account.copy(id = id, isSynced = true)))
        }
    }

    @Query("SELECT * FROM accounts WHERE isSynced = 0")
    suspend fun getUnsyncedAccounts(): List<AccountEntity>

    @Query("UPDATE accounts SET isSynced = 1 WHERE remoteId IN (:remoteIds)")
    suspend fun markAsSynced(remoteIds: List<String>)

    @Query("UPDATE accounts SET name = :newName WHERE name = :oldName")
    suspend fun updateAccount(oldName: String, newName: String)

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAccounts(accounts: List<AccountEntity>)

    @Query("DELETE FROM accounts WHERE name = :name")
    suspend fun deleteAccount(name: String)

    @Query("DELETE FROM accounts WHERE remoteId = :remoteId")
    suspend fun deleteAccountByRemoteId(remoteId: String)
}
