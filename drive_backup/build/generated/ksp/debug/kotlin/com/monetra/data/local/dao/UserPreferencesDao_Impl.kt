package com.monetra.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.monetra.`data`.local.entity.UserPreferencesEntity
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserPreferencesDao_Impl(
  __db: RoomDatabase,
) : UserPreferencesDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUserPreferencesEntity: EntityInsertAdapter<UserPreferencesEntity>

  private val __upsertAdapterOfUserPreferencesEntity: EntityUpsertAdapter<UserPreferencesEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfUserPreferencesEntity = object : EntityInsertAdapter<UserPreferencesEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `user_preferences` (`id`,`remoteId`,`ownerName`,`monthlyIncome`,`monthlySavingsGoal`,`currentSavings`,`isOnboardingCompleted`,`projectionRate`,`projectionYears`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserPreferencesEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.ownerName)
        statement.bindDouble(4, entity.monthlyIncome)
        statement.bindDouble(5, entity.monthlySavingsGoal)
        statement.bindDouble(6, entity.currentSavings)
        val _tmp: Int = if (entity.isOnboardingCompleted) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindDouble(8, entity.projectionRate)
        statement.bindLong(9, entity.projectionYears.toLong())
        statement.bindLong(10, entity.version)
        statement.bindLong(11, entity.updatedAt)
        statement.bindText(12, entity.deviceId)
        val _tmp_1: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(13, _tmp_1.toLong())
      }
    }
    this.__upsertAdapterOfUserPreferencesEntity = EntityUpsertAdapter<UserPreferencesEntity>(object : EntityInsertAdapter<UserPreferencesEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `user_preferences` (`id`,`remoteId`,`ownerName`,`monthlyIncome`,`monthlySavingsGoal`,`currentSavings`,`isOnboardingCompleted`,`projectionRate`,`projectionYears`,`version`,`updatedAt`,`deviceId`,`isSynced`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserPreferencesEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.ownerName)
        statement.bindDouble(4, entity.monthlyIncome)
        statement.bindDouble(5, entity.monthlySavingsGoal)
        statement.bindDouble(6, entity.currentSavings)
        val _tmp: Int = if (entity.isOnboardingCompleted) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindDouble(8, entity.projectionRate)
        statement.bindLong(9, entity.projectionYears.toLong())
        statement.bindLong(10, entity.version)
        statement.bindLong(11, entity.updatedAt)
        statement.bindText(12, entity.deviceId)
        val _tmp_1: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(13, _tmp_1.toLong())
      }
    }, object : EntityDeleteOrUpdateAdapter<UserPreferencesEntity>() {
      protected override fun createQuery(): String = "UPDATE `user_preferences` SET `id` = ?,`remoteId` = ?,`ownerName` = ?,`monthlyIncome` = ?,`monthlySavingsGoal` = ?,`currentSavings` = ?,`isOnboardingCompleted` = ?,`projectionRate` = ?,`projectionYears` = ?,`version` = ?,`updatedAt` = ?,`deviceId` = ?,`isSynced` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: UserPreferencesEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.remoteId)
        statement.bindText(3, entity.ownerName)
        statement.bindDouble(4, entity.monthlyIncome)
        statement.bindDouble(5, entity.monthlySavingsGoal)
        statement.bindDouble(6, entity.currentSavings)
        val _tmp: Int = if (entity.isOnboardingCompleted) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindDouble(8, entity.projectionRate)
        statement.bindLong(9, entity.projectionYears.toLong())
        statement.bindLong(10, entity.version)
        statement.bindLong(11, entity.updatedAt)
        statement.bindText(12, entity.deviceId)
        val _tmp_1: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(13, _tmp_1.toLong())
        statement.bindLong(14, entity.id.toLong())
      }
    })
  }

  public override suspend fun insertAllUserPreferences(prefs: List<UserPreferencesEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUserPreferencesEntity.insert(_connection, prefs)
  }

  public override suspend fun upsertUserPreferences(preferences: UserPreferencesEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfUserPreferencesEntity.upsert(_connection, preferences)
  }

  public override fun getUserPreferences(): Flow<UserPreferencesEntity?> {
    val _sql: String = "SELECT * FROM user_preferences WHERE id = 0"
    return createFlow(__db, false, arrayOf("user_preferences")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfOwnerName: Int = getColumnIndexOrThrow(_stmt, "ownerName")
        val _columnIndexOfMonthlyIncome: Int = getColumnIndexOrThrow(_stmt, "monthlyIncome")
        val _columnIndexOfMonthlySavingsGoal: Int = getColumnIndexOrThrow(_stmt, "monthlySavingsGoal")
        val _columnIndexOfCurrentSavings: Int = getColumnIndexOrThrow(_stmt, "currentSavings")
        val _columnIndexOfIsOnboardingCompleted: Int = getColumnIndexOrThrow(_stmt, "isOnboardingCompleted")
        val _columnIndexOfProjectionRate: Int = getColumnIndexOrThrow(_stmt, "projectionRate")
        val _columnIndexOfProjectionYears: Int = getColumnIndexOrThrow(_stmt, "projectionYears")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: UserPreferencesEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpOwnerName: String
          _tmpOwnerName = _stmt.getText(_columnIndexOfOwnerName)
          val _tmpMonthlyIncome: Double
          _tmpMonthlyIncome = _stmt.getDouble(_columnIndexOfMonthlyIncome)
          val _tmpMonthlySavingsGoal: Double
          _tmpMonthlySavingsGoal = _stmt.getDouble(_columnIndexOfMonthlySavingsGoal)
          val _tmpCurrentSavings: Double
          _tmpCurrentSavings = _stmt.getDouble(_columnIndexOfCurrentSavings)
          val _tmpIsOnboardingCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOnboardingCompleted).toInt()
          _tmpIsOnboardingCompleted = _tmp != 0
          val _tmpProjectionRate: Double
          _tmpProjectionRate = _stmt.getDouble(_columnIndexOfProjectionRate)
          val _tmpProjectionYears: Int
          _tmpProjectionYears = _stmt.getLong(_columnIndexOfProjectionYears).toInt()
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_1 != 0
          _result = UserPreferencesEntity(_tmpId,_tmpRemoteId,_tmpOwnerName,_tmpMonthlyIncome,_tmpMonthlySavingsGoal,_tmpCurrentSavings,_tmpIsOnboardingCompleted,_tmpProjectionRate,_tmpProjectionYears,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllUserPreferences(): List<UserPreferencesEntity> {
    val _sql: String = "SELECT * FROM user_preferences"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfOwnerName: Int = getColumnIndexOrThrow(_stmt, "ownerName")
        val _columnIndexOfMonthlyIncome: Int = getColumnIndexOrThrow(_stmt, "monthlyIncome")
        val _columnIndexOfMonthlySavingsGoal: Int = getColumnIndexOrThrow(_stmt, "monthlySavingsGoal")
        val _columnIndexOfCurrentSavings: Int = getColumnIndexOrThrow(_stmt, "currentSavings")
        val _columnIndexOfIsOnboardingCompleted: Int = getColumnIndexOrThrow(_stmt, "isOnboardingCompleted")
        val _columnIndexOfProjectionRate: Int = getColumnIndexOrThrow(_stmt, "projectionRate")
        val _columnIndexOfProjectionYears: Int = getColumnIndexOrThrow(_stmt, "projectionYears")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<UserPreferencesEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: UserPreferencesEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpOwnerName: String
          _tmpOwnerName = _stmt.getText(_columnIndexOfOwnerName)
          val _tmpMonthlyIncome: Double
          _tmpMonthlyIncome = _stmt.getDouble(_columnIndexOfMonthlyIncome)
          val _tmpMonthlySavingsGoal: Double
          _tmpMonthlySavingsGoal = _stmt.getDouble(_columnIndexOfMonthlySavingsGoal)
          val _tmpCurrentSavings: Double
          _tmpCurrentSavings = _stmt.getDouble(_columnIndexOfCurrentSavings)
          val _tmpIsOnboardingCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOnboardingCompleted).toInt()
          _tmpIsOnboardingCompleted = _tmp != 0
          val _tmpProjectionRate: Double
          _tmpProjectionRate = _stmt.getDouble(_columnIndexOfProjectionRate)
          val _tmpProjectionYears: Int
          _tmpProjectionYears = _stmt.getLong(_columnIndexOfProjectionYears).toInt()
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_1 != 0
          _item = UserPreferencesEntity(_tmpId,_tmpRemoteId,_tmpOwnerName,_tmpMonthlyIncome,_tmpMonthlySavingsGoal,_tmpCurrentSavings,_tmpIsOnboardingCompleted,_tmpProjectionRate,_tmpProjectionYears,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUnsyncedPreferences(): List<UserPreferencesEntity> {
    val _sql: String = "SELECT * FROM user_preferences WHERE isSynced = 0"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRemoteId: Int = getColumnIndexOrThrow(_stmt, "remoteId")
        val _columnIndexOfOwnerName: Int = getColumnIndexOrThrow(_stmt, "ownerName")
        val _columnIndexOfMonthlyIncome: Int = getColumnIndexOrThrow(_stmt, "monthlyIncome")
        val _columnIndexOfMonthlySavingsGoal: Int = getColumnIndexOrThrow(_stmt, "monthlySavingsGoal")
        val _columnIndexOfCurrentSavings: Int = getColumnIndexOrThrow(_stmt, "currentSavings")
        val _columnIndexOfIsOnboardingCompleted: Int = getColumnIndexOrThrow(_stmt, "isOnboardingCompleted")
        val _columnIndexOfProjectionRate: Int = getColumnIndexOrThrow(_stmt, "projectionRate")
        val _columnIndexOfProjectionYears: Int = getColumnIndexOrThrow(_stmt, "projectionYears")
        val _columnIndexOfVersion: Int = getColumnIndexOrThrow(_stmt, "version")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfDeviceId: Int = getColumnIndexOrThrow(_stmt, "deviceId")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _result: MutableList<UserPreferencesEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: UserPreferencesEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpRemoteId: String
          _tmpRemoteId = _stmt.getText(_columnIndexOfRemoteId)
          val _tmpOwnerName: String
          _tmpOwnerName = _stmt.getText(_columnIndexOfOwnerName)
          val _tmpMonthlyIncome: Double
          _tmpMonthlyIncome = _stmt.getDouble(_columnIndexOfMonthlyIncome)
          val _tmpMonthlySavingsGoal: Double
          _tmpMonthlySavingsGoal = _stmt.getDouble(_columnIndexOfMonthlySavingsGoal)
          val _tmpCurrentSavings: Double
          _tmpCurrentSavings = _stmt.getDouble(_columnIndexOfCurrentSavings)
          val _tmpIsOnboardingCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsOnboardingCompleted).toInt()
          _tmpIsOnboardingCompleted = _tmp != 0
          val _tmpProjectionRate: Double
          _tmpProjectionRate = _stmt.getDouble(_columnIndexOfProjectionRate)
          val _tmpProjectionYears: Int
          _tmpProjectionYears = _stmt.getLong(_columnIndexOfProjectionYears).toInt()
          val _tmpVersion: Long
          _tmpVersion = _stmt.getLong(_columnIndexOfVersion)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpDeviceId: String
          _tmpDeviceId = _stmt.getText(_columnIndexOfDeviceId)
          val _tmpIsSynced: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp_1 != 0
          _item = UserPreferencesEntity(_tmpId,_tmpRemoteId,_tmpOwnerName,_tmpMonthlyIncome,_tmpMonthlySavingsGoal,_tmpCurrentSavings,_tmpIsOnboardingCompleted,_tmpProjectionRate,_tmpProjectionYears,_tmpVersion,_tmpUpdatedAt,_tmpDeviceId,_tmpIsSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsSynced() {
    val _sql: String = "UPDATE user_preferences SET isSynced = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllUserPreferences() {
    val _sql: String = "DELETE FROM user_preferences"
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
