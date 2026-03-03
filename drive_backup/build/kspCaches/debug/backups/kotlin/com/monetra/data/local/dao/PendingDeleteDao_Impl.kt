package com.monetra.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.entity.PendingDeleteEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PendingDeleteDao_Impl(
  __db: RoomDatabase,
) : PendingDeleteDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPendingDeleteEntity: EntityInsertAdapter<PendingDeleteEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPendingDeleteEntity = object : EntityInsertAdapter<PendingDeleteEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `pending_deletes` (`id`,`entityId`,`remoteId`,`entityType`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PendingDeleteEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.entityId)
        statement.bindText(3, entity.remoteId)
        statement.bindText(4, entity.entityType)
        statement.bindLong(5, entity.createdAt)
      }
    }
  }

  public override suspend fun insert(entity: PendingDeleteEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPendingDeleteEntity.insert(_connection, entity)
  }

  public override suspend fun getStaleDeletes(cutoff: Long): List<PendingDeleteEntity> {
    val _sql: String = "SELECT * FROM pending_deletes WHERE createdAt < ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, cutoff)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEntityId: Int = getColumnIndexOrThrow(_stmt, "entityId")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<PendingDeleteEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PendingDeleteEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpEntityId: Long
          _tmpEntityId = _stmt.getLong(_columnIndexOfEntityId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = PendingDeleteEntity(_tmpId,_tmpEntityId,_tmpRemoteId,_tmpEntityType,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAll(): Flow<List<PendingDeleteEntity>> {
    val _sql: String = "SELECT * FROM pending_deletes"
    return createFlow(__db, false, arrayOf("pending_deletes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEntityId: Int = getColumnIndexOrThrow(_stmt, "entityId")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<PendingDeleteEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PendingDeleteEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpEntityId: Long
          _tmpEntityId = _stmt.getLong(_columnIndexOfEntityId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = PendingDeleteEntity(_tmpId,_tmpEntityId,_tmpRemoteId,_tmpEntityType,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPendingIdsForType(entityType: String): Flow<List<Long>> {
    val _sql: String = "SELECT entityId FROM pending_deletes WHERE entityType = ?"
    return createFlow(__db, false, arrayOf("pending_deletes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, entityType)
        val _result: MutableList<Long> = mutableListOf()
        while (_stmt.step()) {
          val _item: Long
          _item = _stmt.getLong(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingEntry(entityId: Long, entityType: String): PendingDeleteEntity? {
    val _sql: String = "SELECT * FROM pending_deletes WHERE entityId = ? AND entityType = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, entityId)
        _argIndex = 2
        _stmt.bindText(_argIndex, entityType)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEntityId: Int = getColumnIndexOrThrow(_stmt, "entityId")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: PendingDeleteEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpEntityId: Long
          _tmpEntityId = _stmt.getLong(_columnIndexOfEntityId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = PendingDeleteEntity(_tmpId,_tmpEntityId,_tmpRemoteId,_tmpEntityType,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun cancelDelete(entityId: Long, entityType: String) {
    val _sql: String = "DELETE FROM pending_deletes WHERE entityId = ? AND entityType = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, entityId)
        _argIndex = 2
        _stmt.bindText(_argIndex, entityType)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByIds(ids: List<Long>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM pending_deletes WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Long in ids) {
          _stmt.bindLong(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
