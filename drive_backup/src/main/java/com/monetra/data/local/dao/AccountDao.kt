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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("UPDATE accounts SET name = :newName WHERE name = :oldName")
    suspend fun updateAccount(oldName: String, newName: String)

    @Query("DELETE FROM accounts WHERE name = :name")
    suspend fun deleteAccount(name: String)
}
