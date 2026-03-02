package com.monetra.data.local.dao

import androidx.room.*
import com.monetra.data.local.entity.DeletedEntity

@Dao
interface DeletedEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deletedEntity: DeletedEntity)

    @Query("SELECT * FROM deleted_entities")
    suspend fun getAll(): List<DeletedEntity>

    @Query("DELETE FROM deleted_entities WHERE remoteId IN (:remoteIds)")
    suspend fun deleteByRemoteIds(remoteIds: List<String>)

    @Query("DELETE FROM deleted_entities")
    suspend fun deleteAll()
}
