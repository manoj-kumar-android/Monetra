package com.monetra.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.Converters
import com.monetra.`data`.local.entity.InvestmentEntity
import com.monetra.domain.model.ContributionFrequency
import com.monetra.domain.model.InvestmentType
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
public class InvestmentDao_Impl(
  __db: RoomDatabase,
) : InvestmentDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfInvestmentEntity: EntityInsertAdapter<InvestmentEntity>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfInvestmentEntity = object : EntityInsertAdapter<InvestmentEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `investments` (`id`,`remoteId`,`name`,`type`,`startDate`,`endDate`,`amount`,`monthlyAmount`,`interestRate`,`currentValue`,`frequency`,`stepChanges`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: InvestmentEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.name)
        val _tmp: String = __converters.fromInvestmentType(entity.type)
        statement.bindText(4, _tmp)
        val _tmp_1: String? = __converters.dateToTimestamp(entity.startDate)
        if (_tmp_1 == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmp_1)
        }
        val _tmpEndDate: LocalDate? = entity.endDate
        val _tmp_2: String? = __converters.dateToTimestamp(_tmpEndDate)
        if (_tmp_2 == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmp_2)
        }
        statement.bindDouble(7, entity.amount)
        statement.bindDouble(8, entity.monthlyAmount)
        statement.bindDouble(9, entity.interestRate)
        statement.bindDouble(10, entity.currentValue)
        val _tmp_3: String = __converters.fromContributionFrequency(entity.frequency)
        statement.bindText(11, _tmp_3)
        statement.bindText(12, entity.stepChanges)
        statement.bindLong(13, entity.version)
        statement.bindLong(14, entity.updatedAt)
        statement.bindText(15, entity.deviceId)
        val _tmp_4: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(16, _tmp_4.toLong())
      }
    }
  }

  public override suspend fun upsertInvestment(investment: InvestmentEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfInvestmentEntity.insert(_connection, investment)
  }

  public override suspend fun insertAllInvestments(investments: List<InvestmentEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfInvestmentEntity.insert(_connection, investments)
  }

  public override fun getInvestments(): Flow<List<InvestmentEntity>> {
    val _sql: String = "SELECT * FROM investments"
    return createFlow(__db, false, arrayOf("investments")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfMonthlyAmount: Int = getColumnIndexOrThrow(_stmt, "monthlyAmount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStepChanges: Int = getColumnIndexOrThrow(_stmt, "stepChanges")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<InvestmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: InvestmentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpType: InvestmentType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.toInvestmentType(_tmp)
          val _tmpStartDate: LocalDate
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_2: LocalDate? = __converters.fromTimestamp(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_2
          }
          val _tmpEndDate: LocalDate?
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfEndDate)
          }
          _tmpEndDate = __converters.fromTimestamp(_tmp_3)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpMonthlyAmount: Double
          _tmpMonthlyAmount = _stmt.getDouble(_columnIndexOfMonthlyAmount)
          val _tmpInterestRate: Double
          _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpFrequency: ContributionFrequency
          val _tmp_4: String
          _tmp_4 = _stmt.getText(_columnIndexOfFrequency)
          _tmpFrequency = __converters.toContributionFrequency(_tmp_4)
          val _tmpStepChanges: String
          _tmpStepChanges = _stmt.getText(_columnIndexOfStepChanges)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_5 != 0
          _item = InvestmentEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpType,_tmpStartDate,_tmpEndDate,_tmpAmount,_tmpMonthlyAmount,_tmpInterestRate,_tmpCurrentValue,_tmpFrequency,_tmpStepChanges,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getInvestmentById(id: Long): InvestmentEntity? {
    val _sql: String = "SELECT * FROM investments WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfMonthlyAmount: Int = getColumnIndexOrThrow(_stmt, "monthlyAmount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStepChanges: Int = getColumnIndexOrThrow(_stmt, "stepChanges")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: InvestmentEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpType: InvestmentType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.toInvestmentType(_tmp)
          val _tmpStartDate: LocalDate
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_2: LocalDate? = __converters.fromTimestamp(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_2
          }
          val _tmpEndDate: LocalDate?
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfEndDate)
          }
          _tmpEndDate = __converters.fromTimestamp(_tmp_3)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpMonthlyAmount: Double
          _tmpMonthlyAmount = _stmt.getDouble(_columnIndexOfMonthlyAmount)
          val _tmpInterestRate: Double
          _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpFrequency: ContributionFrequency
          val _tmp_4: String
          _tmp_4 = _stmt.getText(_columnIndexOfFrequency)
          _tmpFrequency = __converters.toContributionFrequency(_tmp_4)
          val _tmpStepChanges: String
          _tmpStepChanges = _stmt.getText(_columnIndexOfStepChanges)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_5 != 0
          _result = InvestmentEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpType,_tmpStartDate,_tmpEndDate,_tmpAmount,_tmpMonthlyAmount,_tmpInterestRate,_tmpCurrentValue,_tmpFrequency,_tmpStepChanges,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedInvestments(): List<InvestmentEntity> {
    val _sql: String = "SELECT * FROM investments WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfMonthlyAmount: Int = getColumnIndexOrThrow(_stmt, "monthlyAmount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStepChanges: Int = getColumnIndexOrThrow(_stmt, "stepChanges")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<InvestmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: InvestmentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpType: InvestmentType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.toInvestmentType(_tmp)
          val _tmpStartDate: LocalDate
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_2: LocalDate? = __converters.fromTimestamp(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_2
          }
          val _tmpEndDate: LocalDate?
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfEndDate)
          }
          _tmpEndDate = __converters.fromTimestamp(_tmp_3)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpMonthlyAmount: Double
          _tmpMonthlyAmount = _stmt.getDouble(_columnIndexOfMonthlyAmount)
          val _tmpInterestRate: Double
          _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpFrequency: ContributionFrequency
          val _tmp_4: String
          _tmp_4 = _stmt.getText(_columnIndexOfFrequency)
          _tmpFrequency = __converters.toContributionFrequency(_tmp_4)
          val _tmpStepChanges: String
          _tmpStepChanges = _stmt.getText(_columnIndexOfStepChanges)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_5 != 0
          _item = InvestmentEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpType,_tmpStartDate,_tmpEndDate,_tmpAmount,_tmpMonthlyAmount,_tmpInterestRate,_tmpCurrentValue,_tmpFrequency,_tmpStepChanges,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getInvestmentByRemoteId(remoteId: String): InvestmentEntity? {
    val _sql: String = "SELECT * FROM investments WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfMonthlyAmount: Int = getColumnIndexOrThrow(_stmt, "monthlyAmount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStepChanges: Int = getColumnIndexOrThrow(_stmt, "stepChanges")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: InvestmentEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpType: InvestmentType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.toInvestmentType(_tmp)
          val _tmpStartDate: LocalDate
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_2: LocalDate? = __converters.fromTimestamp(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_2
          }
          val _tmpEndDate: LocalDate?
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfEndDate)
          }
          _tmpEndDate = __converters.fromTimestamp(_tmp_3)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpMonthlyAmount: Double
          _tmpMonthlyAmount = _stmt.getDouble(_columnIndexOfMonthlyAmount)
          val _tmpInterestRate: Double
          _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpFrequency: ContributionFrequency
          val _tmp_4: String
          _tmp_4 = _stmt.getText(_columnIndexOfFrequency)
          _tmpFrequency = __converters.toContributionFrequency(_tmp_4)
          val _tmpStepChanges: String
          _tmpStepChanges = _stmt.getText(_columnIndexOfStepChanges)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_5 != 0
          _result = InvestmentEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpType,_tmpStartDate,_tmpEndDate,_tmpAmount,_tmpMonthlyAmount,_tmpInterestRate,_tmpCurrentValue,_tmpFrequency,_tmpStepChanges,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllInvestments(): List<InvestmentEntity> {
    val _sql: String = "SELECT * FROM investments"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfEndDate: Int = getColumnIndexOrThrow(_stmt, "endDate")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfMonthlyAmount: Int = getColumnIndexOrThrow(_stmt, "monthlyAmount")
        val _columnIndexOfInterestRate: Int = getColumnIndexOrThrow(_stmt, "interestRate")
        val _columnIndexOfCurrentValue: Int = getColumnIndexOrThrow(_stmt, "currentValue")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfStepChanges: Int = getColumnIndexOrThrow(_stmt, "stepChanges")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<InvestmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: InvestmentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpType: InvestmentType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.toInvestmentType(_tmp)
          val _tmpStartDate: LocalDate
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_2: LocalDate? = __converters.fromTimestamp(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_2
          }
          val _tmpEndDate: LocalDate?
          val _tmp_3: String?
          if (_stmt.isNull(_columnIndexOfEndDate)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getText(_columnIndexOfEndDate)
          }
          _tmpEndDate = __converters.fromTimestamp(_tmp_3)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpMonthlyAmount: Double
          _tmpMonthlyAmount = _stmt.getDouble(_columnIndexOfMonthlyAmount)
          val _tmpInterestRate: Double
          _tmpInterestRate = _stmt.getDouble(_columnIndexOfInterestRate)
          val _tmpCurrentValue: Double
          _tmpCurrentValue = _stmt.getDouble(_columnIndexOfCurrentValue)
          val _tmpFrequency: ContributionFrequency
          val _tmp_4: String
          _tmp_4 = _stmt.getText(_columnIndexOfFrequency)
          _tmpFrequency = __converters.toContributionFrequency(_tmp_4)
          val _tmpStepChanges: String
          _tmpStepChanges = _stmt.getText(_columnIndexOfStepChanges)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_5 != 0
          _item = InvestmentEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpType,_tmpStartDate,_tmpEndDate,_tmpAmount,_tmpMonthlyAmount,_tmpInterestRate,_tmpCurrentValue,_tmpFrequency,_tmpStepChanges,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteInvestment(id: Long) {
    val _sql: String = "DELETE FROM investments WHERE id = ?"
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
    _stringBuilder.append("UPDATE investments SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun deleteAllInvestments() {
    val _sql: String = "DELETE FROM investments"
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
