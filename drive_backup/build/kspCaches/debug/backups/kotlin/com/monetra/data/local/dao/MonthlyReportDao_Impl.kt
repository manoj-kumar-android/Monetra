package com.monetra.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.entity.MonthlyReportEntity
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
public class MonthlyReportDao_Impl(
  __db: RoomDatabase,
) : MonthlyReportDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMonthlyReportEntity: EntityInsertAdapter<MonthlyReportEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMonthlyReportEntity = object : EntityInsertAdapter<MonthlyReportEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `monthly_reports` (`month`,`remoteId`,`income`,`expenses`,`emis`,`investments`,`actualSavings`,`targetSavings`,`status`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MonthlyReportEntity) {
        statement.bindText(1, entity.month)
        statement.bindText(2, entity.remoteId)
        statement.bindDouble(3, entity.income)
        statement.bindDouble(4, entity.expenses)
        statement.bindDouble(5, entity.emis)
        statement.bindDouble(6, entity.investments)
        statement.bindDouble(7, entity.actualSavings)
        statement.bindDouble(8, entity.targetSavings)
        statement.bindText(9, entity.status)
        statement.bindLong(10, entity.version)
        statement.bindLong(11, entity.updatedAt)
        statement.bindText(12, entity.deviceId)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(13, _tmp.toLong())
      }
    }
  }

  public override suspend fun insertReport(report: MonthlyReportEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMonthlyReportEntity.insert(_connection, report)
  }

  public override suspend fun insertAllMonthlyReports(reports: List<MonthlyReportEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMonthlyReportEntity.insert(_connection, reports)
  }

  public override suspend fun getReportByMonth(month: String): MonthlyReportEntity? {
    val _sql: String = "SELECT * FROM monthly_reports WHERE month = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, month)
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfIncome: Int = getColumnIndexOrThrow(_stmt, "income")
        val _columnIndexOfExpenses: Int = getColumnIndexOrThrow(_stmt, "expenses")
        val _columnIndexOfEmis: Int = getColumnIndexOrThrow(_stmt, "emis")
        val _columnIndexOfInvestments: Int = getColumnIndexOrThrow(_stmt, "investments")
        val _columnIndexOfActualSavings: Int = getColumnIndexOrThrow(_stmt, "actualSavings")
        val _columnIndexOfTargetSavings: Int = getColumnIndexOrThrow(_stmt, "targetSavings")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MonthlyReportEntity?
        if (_stmt.step()) {
          val _tmpMonth: String
          _tmpMonth = _stmt.getText(_columnIndexOfMonth)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpIncome: Double
          _tmpIncome = _stmt.getDouble(_columnIndexOfIncome)
          val _tmpExpenses: Double
          _tmpExpenses = _stmt.getDouble(_columnIndexOfExpenses)
          val _tmpEmis: Double
          _tmpEmis = _stmt.getDouble(_columnIndexOfEmis)
          val _tmpInvestments: Double
          _tmpInvestments = _stmt.getDouble(_columnIndexOfInvestments)
          val _tmpActualSavings: Double
          _tmpActualSavings = _stmt.getDouble(_columnIndexOfActualSavings)
          val _tmpTargetSavings: Double
          _tmpTargetSavings = _stmt.getDouble(_columnIndexOfTargetSavings)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
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
          _result = MonthlyReportEntity(_tmpMonth,_tmpRemoteId,_tmpIncome,_tmpExpenses,_tmpEmis,_tmpInvestments,_tmpActualSavings,_tmpTargetSavings,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllReports(): Flow<List<MonthlyReportEntity>> {
    val _sql: String = "SELECT * FROM monthly_reports ORDER BY month DESC"
    return createFlow(__db, false, arrayOf("monthly_reports")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfIncome: Int = getColumnIndexOrThrow(_stmt, "income")
        val _columnIndexOfExpenses: Int = getColumnIndexOrThrow(_stmt, "expenses")
        val _columnIndexOfEmis: Int = getColumnIndexOrThrow(_stmt, "emis")
        val _columnIndexOfInvestments: Int = getColumnIndexOrThrow(_stmt, "investments")
        val _columnIndexOfActualSavings: Int = getColumnIndexOrThrow(_stmt, "actualSavings")
        val _columnIndexOfTargetSavings: Int = getColumnIndexOrThrow(_stmt, "targetSavings")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<MonthlyReportEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MonthlyReportEntity
          val _tmpMonth: String
          _tmpMonth = _stmt.getText(_columnIndexOfMonth)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpIncome: Double
          _tmpIncome = _stmt.getDouble(_columnIndexOfIncome)
          val _tmpExpenses: Double
          _tmpExpenses = _stmt.getDouble(_columnIndexOfExpenses)
          val _tmpEmis: Double
          _tmpEmis = _stmt.getDouble(_columnIndexOfEmis)
          val _tmpInvestments: Double
          _tmpInvestments = _stmt.getDouble(_columnIndexOfInvestments)
          val _tmpActualSavings: Double
          _tmpActualSavings = _stmt.getDouble(_columnIndexOfActualSavings)
          val _tmpTargetSavings: Double
          _tmpTargetSavings = _stmt.getDouble(_columnIndexOfTargetSavings)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
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
          _item = MonthlyReportEntity(_tmpMonth,_tmpRemoteId,_tmpIncome,_tmpExpenses,_tmpEmis,_tmpInvestments,_tmpActualSavings,_tmpTargetSavings,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedReports(): List<MonthlyReportEntity> {
    val _sql: String = "SELECT * FROM monthly_reports WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfIncome: Int = getColumnIndexOrThrow(_stmt, "income")
        val _columnIndexOfExpenses: Int = getColumnIndexOrThrow(_stmt, "expenses")
        val _columnIndexOfEmis: Int = getColumnIndexOrThrow(_stmt, "emis")
        val _columnIndexOfInvestments: Int = getColumnIndexOrThrow(_stmt, "investments")
        val _columnIndexOfActualSavings: Int = getColumnIndexOrThrow(_stmt, "actualSavings")
        val _columnIndexOfTargetSavings: Int = getColumnIndexOrThrow(_stmt, "targetSavings")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<MonthlyReportEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MonthlyReportEntity
          val _tmpMonth: String
          _tmpMonth = _stmt.getText(_columnIndexOfMonth)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpIncome: Double
          _tmpIncome = _stmt.getDouble(_columnIndexOfIncome)
          val _tmpExpenses: Double
          _tmpExpenses = _stmt.getDouble(_columnIndexOfExpenses)
          val _tmpEmis: Double
          _tmpEmis = _stmt.getDouble(_columnIndexOfEmis)
          val _tmpInvestments: Double
          _tmpInvestments = _stmt.getDouble(_columnIndexOfInvestments)
          val _tmpActualSavings: Double
          _tmpActualSavings = _stmt.getDouble(_columnIndexOfActualSavings)
          val _tmpTargetSavings: Double
          _tmpTargetSavings = _stmt.getDouble(_columnIndexOfTargetSavings)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
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
          _item = MonthlyReportEntity(_tmpMonth,_tmpRemoteId,_tmpIncome,_tmpExpenses,_tmpEmis,_tmpInvestments,_tmpActualSavings,_tmpTargetSavings,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getReportByRemoteId(remoteId: String): MonthlyReportEntity? {
    val _sql: String = "SELECT * FROM monthly_reports WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfIncome: Int = getColumnIndexOrThrow(_stmt, "income")
        val _columnIndexOfExpenses: Int = getColumnIndexOrThrow(_stmt, "expenses")
        val _columnIndexOfEmis: Int = getColumnIndexOrThrow(_stmt, "emis")
        val _columnIndexOfInvestments: Int = getColumnIndexOrThrow(_stmt, "investments")
        val _columnIndexOfActualSavings: Int = getColumnIndexOrThrow(_stmt, "actualSavings")
        val _columnIndexOfTargetSavings: Int = getColumnIndexOrThrow(_stmt, "targetSavings")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MonthlyReportEntity?
        if (_stmt.step()) {
          val _tmpMonth: String
          _tmpMonth = _stmt.getText(_columnIndexOfMonth)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpIncome: Double
          _tmpIncome = _stmt.getDouble(_columnIndexOfIncome)
          val _tmpExpenses: Double
          _tmpExpenses = _stmt.getDouble(_columnIndexOfExpenses)
          val _tmpEmis: Double
          _tmpEmis = _stmt.getDouble(_columnIndexOfEmis)
          val _tmpInvestments: Double
          _tmpInvestments = _stmt.getDouble(_columnIndexOfInvestments)
          val _tmpActualSavings: Double
          _tmpActualSavings = _stmt.getDouble(_columnIndexOfActualSavings)
          val _tmpTargetSavings: Double
          _tmpTargetSavings = _stmt.getDouble(_columnIndexOfTargetSavings)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
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
          _result = MonthlyReportEntity(_tmpMonth,_tmpRemoteId,_tmpIncome,_tmpExpenses,_tmpEmis,_tmpInvestments,_tmpActualSavings,_tmpTargetSavings,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllMonthlyReportsList(): List<MonthlyReportEntity> {
    val _sql: String = "SELECT * FROM monthly_reports"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfIncome: Int = getColumnIndexOrThrow(_stmt, "income")
        val _columnIndexOfExpenses: Int = getColumnIndexOrThrow(_stmt, "expenses")
        val _columnIndexOfEmis: Int = getColumnIndexOrThrow(_stmt, "emis")
        val _columnIndexOfInvestments: Int = getColumnIndexOrThrow(_stmt, "investments")
        val _columnIndexOfActualSavings: Int = getColumnIndexOrThrow(_stmt, "actualSavings")
        val _columnIndexOfTargetSavings: Int = getColumnIndexOrThrow(_stmt, "targetSavings")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<MonthlyReportEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MonthlyReportEntity
          val _tmpMonth: String
          _tmpMonth = _stmt.getText(_columnIndexOfMonth)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpIncome: Double
          _tmpIncome = _stmt.getDouble(_columnIndexOfIncome)
          val _tmpExpenses: Double
          _tmpExpenses = _stmt.getDouble(_columnIndexOfExpenses)
          val _tmpEmis: Double
          _tmpEmis = _stmt.getDouble(_columnIndexOfEmis)
          val _tmpInvestments: Double
          _tmpInvestments = _stmt.getDouble(_columnIndexOfInvestments)
          val _tmpActualSavings: Double
          _tmpActualSavings = _stmt.getDouble(_columnIndexOfActualSavings)
          val _tmpTargetSavings: Double
          _tmpTargetSavings = _stmt.getDouble(_columnIndexOfTargetSavings)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
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
          _item = MonthlyReportEntity(_tmpMonth,_tmpRemoteId,_tmpIncome,_tmpExpenses,_tmpEmis,_tmpInvestments,_tmpActualSavings,_tmpTargetSavings,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
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
    _stringBuilder.append("UPDATE monthly_reports SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun deleteAllMonthlyReports() {
    val _sql: String = "DELETE FROM monthly_reports"
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
