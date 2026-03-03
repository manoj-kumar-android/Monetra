package com.monetra.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.entity.CategoryBudgetEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
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
public class CategoryBudgetDao_Impl(
  __db: RoomDatabase,
) : CategoryBudgetDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCategoryBudgetEntity: EntityInsertAdapter<CategoryBudgetEntity>

  private val __upsertAdapterOfCategoryBudgetEntity: EntityUpsertAdapter<CategoryBudgetEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCategoryBudgetEntity = object : EntityInsertAdapter<CategoryBudgetEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `category_budgets` (`categoryName`,`remoteId`,`limit`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryBudgetEntity) {
        statement.bindText(1, entity.categoryName)
        statement.bindText(2, entity.remoteId)
        statement.bindDouble(3, entity.limit)
        statement.bindLong(4, entity.version)
        statement.bindLong(5, entity.updatedAt)
        statement.bindText(6, entity.deviceId)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(7, _tmp.toLong())
      }
    }
    this.__upsertAdapterOfCategoryBudgetEntity = EntityUpsertAdapter<CategoryBudgetEntity>(object : EntityInsertAdapter<CategoryBudgetEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `category_budgets` (`categoryName`,`remoteId`,`limit`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryBudgetEntity) {
        statement.bindText(1, entity.categoryName)
        statement.bindText(2, entity.remoteId)
        statement.bindDouble(3, entity.limit)
        statement.bindLong(4, entity.version)
        statement.bindLong(5, entity.updatedAt)
        statement.bindText(6, entity.deviceId)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(7, _tmp.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<CategoryBudgetEntity>() {
      protected override fun createQuery(): String = "UPDATE `category_budgets` SET `categoryName` = ?,`remoteId` = ?,`limit` = ?,`version` = ?,`updatedAt` = ?,`deviceId` = ?,`isSynced` = ? WHERE `categoryName` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryBudgetEntity) {
        statement.bindText(1, entity.categoryName)
        statement.bindText(2, entity.remoteId)
        statement.bindDouble(3, entity.limit)
        statement.bindLong(4, entity.version)
        statement.bindLong(5, entity.updatedAt)
        statement.bindText(6, entity.deviceId)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindText(8, entity.categoryName)
      }
    })
  }

  public override suspend fun insertAllCategoryBudgets(budgets: List<CategoryBudgetEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCategoryBudgetEntity.insert(_connection, budgets)
  }

  public override suspend fun upsertBudget(budget: CategoryBudgetEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfCategoryBudgetEntity.upsert(_connection, budget)
  }

  public override fun getAllBudgets(): Flow<List<CategoryBudgetEntity>> {
    val _sql: String = "SELECT * FROM category_budgets"
    return createFlow(__db, false, arrayOf("category_budgets")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfLimit: Int = getColumnIndexOrThrow(_stmt, "limit")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<CategoryBudgetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryBudgetEntity
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpLimit: Double
          _tmpLimit = _stmt.getDouble(_columnIndexOfLimit)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          _item = CategoryBudgetEntity(_tmpCategoryName,_tmpRemoteId,_tmpLimit,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBudgetByName(categoryName: String): CategoryBudgetEntity? {
    val _sql: String = "SELECT * FROM category_budgets WHERE categoryName = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, categoryName)
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfLimit: Int = getColumnIndexOrThrow(_stmt, "limit")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: CategoryBudgetEntity?
        if (_stmt.step()) {
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpLimit: Double
          _tmpLimit = _stmt.getDouble(_columnIndexOfLimit)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          _result = CategoryBudgetEntity(_tmpCategoryName,_tmpRemoteId,_tmpLimit,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedBudgets(): List<CategoryBudgetEntity> {
    val _sql: String = "SELECT * FROM category_budgets WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfLimit: Int = getColumnIndexOrThrow(_stmt, "limit")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<CategoryBudgetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryBudgetEntity
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpLimit: Double
          _tmpLimit = _stmt.getDouble(_columnIndexOfLimit)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          _item = CategoryBudgetEntity(_tmpCategoryName,_tmpRemoteId,_tmpLimit,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getBudgetByRemoteId(remoteId: String): CategoryBudgetEntity? {
    val _sql: String = "SELECT * FROM category_budgets WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfLimit: Int = getColumnIndexOrThrow(_stmt, "limit")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: CategoryBudgetEntity?
        if (_stmt.step()) {
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpLimit: Double
          _tmpLimit = _stmt.getDouble(_columnIndexOfLimit)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          _result = CategoryBudgetEntity(_tmpCategoryName,_tmpRemoteId,_tmpLimit,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllCategoryBudgets(): List<CategoryBudgetEntity> {
    val _sql: String = "SELECT * FROM category_budgets"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfCategoryName: Int = getColumnIndexOrThrow(_stmt, "categoryName")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfLimit: Int = getColumnIndexOrThrow(_stmt, "limit")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<CategoryBudgetEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryBudgetEntity
          val _tmpCategoryName: String
          _tmpCategoryName = _stmt.getText(_columnIndexOfCategoryName)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpLimit: Double
          _tmpLimit = _stmt.getDouble(_columnIndexOfLimit)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          _item = CategoryBudgetEntity(_tmpCategoryName,_tmpRemoteId,_tmpLimit,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBudget(categoryName: String) {
    val _sql: String = "DELETE FROM category_budgets WHERE categoryName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, categoryName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsSynced(remoteIds: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE category_budgets SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun deleteAllCategoryBudgets() {
    val _sql: String = "DELETE FROM category_budgets"
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
