package com.monetra.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.entity.SavingEntity
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
public class SavingDao_Impl(
  __db: RoomDatabase,
) : SavingDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSavingEntity: EntityInsertAdapter<SavingEntity>

  private val __deleteAdapterOfSavingEntity: EntityDeleteOrUpdateAdapter<SavingEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSavingEntity = object : EntityInsertAdapter<SavingEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `savings` (`id`,`remoteId`,`bankName`,`amount`,`interestRate`,`note`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SavingEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.bankName)
        statement.bindDouble(4, entity.amount)
        val _tmpInterestRate: Double? = entity.interestRate
        if (_tmpInterestRate == null) {
          statement.bindNull(5)
        } else {
          statement.bindDouble(5, _tmpInterestRate)
        }
        statement.bindText(6, entity.note)
        statement.bindLong(7, entity.version)
        statement.bindLong(8, entity.updatedAt)
        statement.bindText(9, entity.deviceId)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(10, _tmp.toLong())
      }
    }
    this.__deleteAdapterOfSavingEntity = object : EntityDeleteOrUpdateAdapter<SavingEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `savings` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SavingEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertSaving(saving: SavingEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSavingEntity.insert(_connection, saving)
  }

  public override suspend fun insertAllSavings(savings: List<SavingEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSavingEntity.insert(_connection, savings)
  }

  public override suspend fun deleteSaving(saving: SavingEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfSavingEntity.handle(_connection, saving)
  }

  public override fun getAllSaving(): Flow<List<SavingEntity>> {
    val _sql: String = "SELECT * FROM savings"
    return createFlow(__db, false, arrayOf("savings")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBankName: Int = getColumnIndexOrThrow(_stmt, "bankName")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<SavingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavingEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBankName: String
          _tmpBankName = _stmt.getText(_columnIndexOfBankName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpInterestRate: Double?
          if (_stmt.isNull(_columnIndexOfInterestRate)) {
            _tmpInterestRate = null
          } else {
            _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _item = SavingEntity(_tmpId,_tmpRemoteId,_tmpBankName,_tmpAmount,_tmpInterestRate,_tmpNote,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalSavingAmount(): Flow<Double?> {
    val _sql: String = "SELECT SUM(amount) FROM savings"
    return createFlow(__db, false, arrayOf("savings")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSavingById(id: Long): SavingEntity? {
    val _sql: String = "SELECT * FROM savings WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBankName: Int = getColumnIndexOrThrow(_stmt, "bankName")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: SavingEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBankName: String
          _tmpBankName = _stmt.getText(_columnIndexOfBankName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpInterestRate: Double?
          if (_stmt.isNull(_columnIndexOfInterestRate)) {
            _tmpInterestRate = null
          } else {
            _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _result = SavingEntity(_tmpId,_tmpRemoteId,_tmpBankName,_tmpAmount,_tmpInterestRate,_tmpNote,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedSavings(): List<SavingEntity> {
    val _sql: String = "SELECT * FROM savings WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBankName: Int = getColumnIndexOrThrow(_stmt, "bankName")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<SavingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavingEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBankName: String
          _tmpBankName = _stmt.getText(_columnIndexOfBankName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpInterestRate: Double?
          if (_stmt.isNull(_columnIndexOfInterestRate)) {
            _tmpInterestRate = null
          } else {
            _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _item = SavingEntity(_tmpId,_tmpRemoteId,_tmpBankName,_tmpAmount,_tmpInterestRate,_tmpNote,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSavingByRemoteId(remoteId: String): SavingEntity? {
    val _sql: String = "SELECT * FROM savings WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBankName: Int = getColumnIndexOrThrow(_stmt, "bankName")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: SavingEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBankName: String
          _tmpBankName = _stmt.getText(_columnIndexOfBankName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpInterestRate: Double?
          if (_stmt.isNull(_columnIndexOfInterestRate)) {
            _tmpInterestRate = null
          } else {
            _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _result = SavingEntity(_tmpId,_tmpRemoteId,_tmpBankName,_tmpAmount,_tmpInterestRate,_tmpNote,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllSavingsList(): List<SavingEntity> {
    val _sql: String = "SELECT * FROM savings"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBankName: Int = getColumnIndexOrThrow(_stmt, "bankName")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<SavingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavingEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBankName: String
          _tmpBankName = _stmt.getText(_columnIndexOfBankName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpInterestRate: Double?
          if (_stmt.isNull(_columnIndexOfInterestRate)) {
            _tmpInterestRate = null
          } else {
            _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
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
          _item = SavingEntity(_tmpId,_tmpRemoteId,_tmpBankName,_tmpAmount,_tmpInterestRate,_tmpNote,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsSynced(remoteIds: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE savings SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun deleteAllSavings() {
    val _sql: String = "DELETE FROM savings"
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
