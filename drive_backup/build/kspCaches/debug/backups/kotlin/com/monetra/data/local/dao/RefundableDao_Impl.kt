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
import com.monetra.`data`.local.Converters
import com.monetra.`data`.local.entity.RefundableEntity
import java.time.LocalDate
import java.time.LocalDateTime
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
public class RefundableDao_Impl(
  __db: RoomDatabase,
) : RefundableDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRefundableEntity: EntityInsertAdapter<RefundableEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfRefundableEntity: EntityDeleteOrUpdateAdapter<RefundableEntity>

  private val __upsertAdapterOfRefundableEntity: EntityUpsertAdapter<RefundableEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfRefundableEntity = object : EntityInsertAdapter<RefundableEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `refundable` (`id`,`remoteId`,`amount`,`personName`,`phoneNumber`,`givenDate`,`dueDate`,`note`,`isPaid`,`remindMe`,`entryType`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RefundableEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindDouble(3, entity.amount)
        statement.bindText(4, entity.personName)
        statement.bindText(5, entity.phoneNumber)
        val _tmp: String? = __converters.dateToTimestamp(entity.givenDate)
        if (_tmp == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmp)
        }
        val _tmp_1: String? = __converters.dateTimeToTimestamp(entity.dueDate)
        if (_tmp_1 == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmp_1)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpNote)
        }
        val _tmp_2: Int = if (entity.isPaid) 1 else 0
        statement.bindLong(9, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.remindMe) 1 else 0
        statement.bindLong(10, _tmp_3.toLong())
        statement.bindText(11, entity.entryType)
        statement.bindLong(12, entity.version)
        statement.bindLong(13, entity.updatedAt)
        statement.bindText(14, entity.deviceId)
        val _tmp_4: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(15, _tmp_4.toLong())
      }
    }
    this.__deleteAdapterOfRefundableEntity = object : EntityDeleteOrUpdateAdapter<RefundableEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `refundable` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: RefundableEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__upsertAdapterOfRefundableEntity = EntityUpsertAdapter<RefundableEntity>(object : EntityInsertAdapter<RefundableEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `refundable` (`id`,`remoteId`,`amount`,`personName`,`phoneNumber`,`givenDate`,`dueDate`,`note`,`isPaid`,`remindMe`,`entryType`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RefundableEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindDouble(3, entity.amount)
        statement.bindText(4, entity.personName)
        statement.bindText(5, entity.phoneNumber)
        val _tmp: String? = __converters.dateToTimestamp(entity.givenDate)
        if (_tmp == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmp)
        }
        val _tmp_1: String? = __converters.dateTimeToTimestamp(entity.dueDate)
        if (_tmp_1 == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmp_1)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpNote)
        }
        val _tmp_2: Int = if (entity.isPaid) 1 else 0
        statement.bindLong(9, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.remindMe) 1 else 0
        statement.bindLong(10, _tmp_3.toLong())
        statement.bindText(11, entity.entryType)
        statement.bindLong(12, entity.version)
        statement.bindLong(13, entity.updatedAt)
        statement.bindText(14, entity.deviceId)
        val _tmp_4: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(15, _tmp_4.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<RefundableEntity>() {
      protected override fun createQuery(): String = "UPDATE `refundable` SET `id` = ?,`remoteId` = ?,`amount` = ?,`personName` = ?,`phoneNumber` = ?,`givenDate` = ?,`dueDate` = ?,`note` = ?,`isPaid` = ?,`remindMe` = ?,`entryType` = ?,`version` = ?,`updatedAt` = ?,`deviceId` = ?,`isSynced` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: RefundableEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.remoteId)
        statement.bindDouble(3, entity.amount)
        statement.bindText(4, entity.personName)
        statement.bindText(5, entity.phoneNumber)
        val _tmp: String? = __converters.dateToTimestamp(entity.givenDate)
        if (_tmp == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmp)
        }
        val _tmp_1: String? = __converters.dateTimeToTimestamp(entity.dueDate)
        if (_tmp_1 == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmp_1)
        }
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpNote)
        }
        val _tmp_2: Int = if (entity.isPaid) 1 else 0
        statement.bindLong(9, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.remindMe) 1 else 0
        statement.bindLong(10, _tmp_3.toLong())
        statement.bindText(11, entity.entryType)
        statement.bindLong(12, entity.version)
        statement.bindLong(13, entity.updatedAt)
        statement.bindText(14, entity.deviceId)
        val _tmp_4: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(15, _tmp_4.toLong())
        statement.bindLong(16, entity.id)
      }
    })
  }

  public override suspend fun insertAllRefundables(refundables: List<RefundableEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRefundableEntity.insert(_connection, refundables)
  }

  public override suspend fun deleteRefundable(refundable: RefundableEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfRefundableEntity.handle(_connection, refundable)
  }

  public override suspend fun upsertRefundable(refundable: RefundableEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __upsertAdapterOfRefundableEntity.upsertAndReturnId(_connection, refundable)
    _result
  }

  public override fun getAllRefundables(): Flow<List<RefundableEntity>> {
    val _sql: String = "SELECT * FROM refundable ORDER BY dueDate ASC"
    return createFlow(__db, false, arrayOf("refundable")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfPhoneNumber: Int = getColumnIndexOrThrow(_stmt, "phoneNumber")
        val _columnIndexOfGivenDate: Int = getColumnIndexOrThrow(_stmt, "givenDate")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsPaid: Int = getColumnIndexOrThrow(_stmt, "isPaid")
        val _columnIndexOfRemindMe: Int = getColumnIndexOrThrow(_stmt, "remindMe")
        val _columnIndexOfEntryType: Int = getColumnIndexOrThrow(_stmt, "entryType")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<RefundableEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RefundableEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpPhoneNumber: String
          _tmpPhoneNumber = _stmt.getText(_columnIndexOfPhoneNumber)
          val _tmpGivenDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfGivenDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfGivenDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpGivenDate = _tmp_1
          }
          val _tmpDueDate: LocalDateTime
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfDueDate)
          }
          val _tmp_3: LocalDateTime? = __converters.fromDateTimeTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpDueDate = _tmp_3
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsPaid: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsPaid).toInt()
          _tmpIsPaid = _tmp_4 != 0
          val _tmpRemindMe: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfRemindMe).toInt()
          _tmpRemindMe = _tmp_5 != 0
          val _tmpEntryType: String
          _tmpEntryType = _stmt.getText(_columnIndexOfEntryType)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_6 != 0
          _item = RefundableEntity(_tmpId,_tmpRemoteId,_tmpAmount,_tmpPersonName,_tmpPhoneNumber,_tmpGivenDate,_tmpDueDate,_tmpNote,_tmpIsPaid,_tmpRemindMe,_tmpEntryType,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRefundableById(id: Long): RefundableEntity? {
    val _sql: String = "SELECT * FROM refundable WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfPhoneNumber: Int = getColumnIndexOrThrow(_stmt, "phoneNumber")
        val _columnIndexOfGivenDate: Int = getColumnIndexOrThrow(_stmt, "givenDate")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsPaid: Int = getColumnIndexOrThrow(_stmt, "isPaid")
        val _columnIndexOfRemindMe: Int = getColumnIndexOrThrow(_stmt, "remindMe")
        val _columnIndexOfEntryType: Int = getColumnIndexOrThrow(_stmt, "entryType")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: RefundableEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpPhoneNumber: String
          _tmpPhoneNumber = _stmt.getText(_columnIndexOfPhoneNumber)
          val _tmpGivenDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfGivenDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfGivenDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpGivenDate = _tmp_1
          }
          val _tmpDueDate: LocalDateTime
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfDueDate)
          }
          val _tmp_3: LocalDateTime? = __converters.fromDateTimeTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpDueDate = _tmp_3
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsPaid: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsPaid).toInt()
          _tmpIsPaid = _tmp_4 != 0
          val _tmpRemindMe: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfRemindMe).toInt()
          _tmpRemindMe = _tmp_5 != 0
          val _tmpEntryType: String
          _tmpEntryType = _stmt.getText(_columnIndexOfEntryType)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_6 != 0
          _result = RefundableEntity(_tmpId,_tmpRemoteId,_tmpAmount,_tmpPersonName,_tmpPhoneNumber,_tmpGivenDate,_tmpDueDate,_tmpNote,_tmpIsPaid,_tmpRemindMe,_tmpEntryType,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getRefundableFlowById(id: Long): Flow<RefundableEntity?> {
    val _sql: String = "SELECT * FROM refundable WHERE id = ?"
    return createFlow(__db, false, arrayOf("refundable")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfPhoneNumber: Int = getColumnIndexOrThrow(_stmt, "phoneNumber")
        val _columnIndexOfGivenDate: Int = getColumnIndexOrThrow(_stmt, "givenDate")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsPaid: Int = getColumnIndexOrThrow(_stmt, "isPaid")
        val _columnIndexOfRemindMe: Int = getColumnIndexOrThrow(_stmt, "remindMe")
        val _columnIndexOfEntryType: Int = getColumnIndexOrThrow(_stmt, "entryType")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: RefundableEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpPhoneNumber: String
          _tmpPhoneNumber = _stmt.getText(_columnIndexOfPhoneNumber)
          val _tmpGivenDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfGivenDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfGivenDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpGivenDate = _tmp_1
          }
          val _tmpDueDate: LocalDateTime
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfDueDate)
          }
          val _tmp_3: LocalDateTime? = __converters.fromDateTimeTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpDueDate = _tmp_3
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsPaid: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsPaid).toInt()
          _tmpIsPaid = _tmp_4 != 0
          val _tmpRemindMe: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfRemindMe).toInt()
          _tmpRemindMe = _tmp_5 != 0
          val _tmpEntryType: String
          _tmpEntryType = _stmt.getText(_columnIndexOfEntryType)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_6 != 0
          _result = RefundableEntity(_tmpId,_tmpRemoteId,_tmpAmount,_tmpPersonName,_tmpPhoneNumber,_tmpGivenDate,_tmpDueDate,_tmpNote,_tmpIsPaid,_tmpRemindMe,_tmpEntryType,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedRefundables(): List<RefundableEntity> {
    val _sql: String = "SELECT * FROM refundable WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfPhoneNumber: Int = getColumnIndexOrThrow(_stmt, "phoneNumber")
        val _columnIndexOfGivenDate: Int = getColumnIndexOrThrow(_stmt, "givenDate")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsPaid: Int = getColumnIndexOrThrow(_stmt, "isPaid")
        val _columnIndexOfRemindMe: Int = getColumnIndexOrThrow(_stmt, "remindMe")
        val _columnIndexOfEntryType: Int = getColumnIndexOrThrow(_stmt, "entryType")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<RefundableEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RefundableEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpPhoneNumber: String
          _tmpPhoneNumber = _stmt.getText(_columnIndexOfPhoneNumber)
          val _tmpGivenDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfGivenDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfGivenDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpGivenDate = _tmp_1
          }
          val _tmpDueDate: LocalDateTime
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfDueDate)
          }
          val _tmp_3: LocalDateTime? = __converters.fromDateTimeTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpDueDate = _tmp_3
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsPaid: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsPaid).toInt()
          _tmpIsPaid = _tmp_4 != 0
          val _tmpRemindMe: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfRemindMe).toInt()
          _tmpRemindMe = _tmp_5 != 0
          val _tmpEntryType: String
          _tmpEntryType = _stmt.getText(_columnIndexOfEntryType)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_6 != 0
          _item = RefundableEntity(_tmpId,_tmpRemoteId,_tmpAmount,_tmpPersonName,_tmpPhoneNumber,_tmpGivenDate,_tmpDueDate,_tmpNote,_tmpIsPaid,_tmpRemindMe,_tmpEntryType,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRefundableByRemoteId(remoteId: String): RefundableEntity? {
    val _sql: String = "SELECT * FROM refundable WHERE remoteId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, remoteId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfPhoneNumber: Int = getColumnIndexOrThrow(_stmt, "phoneNumber")
        val _columnIndexOfGivenDate: Int = getColumnIndexOrThrow(_stmt, "givenDate")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsPaid: Int = getColumnIndexOrThrow(_stmt, "isPaid")
        val _columnIndexOfRemindMe: Int = getColumnIndexOrThrow(_stmt, "remindMe")
        val _columnIndexOfEntryType: Int = getColumnIndexOrThrow(_stmt, "entryType")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: RefundableEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpPhoneNumber: String
          _tmpPhoneNumber = _stmt.getText(_columnIndexOfPhoneNumber)
          val _tmpGivenDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfGivenDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfGivenDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpGivenDate = _tmp_1
          }
          val _tmpDueDate: LocalDateTime
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfDueDate)
          }
          val _tmp_3: LocalDateTime? = __converters.fromDateTimeTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpDueDate = _tmp_3
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsPaid: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsPaid).toInt()
          _tmpIsPaid = _tmp_4 != 0
          val _tmpRemindMe: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfRemindMe).toInt()
          _tmpRemindMe = _tmp_5 != 0
          val _tmpEntryType: String
          _tmpEntryType = _stmt.getText(_columnIndexOfEntryType)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_6 != 0
          _result = RefundableEntity(_tmpId,_tmpRemoteId,_tmpAmount,_tmpPersonName,_tmpPhoneNumber,_tmpGivenDate,_tmpDueDate,_tmpNote,_tmpIsPaid,_tmpRemindMe,_tmpEntryType,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllRefundablesList(): List<RefundableEntity> {
    val _sql: String = "SELECT * FROM refundable"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfPersonName: Int = getColumnIndexOrThrow(_stmt, "personName")
        val _columnIndexOfPhoneNumber: Int = getColumnIndexOrThrow(_stmt, "phoneNumber")
        val _columnIndexOfGivenDate: Int = getColumnIndexOrThrow(_stmt, "givenDate")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfIsPaid: Int = getColumnIndexOrThrow(_stmt, "isPaid")
        val _columnIndexOfRemindMe: Int = getColumnIndexOrThrow(_stmt, "remindMe")
        val _columnIndexOfEntryType: Int = getColumnIndexOrThrow(_stmt, "entryType")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<RefundableEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RefundableEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpPersonName: String
          _tmpPersonName = _stmt.getText(_columnIndexOfPersonName)
          val _tmpPhoneNumber: String
          _tmpPhoneNumber = _stmt.getText(_columnIndexOfPhoneNumber)
          val _tmpGivenDate: LocalDate
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfGivenDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfGivenDate)
          }
          val _tmp_1: LocalDate? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDate', but it was NULL.")
          } else {
            _tmpGivenDate = _tmp_1
          }
          val _tmpDueDate: LocalDateTime
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfDueDate)
          }
          val _tmp_3: LocalDateTime? = __converters.fromDateTimeTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpDueDate = _tmp_3
          }
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpIsPaid: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsPaid).toInt()
          _tmpIsPaid = _tmp_4 != 0
          val _tmpRemindMe: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfRemindMe).toInt()
          _tmpRemindMe = _tmp_5 != 0
          val _tmpEntryType: String
          _tmpEntryType = _stmt.getText(_columnIndexOfEntryType)
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_6: Int
          _tmp_6 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_6 != 0
          _item = RefundableEntity(_tmpId,_tmpRemoteId,_tmpAmount,_tmpPersonName,_tmpPhoneNumber,_tmpGivenDate,_tmpDueDate,_tmpNote,_tmpIsPaid,_tmpRemindMe,_tmpEntryType,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePaidStatus(id: Long, isPaid: Boolean) {
    val _sql: String = "UPDATE refundable SET isPaid = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (isPaid) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsSynced(remoteIds: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("UPDATE refundable SET isSynced = 1 WHERE remoteId IN (")
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

  public override suspend fun deleteAllRefundables() {
    val _sql: String = "DELETE FROM refundable"
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
