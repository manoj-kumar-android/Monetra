package com.monetra.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.Converters
import com.monetra.`data`.local.entity.LoanEntity
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
public class LoanDao_Impl(
  __db: RoomDatabase,
) : LoanDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLoanEntity: EntityInsertAdapter<LoanEntity>

  private val __converters: Converters = Converters()

  private val __updateAdapterOfLoanEntity: EntityDeleteOrUpdateAdapter<LoanEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfLoanEntity = object : EntityInsertAdapter<LoanEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `loans` (`id`,`remoteId`,`name`,`totalPrincipal`,`annualInterestRate`,`monthlyEmi`,`startDate`,`tenureMonths`,`remainingTenure`,`category`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LoanEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.name)
        statement.bindDouble(4, entity.totalPrincipal)
        statement.bindDouble(5, entity.annualInterestRate)
        statement.bindDouble(6, entity.monthlyEmi)
        val _tmp: String? = __converters.dateToTimestamp(entity.startDate)
        if (_tmp == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmp)
        }
        statement.bindLong(8, entity.tenureMonths.toLong())
        statement.bindLong(9, entity.remainingTenure.toLong())
        statement.bindText(10, entity.category)
        statement.bindLong(11, entity.version)
        statement.bindLong(12, entity.updatedAt)
        statement.bindText(13, entity.deviceId)
        val _tmp_1: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(14, _tmp_1.toLong())
      }
    }
    this.__updateAdapterOfLoanEntity = object : EntityDeleteOrUpdateAdapter<LoanEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `loans` SET `id` = ?,`remoteId` = ?,`name` = ?,`totalPrincipal` = ?,`annualInterestRate` = ?,`monthlyEmi` = ?,`startDate` = ?,`tenureMonths` = ?,`remainingTenure` = ?,`category` = ?,`version` = ?,`updatedAt` = ?,`deviceId` = ?,`isSynced` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: LoanEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.name)
        statement.bindDouble(4, entity.totalPrincipal)
        statement.bindDouble(5, entity.annualInterestRate)
        statement.bindDouble(6, entity.monthlyEmi)
        val _tmp: String? = __converters.dateToTimestamp(entity.startDate)
        if (_tmp == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmp)
        }
        statement.bindLong(8, entity.tenureMonths.toLong())
        statement.bindLong(9, entity.remainingTenure.toLong())
        statement.bindText(10, entity.category)
        statement.bindLong(11, entity.version)
        statement.bindLong(12, entity.updatedAt)
        statement.bindText(13, entity.deviceId)
        val _tmp_1: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(14, _tmp_1.toLong())
        statement.bindLong(15, entity.id)
      }
    }
  }

  public override suspend fun insertLoan(loan: LoanEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLoanEntity.insert(_connection, loan)
  }

  public override suspend fun insertAllLoans(loans: List<LoanEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLoanEntity.insert(_connection, loans)
  }

  public override suspend fun updateLoan(loan: LoanEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfLoanEntity.handle(_connection, loan)
  }

  public override fun getAllLoans(): Flow<List<LoanEntity>> {
    val _sql: String = "SELECT * FROM loans"
    return createFlow(__db, false, arrayOf("loans")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTotalPrincipal: Int = getColumnIndexOrThrow(_stmt, "totalPrincipal")
        val _columnIndexOfAnnualInterestRate: Int = getColumnIndexOrThrow(_stmt, "annualInterestRate")
        val _columnIndexOfMonthlyEmi: Int = getColumnIndexOrThrow(_stmt, "monthlyEmi")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfTenureMonths: Int = getColumnIndexOrThrow(_stmt, "tenureMonths")
        val _columnIndexOfRemainingTenure: Int = getColumnIndexOrThrow(_stmt, "remainingTenure")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<LoanEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LoanEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTotalPrincipal: Double
          _tmpTotalPrincipal = _stmt.getDouble(_columnIndexOfTotalPrincipal)
          val _tmpAnnualInterestRate: Double
          _tmpAnnualInterestRate = _stmt.getDouble(_columnIndexOfAnnualInterestRate)
          val _tmpMonthlyEmi: Double
          _tmpMonthlyEmi = _stmt.getDouble(_columnIndexOfMonthlyEmi)
          val _tmpStartDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpTenureMonths: Int
          _tmpTenureMonths = _stmt.getLong(_columnIndexOfTenureMonths).toInt()
          val _tmpRemainingTenure: Int
          _tmpRemainingTenure = _stmt.getLong(_columnIndexOfRemainingTenure).toInt()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          _item = LoanEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpTotalPrincipal,_tmpAnnualInterestRate,_tmpMonthlyEmi,_tmpStartDate,_tmpTenureMonths,_tmpRemainingTenure,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLoanById(id: Long): LoanEntity? {
    val _sql: String = "SELECT * FROM loans WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTotalPrincipal: Int = getColumnIndexOrThrow(_stmt, "totalPrincipal")
        val _columnIndexOfAnnualInterestRate: Int = getColumnIndexOrThrow(_stmt, "annualInterestRate")
        val _columnIndexOfMonthlyEmi: Int = getColumnIndexOrThrow(_stmt, "monthlyEmi")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfTenureMonths: Int = getColumnIndexOrThrow(_stmt, "tenureMonths")
        val _columnIndexOfRemainingTenure: Int = getColumnIndexOrThrow(_stmt, "remainingTenure")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: LoanEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTotalPrincipal: Double
          _tmpTotalPrincipal = _stmt.getDouble(_columnIndexOfTotalPrincipal)
          val _tmpAnnualInterestRate: Double
          _tmpAnnualInterestRate = _stmt.getDouble(_columnIndexOfAnnualInterestRate)
          val _tmpMonthlyEmi: Double
          _tmpMonthlyEmi = _stmt.getDouble(_columnIndexOfMonthlyEmi)
          val _tmpStartDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpTenureMonths: Int
          _tmpTenureMonths = _stmt.getLong(_columnIndexOfTenureMonths).toInt()
          val _tmpRemainingTenure: Int
          _tmpRemainingTenure = _stmt.getLong(_columnIndexOfRemainingTenure).toInt()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          _result = LoanEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpTotalPrincipal,_tmpAnnualInterestRate,_tmpMonthlyEmi,_tmpStartDate,_tmpTenureMonths,_tmpRemainingTenure,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalMonthlyEmi(): Flow<Double?> {
    val _sql: String = "SELECT SUM(monthlyEmi) FROM loans"
    return createFlow(__db, false, arrayOf("loans")) { _connection ->
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

  public override suspend fun getUnsyncedLoans(): List<LoanEntity> {
    val _sql: String = "SELECT * FROM loans WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTotalPrincipal: Int = getColumnIndexOrThrow(_stmt, "totalPrincipal")
        val _columnIndexOfAnnualInterestRate: Int = getColumnIndexOrThrow(_stmt, "annualInterestRate")
        val _columnIndexOfMonthlyEmi: Int = getColumnIndexOrThrow(_stmt, "monthlyEmi")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfTenureMonths: Int = getColumnIndexOrThrow(_stmt, "tenureMonths")
        val _columnIndexOfRemainingTenure: Int = getColumnIndexOrThrow(_stmt, "remainingTenure")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<LoanEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LoanEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTotalPrincipal: Double
          _tmpTotalPrincipal = _stmt.getDouble(_columnIndexOfTotalPrincipal)
          val _tmpAnnualInterestRate: Double
          _tmpAnnualInterestRate = _stmt.getDouble(_columnIndexOfAnnualInterestRate)
          val _tmpMonthlyEmi: Double
          _tmpMonthlyEmi = _stmt.getDouble(_columnIndexOfMonthlyEmi)
          val _tmpStartDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpTenureMonths: Int
          _tmpTenureMonths = _stmt.getLong(_columnIndexOfTenureMonths).toInt()
          val _tmpRemainingTenure: Int
          _tmpRemainingTenure = _stmt.getLong(_columnIndexOfRemainingTenure).toInt()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          _item = LoanEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpTotalPrincipal,_tmpAnnualInterestRate,_tmpMonthlyEmi,_tmpStartDate,_tmpTenureMonths,_tmpRemainingTenure,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLoanByRemoteId(remoteId: String): LoanEntity? {
    val _sql: String = "SELECT * FROM loans WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTotalPrincipal: Int = getColumnIndexOrThrow(_stmt, "totalPrincipal")
        val _columnIndexOfAnnualInterestRate: Int = getColumnIndexOrThrow(_stmt, "annualInterestRate")
        val _columnIndexOfMonthlyEmi: Int = getColumnIndexOrThrow(_stmt, "monthlyEmi")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfTenureMonths: Int = getColumnIndexOrThrow(_stmt, "tenureMonths")
        val _columnIndexOfRemainingTenure: Int = getColumnIndexOrThrow(_stmt, "remainingTenure")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: LoanEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTotalPrincipal: Double
          _tmpTotalPrincipal = _stmt.getDouble(_columnIndexOfTotalPrincipal)
          val _tmpAnnualInterestRate: Double
          _tmpAnnualInterestRate = _stmt.getDouble(_columnIndexOfAnnualInterestRate)
          val _tmpMonthlyEmi: Double
          _tmpMonthlyEmi = _stmt.getDouble(_columnIndexOfMonthlyEmi)
          val _tmpStartDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpTenureMonths: Int
          _tmpTenureMonths = _stmt.getLong(_columnIndexOfTenureMonths).toInt()
          val _tmpRemainingTenure: Int
          _tmpRemainingTenure = _stmt.getLong(_columnIndexOfRemainingTenure).toInt()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          _result = LoanEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpTotalPrincipal,_tmpAnnualInterestRate,_tmpMonthlyEmi,_tmpStartDate,_tmpTenureMonths,_tmpRemainingTenure,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllLoansForBackUp(): List<LoanEntity> {
    val _sql: String = "SELECT * FROM loans"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTotalPrincipal: Int = getColumnIndexOrThrow(_stmt, "totalPrincipal")
        val _columnIndexOfAnnualInterestRate: Int = getColumnIndexOrThrow(_stmt, "annualInterestRate")
        val _columnIndexOfMonthlyEmi: Int = getColumnIndexOrThrow(_stmt, "monthlyEmi")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfTenureMonths: Int = getColumnIndexOrThrow(_stmt, "tenureMonths")
        val _columnIndexOfRemainingTenure: Int = getColumnIndexOrThrow(_stmt, "remainingTenure")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<LoanEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LoanEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTotalPrincipal: Double
          _tmpTotalPrincipal = _stmt.getDouble(_columnIndexOfTotalPrincipal)
          val _tmpAnnualInterestRate: Double
          _tmpAnnualInterestRate = _stmt.getDouble(_columnIndexOfAnnualInterestRate)
          val _tmpMonthlyEmi: Double
          _tmpMonthlyEmi = _stmt.getDouble(_columnIndexOfMonthlyEmi)
          val _tmpStartDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpTenureMonths: Int
          _tmpTenureMonths = _stmt.getLong(_columnIndexOfTenureMonths).toInt()
          val _tmpRemainingTenure: Int
          _tmpRemainingTenure = _stmt.getLong(_columnIndexOfRemainingTenure).toInt()
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
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
          _item = LoanEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpTotalPrincipal,_tmpAnnualInterestRate,_tmpMonthlyEmi,_tmpStartDate,_tmpTenureMonths,_tmpRemainingTenure,_tmpCategory,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteLoan(id: Long) {
    val _sql: String = "DELETE FROM loans WHERE id = ?"
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
    _stringBuilder.append("UPDATE loans SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun deleteAllLoans() {
    val _sql: String = "DELETE FROM loans"
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
