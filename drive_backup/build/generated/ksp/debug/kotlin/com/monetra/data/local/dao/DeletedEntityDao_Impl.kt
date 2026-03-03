package com.monetra.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.entity.DeletedEntity
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

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DeletedEntityDao_Impl(
  __db: RoomDatabase,
) : DeletedEntityDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDeletedEntity: EntityInsertAdapter<DeletedEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDeletedEntity = object : EntityInsertAdapter<DeletedEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `deleted_entities` (`remoteId`,`entityType`,`deletedAt`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DeletedEntity) {
        statement.bindText(1, entity.remoteId)
        statement.bindText(2, entity.entityType)
        statement.bindLong(3, entity.deletedAt)
      }
    }
  }

  public override suspend fun insert(deletedEntity: DeletedEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDeletedEntity.insert(_connection, deletedEntity)
  }

  public override suspend fun getAll(): List<DeletedEntity> {
    val _sql: String = "SELECT * FROM deleted_entities"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfDeletedAt: Int = getColumnIndexOrThrow(_stmt, "deletedAt")
        val _result: MutableList<DeletedEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DeletedEntity
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpDeletedAt: Long
          _tmpDeletedAt = _stmt.getLong(_columnIndexOfDeletedAt)
          _item = DeletedEntity(_tmpRemoteId,_tmpEntityType,_tmpDeletedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByRemoteIds(remoteIds: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM deleted_entities WHERE remoteId IN (")
    val _inputSize: Int = remoteIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in remoteIds) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM deleted_entities"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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
