package com.monetra.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.Converters
import com.monetra.`data`.local.entity.GoalEntity
import com.monetra.domain.model.GoalCategory
import java.time.LocalDate
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
public class GoalDao_Impl(
  __db: RoomDatabase,
) : GoalDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfGoalEntity: EntityInsertAdapter<GoalEntity>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfGoalEntity = object : EntityInsertAdapter<GoalEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `goals` (`id`,`remoteId`,`title`,`targetAmount`,`currentAmount`,`deadline`,`category`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GoalEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.title)
        statement.bindDouble(4, entity.targetAmount)
        statement.bindDouble(5, entity.currentAmount)
        val _tmpDeadline: LocalDate? = entity.deadline
        val _tmp: String? = __converters.dateToTimestamp(_tmpDeadline)
        if (_tmp == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmp)
        }
        val _tmp_1: String = __converters.fromGoalCategory(entity.category)
        statement.bindText(7, _tmp_1)
        statement.bindLong(8, entity.version)
        statement.bindLong(9, entity.updatedAt)
        statement.bindText(10, entity.deviceId)
        val _tmp_2: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(11, _tmp_2.toLong())
      }
    }
  }

  public override suspend fun upsertGoal(goal: GoalEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfGoalEntity.insert(_connection, goal)
  }

  public override suspend fun insertAllGoals(goals: List<GoalEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfGoalEntity.insert(_connection, goals)
  }

  public override fun getGoals(): Flow<List<GoalEntity>> {
    val _sql: String = "SELECT * FROM goals"
    return createFlow(__db, false, arrayOf("goals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfTargetAmount: Int = getColumnIndexOrThrow(_stmt, "targetAmount")
        val _columnIndexOfCurrentAmount: Int = getColumnIndexOrThrow(_stmt, "currentAmount")
        val _columnIndexOfDeadline: Int = getColumnIndexOrThrow(_stmt, "deadline")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpTargetAmount: Double
          _tmpTargetAmount = _stmt.getDouble(_columnIndexOfTargetAmount)
          val _tmpCurrentAmount: Double
          _tmpCurrentAmount = _stmt.getDouble(_columnIndexOfCurrentAmount)
          val _tmpDeadline: LocalDate?
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfDeadline)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfDeadline)
          }
          _tmpDeadline = __converters.fromTimestamp(_tmp)
          val _tmpCategory: GoalCategory
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCategory)
          _tmpCategory = __converters.toGoalCategory(_tmp_1)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          _item = GoalEntity(_tmpId,_tmpRemoteId,_tmpTitle,_tmpTargetAmount,_tmpCurrentAmount,_tmpDeadline,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getGoalById(id: Long): GoalEntity? {
    val _sql: String = "SELECT * FROM goals WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfTargetAmount: Int = getColumnIndexOrThrow(_stmt, "targetAmount")
        val _columnIndexOfCurrentAmount: Int = getColumnIndexOrThrow(_stmt, "currentAmount")
        val _columnIndexOfDeadline: Int = getColumnIndexOrThrow(_stmt, "deadline")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: GoalEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpTargetAmount: Double
          _tmpTargetAmount = _stmt.getDouble(_columnIndexOfTargetAmount)
          val _tmpCurrentAmount: Double
          _tmpCurrentAmount = _stmt.getDouble(_columnIndexOfCurrentAmount)
          val _tmpDeadline: LocalDate?
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfDeadline)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfDeadline)
          }
          _tmpDeadline = __converters.fromTimestamp(_tmp)
          val _tmpCategory: GoalCategory
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCategory)
          _tmpCategory = __converters.toGoalCategory(_tmp_1)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          _result = GoalEntity(_tmpId,_tmpRemoteId,_tmpTitle,_tmpTargetAmount,_tmpCurrentAmount,_tmpDeadline,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedGoals(): List<GoalEntity> {
    val _sql: String = "SELECT * FROM goals WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfTargetAmount: Int = getColumnIndexOrThrow(_stmt, "targetAmount")
        val _columnIndexOfCurrentAmount: Int = getColumnIndexOrThrow(_stmt, "currentAmount")
        val _columnIndexOfDeadline: Int = getColumnIndexOrThrow(_stmt, "deadline")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpTargetAmount: Double
          _tmpTargetAmount = _stmt.getDouble(_columnIndexOfTargetAmount)
          val _tmpCurrentAmount: Double
          _tmpCurrentAmount = _stmt.getDouble(_columnIndexOfCurrentAmount)
          val _tmpDeadline: LocalDate?
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfDeadline)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfDeadline)
          }
          _tmpDeadline = __converters.fromTimestamp(_tmp)
          val _tmpCategory: GoalCategory
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCategory)
          _tmpCategory = __converters.toGoalCategory(_tmp_1)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          _item = GoalEntity(_tmpId,_tmpRemoteId,_tmpTitle,_tmpTargetAmount,_tmpCurrentAmount,_tmpDeadline,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getGoalByRemoteId(remoteId: String): GoalEntity? {
    val _sql: String = "SELECT * FROM goals WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfTargetAmount: Int = getColumnIndexOrThrow(_stmt, "targetAmount")
        val _columnIndexOfCurrentAmount: Int = getColumnIndexOrThrow(_stmt, "currentAmount")
        val _columnIndexOfDeadline: Int = getColumnIndexOrThrow(_stmt, "deadline")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: GoalEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpTargetAmount: Double
          _tmpTargetAmount = _stmt.getDouble(_columnIndexOfTargetAmount)
          val _tmpCurrentAmount: Double
          _tmpCurrentAmount = _stmt.getDouble(_columnIndexOfCurrentAmount)
          val _tmpDeadline: LocalDate?
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfDeadline)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfDeadline)
          }
          _tmpDeadline = __converters.fromTimestamp(_tmp)
          val _tmpCategory: GoalCategory
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCategory)
          _tmpCategory = __converters.toGoalCategory(_tmp_1)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          _result = GoalEntity(_tmpId,_tmpRemoteId,_tmpTitle,_tmpTargetAmount,_tmpCurrentAmount,_tmpDeadline,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllGoals(): List<GoalEntity> {
    val _sql: String = "SELECT * FROM goals"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfTargetAmount: Int = getColumnIndexOrThrow(_stmt, "targetAmount")
        val _columnIndexOfCurrentAmount: Int = getColumnIndexOrThrow(_stmt, "currentAmount")
        val _columnIndexOfDeadline: Int = getColumnIndexOrThrow(_stmt, "deadline")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpTargetAmount: Double
          _tmpTargetAmount = _stmt.getDouble(_columnIndexOfTargetAmount)
          val _tmpCurrentAmount: Double
          _tmpCurrentAmount = _stmt.getDouble(_columnIndexOfCurrentAmount)
          val _tmpDeadline: LocalDate?
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfDeadline)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfDeadline)
          }
          _tmpDeadline = __converters.fromTimestamp(_tmp)
          val _tmpCategory: GoalCategory
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCategory)
          _tmpCategory = __converters.toGoalCategory(_tmp_1)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_2 != 0
          _item = GoalEntity(_tmpId,_tmpRemoteId,_tmpTitle,_tmpTargetAmount,_tmpCurrentAmount,_tmpDeadline,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteGoal(id: Long) {
    val _sql: String = "DELETE FROM goals WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsSynced(remoteIds: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE goals SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun deleteAllGoals() {
    val _sql: String = "DELETE FROM goals"
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
