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
import com.monetra.`data`.local.entity.BillInstanceEntity
import com.monetra.`data`.local.entity.MonthlyExpenseEntity
import com.monetra.domain.model.BillStatus
import java.time.YearMonth
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
public class MonthlyExpenseDao_Impl(
  __db: RoomDatabase,
) : MonthlyExpenseDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMonthlyExpenseEntity: EntityInsertAdapter<MonthlyExpenseEntity>

  private val __insertAdapterOfBillInstanceEntity: EntityInsertAdapter<BillInstanceEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfMonthlyExpenseEntity:
      EntityDeleteOrUpdateAdapter<MonthlyExpenseEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMonthlyExpenseEntity = object : EntityInsertAdapter<MonthlyExpenseEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `monthly_expenses` (`id`,`remoteId`,`name`,`amount`,`category`,`dueDay`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MonthlyExpenseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.name)
        statement.bindDouble(4, entity.amount)
        statement.bindText(5, entity.category)
        statement.bindLong(6, entity.dueDay.toLong())
        statement.bindLong(7, entity.version)
        statement.bindLong(8, entity.updatedAt)
        statement.bindText(9, entity.deviceId)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(10, _tmp.toLong())
      }
    }
    this.__insertAdapterOfBillInstanceEntity = object : EntityInsertAdapter<BillInstanceEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `bill_instances` (`id`,`remoteId`,`billId`,`month`,`amount`,`paidAmount`,`status`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BillInstanceEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindLong(3, entity.billId)
        val _tmp: String? = __converters.fromYearMonth(entity.month)
        if (_tmp == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmp)
        }
        statement.bindDouble(5, entity.amount)
        statement.bindDouble(6, entity.paidAmount)
        val _tmp_1: String = __converters.fromBillStatus(entity.status)
        statement.bindText(7, _tmp_1)
        statement.bindLong(8, entity.version)
        statement.bindLong(9, entity.updatedAt)
        statement.bindText(10, entity.deviceId)
        val _tmp_2: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(11, _tmp_2.toLong())
      }
    }
    this.__deleteAdapterOfMonthlyExpenseEntity = object : EntityDeleteOrUpdateAdapter<MonthlyExpenseEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `monthly_expenses` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MonthlyExpenseEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertMonthlyExpense(expense: MonthlyExpenseEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfMonthlyExpenseEntity.insertAndReturnId(_connection, expense)
    _result
  }

  public override suspend fun insertBillInstance(instance: BillInstanceEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfBillInstanceEntity.insert(_connection, instance)
  }

  public override suspend fun insertAllMonthlyExpenses(expenses: List<MonthlyExpenseEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMonthlyExpenseEntity.insert(_connection, expenses)
  }

  public override suspend fun insertAllBillInstances(instances: List<BillInstanceEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfBillInstanceEntity.insert(_connection, instances)
  }

  public override suspend fun deleteMonthlyExpense(expense: MonthlyExpenseEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfMonthlyExpenseEntity.handle(_connection, expense)
  }

  public override fun getAllMonthlyExpenses(): Flow<List<MonthlyExpenseEntity>> {
    val _sql: String = "SELECT * FROM monthly_expenses"
    return createFlow(__db, false, arrayOf("monthly_expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfDueDay: Int = getColumnIndexOrThrow(_stmt, "dueDay")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<MonthlyExpenseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MonthlyExpenseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpDueDay: Int
          _tmpDueDay = _stmt.getLong(_columnIndexOfDueDay).toInt()
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
          _item = MonthlyExpenseEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpAmount,_tmpCategory,_tmpDueDay,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMonthlyExpensesByCategory(category: String): List<MonthlyExpenseEntity> {
    val _sql: String = "SELECT * FROM monthly_expenses WHERE category = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfDueDay: Int = getColumnIndexOrThrow(_stmt, "dueDay")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<MonthlyExpenseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MonthlyExpenseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpDueDay: Int
          _tmpDueDay = _stmt.getLong(_columnIndexOfDueDay).toInt()
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
          _item = MonthlyExpenseEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpAmount,_tmpCategory,_tmpDueDay,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMonthlyExpenseById(id: Long): MonthlyExpenseEntity? {
    val _sql: String = "SELECT * FROM monthly_expenses WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfDueDay: Int = getColumnIndexOrThrow(_stmt, "dueDay")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MonthlyExpenseEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpDueDay: Int
          _tmpDueDay = _stmt.getLong(_columnIndexOfDueDay).toInt()
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
          _result = MonthlyExpenseEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpAmount,_tmpCategory,_tmpDueDay,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalMonthlyExpenseAmount(): Flow<Double?> {
    val _sql: String = "SELECT SUM(amount) FROM monthly_expenses"
    return createFlow(__db, false, arrayOf("monthly_expenses")) { _connection ->
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

  public override suspend fun getAllInstancesForBillList(billId: Long): List<BillInstanceEntity> {
    val _sql: String = "SELECT * FROM bill_instances WHERE billId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, billId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBillId: Int = getColumnIndexOrThrow(_stmt, "billId")
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPaidAmount: Int = getColumnIndexOrThrow(_stmt, "paidAmount")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<BillInstanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BillInstanceEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBillId: Long
          _tmpBillId = _stmt.getLong(_columnIndexOfBillId)
          val _tmpMonth: YearMonth
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfMonth)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfMonth)
          }
          val _tmp_1: YearMonth? = __converters.toYearMonth(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.YearMonth', but it was NULL.")
          } else {
            _tmpMonth = _tmp_1
          }
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPaidAmount: Double
          _tmpPaidAmount = _stmt.getDouble(_columnIndexOfPaidAmount)
          val _tmpStatus: BillStatus
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __converters.toBillStatus(_tmp_2)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_3 != 0
          _item = BillInstanceEntity(_tmpId,_tmpRemoteId,_tmpBillId,_tmpMonth,_tmpAmount,_tmpPaidAmount,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getInstancesForMonth(month: YearMonth): Flow<List<BillInstanceEntity>> {
    val _sql: String = "SELECT * FROM bill_instances WHERE month = ?"
    return createFlow(__db, false, arrayOf("bill_instances")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String? = __converters.fromYearMonth(month)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp)
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBillId: Int = getColumnIndexOrThrow(_stmt, "billId")
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPaidAmount: Int = getColumnIndexOrThrow(_stmt, "paidAmount")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<BillInstanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BillInstanceEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBillId: Long
          _tmpBillId = _stmt.getLong(_columnIndexOfBillId)
          val _tmpMonth: YearMonth
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfMonth)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfMonth)
          }
          val _tmp_2: YearMonth? = __converters.toYearMonth(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'java.time.YearMonth', but it was NULL.")
          } else {
            _tmpMonth = _tmp_2
          }
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPaidAmount: Double
          _tmpPaidAmount = _stmt.getDouble(_columnIndexOfPaidAmount)
          val _tmpStatus: BillStatus
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __converters.toBillStatus(_tmp_3)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_4 != 0
          _item = BillInstanceEntity(_tmpId,_tmpRemoteId,_tmpBillId,_tmpMonth,_tmpAmount,_tmpPaidAmount,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getInstanceByBillAndMonth(billId: Long, month: YearMonth): BillInstanceEntity? {
    val _sql: String = "SELECT * FROM bill_instances WHERE billId = ? AND month = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, billId)
        _argIndex = 2
        val _tmp: String? = __converters.fromYearMonth(month)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp)
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBillId: Int = getColumnIndexOrThrow(_stmt, "billId")
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPaidAmount: Int = getColumnIndexOrThrow(_stmt, "paidAmount")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: BillInstanceEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBillId: Long
          _tmpBillId = _stmt.getLong(_columnIndexOfBillId)
          val _tmpMonth: YearMonth
          val _tmp_1: String?
          if (_stmt.isNull(_columnIndexOfMonth)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getText(_columnIndexOfMonth)
          }
          val _tmp_2: YearMonth? = __converters.toYearMonth(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'java.time.YearMonth', but it was NULL.")
          } else {
            _tmpMonth = _tmp_2
          }
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPaidAmount: Double
          _tmpPaidAmount = _stmt.getDouble(_columnIndexOfPaidAmount)
          val _tmpStatus: BillStatus
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __converters.toBillStatus(_tmp_3)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_4 != 0
          _result = BillInstanceEntity(_tmpId,_tmpRemoteId,_tmpBillId,_tmpMonth,_tmpAmount,_tmpPaidAmount,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getInstanceById(id: Long?): BillInstanceEntity? {
    val _sql: String = "SELECT * FROM bill_instances WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (id == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindLong(_argIndex, id)
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBillId: Int = getColumnIndexOrThrow(_stmt, "billId")
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPaidAmount: Int = getColumnIndexOrThrow(_stmt, "paidAmount")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: BillInstanceEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBillId: Long
          _tmpBillId = _stmt.getLong(_columnIndexOfBillId)
          val _tmpMonth: YearMonth
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfMonth)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfMonth)
          }
          val _tmp_1: YearMonth? = __converters.toYearMonth(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.YearMonth', but it was NULL.")
          } else {
            _tmpMonth = _tmp_1
          }
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPaidAmount: Double
          _tmpPaidAmount = _stmt.getDouble(_columnIndexOfPaidAmount)
          val _tmpStatus: BillStatus
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __converters.toBillStatus(_tmp_2)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_3 != 0
          _result = BillInstanceEntity(_tmpId,_tmpRemoteId,_tmpBillId,_tmpMonth,_tmpAmount,_tmpPaidAmount,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalReservedAmountForMonth(month: YearMonth): Flow<Double?> {
    val _sql: String = "SELECT SUM(amount - paidAmount) FROM bill_instances WHERE month = ? AND status != 'PAID'"
    return createFlow(__db, false, arrayOf("bill_instances")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String? = __converters.fromYearMonth(month)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp)
        }
        val _result: Double?
        if (_stmt.step()) {
          val _tmp_1: Double?
          if (_stmt.isNull(0)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getDouble(0)
          }
          _result = _tmp_1
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun hasInstanceForMonth(billId: Long, month: YearMonth): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM bill_instances WHERE billId = ? AND month = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, billId)
        _argIndex = 2
        val _tmp: String? = __converters.fromYearMonth(month)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp)
        }
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(0).toInt()
          _result = _tmp_1 != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedExpenses(): List<MonthlyExpenseEntity> {
    val _sql: String = "SELECT * FROM monthly_expenses WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfDueDay: Int = getColumnIndexOrThrow(_stmt, "dueDay")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<MonthlyExpenseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MonthlyExpenseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpDueDay: Int
          _tmpDueDay = _stmt.getLong(_columnIndexOfDueDay).toInt()
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
          _item = MonthlyExpenseEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpAmount,_tmpCategory,_tmpDueDay,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExpenseByRemoteId(remoteId: String): MonthlyExpenseEntity? {
    val _sql: String = "SELECT * FROM monthly_expenses WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfDueDay: Int = getColumnIndexOrThrow(_stmt, "dueDay")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MonthlyExpenseEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpDueDay: Int
          _tmpDueDay = _stmt.getLong(_columnIndexOfDueDay).toInt()
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
          _result = MonthlyExpenseEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpAmount,_tmpCategory,_tmpDueDay,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedInstances(): List<BillInstanceEntity> {
    val _sql: String = "SELECT * FROM bill_instances WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBillId: Int = getColumnIndexOrThrow(_stmt, "billId")
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPaidAmount: Int = getColumnIndexOrThrow(_stmt, "paidAmount")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<BillInstanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BillInstanceEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBillId: Long
          _tmpBillId = _stmt.getLong(_columnIndexOfBillId)
          val _tmpMonth: YearMonth
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfMonth)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfMonth)
          }
          val _tmp_1: YearMonth? = __converters.toYearMonth(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.YearMonth', but it was NULL.")
          } else {
            _tmpMonth = _tmp_1
          }
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPaidAmount: Double
          _tmpPaidAmount = _stmt.getDouble(_columnIndexOfPaidAmount)
          val _tmpStatus: BillStatus
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __converters.toBillStatus(_tmp_2)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_3 != 0
          _item = BillInstanceEntity(_tmpId,_tmpRemoteId,_tmpBillId,_tmpMonth,_tmpAmount,_tmpPaidAmount,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getInstanceByRemoteId(remoteId: String): BillInstanceEntity? {
    val _sql: String = "SELECT * FROM bill_instances WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBillId: Int = getColumnIndexOrThrow(_stmt, "billId")
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPaidAmount: Int = getColumnIndexOrThrow(_stmt, "paidAmount")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: BillInstanceEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBillId: Long
          _tmpBillId = _stmt.getLong(_columnIndexOfBillId)
          val _tmpMonth: YearMonth
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfMonth)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfMonth)
          }
          val _tmp_1: YearMonth? = __converters.toYearMonth(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.YearMonth', but it was NULL.")
          } else {
            _tmpMonth = _tmp_1
          }
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPaidAmount: Double
          _tmpPaidAmount = _stmt.getDouble(_columnIndexOfPaidAmount)
          val _tmpStatus: BillStatus
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __converters.toBillStatus(_tmp_2)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_3 != 0
          _result = BillInstanceEntity(_tmpId,_tmpRemoteId,_tmpBillId,_tmpMonth,_tmpAmount,_tmpPaidAmount,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllMonthlyExpensesList(): List<MonthlyExpenseEntity> {
    val _sql: String = "SELECT * FROM monthly_expenses"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfDueDay: Int = getColumnIndexOrThrow(_stmt, "dueDay")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<MonthlyExpenseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MonthlyExpenseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpDueDay: Int
          _tmpDueDay = _stmt.getLong(_columnIndexOfDueDay).toInt()
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
          _item = MonthlyExpenseEntity(_tmpId,_tmpRemoteId,_tmpName,_tmpAmount,_tmpCategory,_tmpDueDay,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllBillInstances(): List<BillInstanceEntity> {
    val _sql: String = "SELECT * FROM bill_instances"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfBillId: Int = getColumnIndexOrThrow(_stmt, "billId")
        val _columnIndexOfMonth: Int = getColumnIndexOrThrow(_stmt, "month")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPaidAmount: Int = getColumnIndexOrThrow(_stmt, "paidAmount")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<BillInstanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BillInstanceEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpBillId: Long
          _tmpBillId = _stmt.getLong(_columnIndexOfBillId)
          val _tmpMonth: YearMonth
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfMonth)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfMonth)
          }
          val _tmp_1: YearMonth? = __converters.toYearMonth(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.YearMonth', but it was NULL.")
          } else {
            _tmpMonth = _tmp_1
          }
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPaidAmount: Double
          _tmpPaidAmount = _stmt.getDouble(_columnIndexOfPaidAmount)
          val _tmpStatus: BillStatus
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfStatus)
          _tmpStatus = __converters.toBillStatus(_tmp_2)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_3 != 0
          _item = BillInstanceEntity(_tmpId,_tmpRemoteId,_tmpBillId,_tmpMonth,_tmpAmount,_tmpPaidAmount,_tmpStatus,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBillInstanceById(id: Long) {
    val _sql: String = "DELETE FROM bill_instances WHERE id = ?"
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

  public override suspend fun markAsSyncedExpenses(remoteIds: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE monthly_expenses SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun markAsSyncedInstances(remoteIds: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE bill_instances SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun deleteAllMonthlyExpenses() {
    val _sql: String = "DELETE FROM monthly_expenses"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllBillInstances() {
    val _sql: String = "DELETE FROM bill_instances"
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
