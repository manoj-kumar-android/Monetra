package com.monetra.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.monetra.`data`.local.dao.CategoryBudgetDao
import com.monetra.`data`.local.dao.CategoryBudgetDao_Impl
import com.monetra.`data`.local.dao.DeletedEntityDao
import com.monetra.`data`.local.dao.DeletedEntityDao_Impl
import com.monetra.`data`.local.dao.GoalDao
import com.monetra.`data`.local.dao.GoalDao_Impl
import com.monetra.`data`.local.dao.InvestmentDao
import com.monetra.`data`.local.dao.InvestmentDao_Impl
import com.monetra.`data`.local.dao.LoanDao
import com.monetra.`data`.local.dao.LoanDao_Impl
import com.monetra.`data`.local.dao.MonthlyExpenseDao
import com.monetra.`data`.local.dao.MonthlyExpenseDao_Impl
import com.monetra.`data`.local.dao.MonthlyReportDao
import com.monetra.`data`.local.dao.MonthlyReportDao_Impl
import com.monetra.`data`.local.dao.PendingDeleteDao
import com.monetra.`data`.local.dao.PendingDeleteDao_Impl
import com.monetra.`data`.local.dao.RefundableDao
import com.monetra.`data`.local.dao.RefundableDao_Impl
import com.monetra.`data`.local.dao.SavingDao
import com.monetra.`data`.local.dao.SavingDao_Impl
import com.monetra.`data`.local.dao.TransactionDao
import com.monetra.`data`.local.dao.TransactionDao_Impl
import com.monetra.`data`.local.dao.UserPreferencesDao
import com.monetra.`data`.local.dao.UserPreferencesDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MonetraDatabase_Impl : MonetraDatabase() {
  private val _transactionDao: Lazy<TransactionDao> = lazy {
    TransactionDao_Impl(this)
  }

  public override val transactionDao: TransactionDao
    get() = _transactionDao.value

  private val _userPreferencesDao: Lazy<UserPreferencesDao> = lazy {
    UserPreferencesDao_Impl(this)
  }

  public override val userPreferencesDao: UserPreferencesDao
    get() = _userPreferencesDao.value

  private val _categoryBudgetDao: Lazy<CategoryBudgetDao> = lazy {
    CategoryBudgetDao_Impl(this)
  }

  public override val categoryBudgetDao: CategoryBudgetDao
    get() = _categoryBudgetDao.value

  private val _goalDao: Lazy<GoalDao> = lazy {
    GoalDao_Impl(this)
  }

  public override val goalDao: GoalDao
    get() = _goalDao.value

  private val _investmentDao: Lazy<InvestmentDao> = lazy {
    InvestmentDao_Impl(this)
  }

  public override val investmentDao: InvestmentDao
    get() = _investmentDao.value

  private val _monthlyReportDao: Lazy<MonthlyReportDao> = lazy {
    MonthlyReportDao_Impl(this)
  }

  public override val monthlyReportDao: MonthlyReportDao
    get() = _monthlyReportDao.value

  private val _loanDao: Lazy<LoanDao> = lazy {
    LoanDao_Impl(this)
  }

  public override val loanDao: LoanDao
    get() = _loanDao.value

  private val _monthlyExpenseDao: Lazy<MonthlyExpenseDao> = lazy {
    MonthlyExpenseDao_Impl(this)
  }

  public override val monthlyExpenseDao: MonthlyExpenseDao
    get() = _monthlyExpenseDao.value

  private val _refundableDao: Lazy<RefundableDao> = lazy {
    RefundableDao_Impl(this)
  }

  public override val refundableDao: RefundableDao
    get() = _refundableDao.value

  private val _savingDao: Lazy<SavingDao> = lazy {
    SavingDao_Impl(this)
  }

  public override val savingDao: SavingDao
    get() = _savingDao.value

  private val _deletedEntityDao: Lazy<DeletedEntityDao> = lazy {
    DeletedEntityDao_Impl(this)
  }

  public override val deletedEntityDao: DeletedEntityDao
    get() = _deletedEntityDao.value

  private val _pendingDeleteDao: Lazy<PendingDeleteDao> = lazy {
    PendingDeleteDao_Impl(this)
  }

  public override val pendingDeleteDao: PendingDeleteDao
    get() = _pendingDeleteDao.value

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(6, "013d5a5eb1303c11e33706cadea68c54", "89d85c35e52807cca3bc86627d119f36") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` TEXT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `date` TEXT NOT NULL, `note` TEXT NOT NULL, `linkedBillId` INTEGER, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_date_type` ON `transactions` (`date`, `type`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_remoteId` ON `transactions` (`remoteId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_preferences` (`id` INTEGER NOT NULL, `remoteId` TEXT NOT NULL, `ownerName` TEXT NOT NULL, `monthlyIncome` REAL NOT NULL, `monthlySavingsGoal` REAL NOT NULL, `currentSavings` REAL NOT NULL, `isOnboardingCompleted` INTEGER NOT NULL, `projectionRate` REAL NOT NULL, `projectionYears` INTEGER NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `category_budgets` (`categoryName` TEXT NOT NULL, `remoteId` TEXT NOT NULL, `limit` REAL NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL, PRIMARY KEY(`categoryName`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` TEXT NOT NULL, `title` TEXT NOT NULL, `targetAmount` REAL NOT NULL, `currentAmount` REAL NOT NULL, `deadline` TEXT, `category` TEXT NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `investments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` TEXT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT, `amount` REAL NOT NULL, `monthlyAmount` REAL NOT NULL, `interestRate` REAL NOT NULL, `currentValue` REAL NOT NULL, `frequency` TEXT NOT NULL, `stepChanges` TEXT NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `monthly_reports` (`month` TEXT NOT NULL, `remoteId` TEXT NOT NULL, `income` REAL NOT NULL, `expenses` REAL NOT NULL, `emis` REAL NOT NULL, `investments` REAL NOT NULL, `actualSavings` REAL NOT NULL, `targetSavings` REAL NOT NULL, `status` TEXT NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL, PRIMARY KEY(`month`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `loans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` TEXT NOT NULL, `name` TEXT NOT NULL, `totalPrincipal` REAL NOT NULL, `annualInterestRate` REAL NOT NULL, `monthlyEmi` REAL NOT NULL, `startDate` TEXT NOT NULL, `tenureMonths` INTEGER NOT NULL, `remainingTenure` INTEGER NOT NULL, `category` TEXT NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `monthly_expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` TEXT NOT NULL, `name` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, `dueDay` INTEGER NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `bill_instances` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` TEXT NOT NULL, `billId` INTEGER NOT NULL, `month` TEXT NOT NULL, `amount` REAL NOT NULL, `paidAmount` REAL NOT NULL, `status` TEXT NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL, FOREIGN KEY(`billId`) REFERENCES `monthly_expenses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_bill_instances_billId_month` ON `bill_instances` (`billId`, `month`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `refundable` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` TEXT NOT NULL, `amount` REAL NOT NULL, `personName` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `givenDate` TEXT NOT NULL, `dueDate` TEXT NOT NULL, `note` TEXT, `isPaid` INTEGER NOT NULL, `remindMe` INTEGER NOT NULL, `entryType` TEXT NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `savings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` TEXT NOT NULL, `bankName` TEXT NOT NULL, `amount` REAL NOT NULL, `interestRate` REAL, `note` TEXT NOT NULL, `version` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `deviceId` TEXT NOT NULL, `isSynced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `deleted_entities` (`remoteId` TEXT NOT NULL, `entityType` TEXT NOT NULL, `deletedAt` INTEGER NOT NULL, PRIMARY KEY(`remoteId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `pending_deletes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityId` INTEGER NOT NULL, `remoteId` TEXT NOT NULL, `entityType` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '013d5a5eb1303c11e33706cadea68c54')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `transactions`")
        connection.execSQL("DROP TABLE IF EXISTS `user_preferences`")
        connection.execSQL("DROP TABLE IF EXISTS `category_budgets`")
        connection.execSQL("DROP TABLE IF EXISTS `goals`")
        connection.execSQL("DROP TABLE IF EXISTS `investments`")
        connection.execSQL("DROP TABLE IF EXISTS `monthly_reports`")
        connection.execSQL("DROP TABLE IF EXISTS `loans`")
        connection.execSQL("DROP TABLE IF EXISTS `monthly_expenses`")
        connection.execSQL("DROP TABLE IF EXISTS `bill_instances`")
        connection.execSQL("DROP TABLE IF EXISTS `refundable`")
        connection.execSQL("DROP TABLE IF EXISTS `savings`")
        connection.execSQL("DROP TABLE IF EXISTS `deleted_entities`")
        connection.execSQL("DROP TABLE IF EXISTS `pending_deletes`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsTransactions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTransactions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("amount", TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("date", TableInfo.Column("date", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("linkedBillId", TableInfo.Column("linkedBillId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTransactions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTransactions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTransactions.add(TableInfo.Index("index_transactions_date_type", false, listOf("date", "type"), listOf("ASC", "ASC")))
        _indicesTransactions.add(TableInfo.Index("index_transactions_remoteId", true, listOf("remoteId"), listOf("ASC")))
        val _infoTransactions: TableInfo = TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions)
        val _existingTransactions: TableInfo = read(connection, "transactions")
        if (!_infoTransactions.equals(_existingTransactions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |transactions(com.monetra.data.local.entity.TransactionEntity).
              | Expected:
              |""".trimMargin() + _infoTransactions + """
              |
              | Found:
              |""".trimMargin() + _existingTransactions)
        }
        val _columnsUserPreferences: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUserPreferences.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("ownerName", TableInfo.Column("ownerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("monthlyIncome", TableInfo.Column("monthlyIncome", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("monthlySavingsGoal", TableInfo.Column("monthlySavingsGoal", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("currentSavings", TableInfo.Column("currentSavings", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("isOnboardingCompleted", TableInfo.Column("isOnboardingCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("projectionRate", TableInfo.Column("projectionRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("projectionYears", TableInfo.Column("projectionYears", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserPreferences.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserPreferences: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUserPreferences: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUserPreferences: TableInfo = TableInfo("user_preferences", _columnsUserPreferences, _foreignKeysUserPreferences, _indicesUserPreferences)
        val _existingUserPreferences: TableInfo = read(connection, "user_preferences")
        if (!_infoUserPreferences.equals(_existingUserPreferences)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |user_preferences(com.monetra.data.local.entity.UserPreferencesEntity).
              | Expected:
              |""".trimMargin() + _infoUserPreferences + """
              |
              | Found:
              |""".trimMargin() + _existingUserPreferences)
        }
        val _columnsCategoryBudgets: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategoryBudgets.put("categoryName", TableInfo.Column("categoryName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategoryBudgets.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategoryBudgets.put("limit", TableInfo.Column("limit", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategoryBudgets.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategoryBudgets.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategoryBudgets.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategoryBudgets.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategoryBudgets: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategoryBudgets: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategoryBudgets: TableInfo = TableInfo("category_budgets", _columnsCategoryBudgets, _foreignKeysCategoryBudgets, _indicesCategoryBudgets)
        val _existingCategoryBudgets: TableInfo = read(connection, "category_budgets")
        if (!_infoCategoryBudgets.equals(_existingCategoryBudgets)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |category_budgets(com.monetra.data.local.entity.CategoryBudgetEntity).
              | Expected:
              |""".trimMargin() + _infoCategoryBudgets + """
              |
              | Found:
              |""".trimMargin() + _existingCategoryBudgets)
        }
        val _columnsGoals: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGoals.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("targetAmount", TableInfo.Column("targetAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("currentAmount", TableInfo.Column("currentAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("deadline", TableInfo.Column("deadline", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGoals: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGoals: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoGoals: TableInfo = TableInfo("goals", _columnsGoals, _foreignKeysGoals, _indicesGoals)
        val _existingGoals: TableInfo = read(connection, "goals")
        if (!_infoGoals.equals(_existingGoals)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |goals(com.monetra.data.local.entity.GoalEntity).
              | Expected:
              |""".trimMargin() + _infoGoals + """
              |
              | Found:
              |""".trimMargin() + _existingGoals)
        }
        val _columnsInvestments: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsInvestments.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("startDate", TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("endDate", TableInfo.Column("endDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("amount", TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("monthlyAmount", TableInfo.Column("monthlyAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("interestRate", TableInfo.Column("interestRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("currentValue", TableInfo.Column("currentValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("frequency", TableInfo.Column("frequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("stepChanges", TableInfo.Column("stepChanges", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsInvestments.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysInvestments: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesInvestments: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoInvestments: TableInfo = TableInfo("investments", _columnsInvestments, _foreignKeysInvestments, _indicesInvestments)
        val _existingInvestments: TableInfo = read(connection, "investments")
        if (!_infoInvestments.equals(_existingInvestments)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |investments(com.monetra.data.local.entity.InvestmentEntity).
              | Expected:
              |""".trimMargin() + _infoInvestments + """
              |
              | Found:
              |""".trimMargin() + _existingInvestments)
        }
        val _columnsMonthlyReports: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMonthlyReports.put("month", TableInfo.Column("month", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("income", TableInfo.Column("income", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("expenses", TableInfo.Column("expenses", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("emis", TableInfo.Column("emis", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("investments", TableInfo.Column("investments", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("actualSavings", TableInfo.Column("actualSavings", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("targetSavings", TableInfo.Column("targetSavings", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyReports.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMonthlyReports: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMonthlyReports: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMonthlyReports: TableInfo = TableInfo("monthly_reports", _columnsMonthlyReports, _foreignKeysMonthlyReports, _indicesMonthlyReports)
        val _existingMonthlyReports: TableInfo = read(connection, "monthly_reports")
        if (!_infoMonthlyReports.equals(_existingMonthlyReports)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |monthly_reports(com.monetra.data.local.entity.MonthlyReportEntity).
              | Expected:
              |""".trimMargin() + _infoMonthlyReports + """
              |
              | Found:
              |""".trimMargin() + _existingMonthlyReports)
        }
        val _columnsLoans: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLoans.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("totalPrincipal", TableInfo.Column("totalPrincipal", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("annualInterestRate", TableInfo.Column("annualInterestRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("monthlyEmi", TableInfo.Column("monthlyEmi", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("startDate", TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("tenureMonths", TableInfo.Column("tenureMonths", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("remainingTenure", TableInfo.Column("remainingTenure", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLoans.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLoans: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLoans: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoLoans: TableInfo = TableInfo("loans", _columnsLoans, _foreignKeysLoans, _indicesLoans)
        val _existingLoans: TableInfo = read(connection, "loans")
        if (!_infoLoans.equals(_existingLoans)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |loans(com.monetra.data.local.entity.LoanEntity).
              | Expected:
              |""".trimMargin() + _infoLoans + """
              |
              | Found:
              |""".trimMargin() + _existingLoans)
        }
        val _columnsMonthlyExpenses: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMonthlyExpenses.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("amount", TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("dueDay", TableInfo.Column("dueDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMonthlyExpenses.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMonthlyExpenses: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMonthlyExpenses: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMonthlyExpenses: TableInfo = TableInfo("monthly_expenses", _columnsMonthlyExpenses, _foreignKeysMonthlyExpenses, _indicesMonthlyExpenses)
        val _existingMonthlyExpenses: TableInfo = read(connection, "monthly_expenses")
        if (!_infoMonthlyExpenses.equals(_existingMonthlyExpenses)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |monthly_expenses(com.monetra.data.local.entity.MonthlyExpenseEntity).
              | Expected:
              |""".trimMargin() + _infoMonthlyExpenses + """
              |
              | Found:
              |""".trimMargin() + _existingMonthlyExpenses)
        }
        val _columnsBillInstances: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBillInstances.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("billId", TableInfo.Column("billId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("month", TableInfo.Column("month", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("amount", TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("paidAmount", TableInfo.Column("paidAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBillInstances.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBillInstances: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysBillInstances.add(TableInfo.ForeignKey("monthly_expenses", "CASCADE", "NO ACTION", listOf("billId"), listOf("id")))
        val _indicesBillInstances: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesBillInstances.add(TableInfo.Index("index_bill_instances_billId_month", true, listOf("billId", "month"), listOf("ASC", "ASC")))
        val _infoBillInstances: TableInfo = TableInfo("bill_instances", _columnsBillInstances, _foreignKeysBillInstances, _indicesBillInstances)
        val _existingBillInstances: TableInfo = read(connection, "bill_instances")
        if (!_infoBillInstances.equals(_existingBillInstances)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |bill_instances(com.monetra.data.local.entity.BillInstanceEntity).
              | Expected:
              |""".trimMargin() + _infoBillInstances + """
              |
              | Found:
              |""".trimMargin() + _existingBillInstances)
        }
        val _columnsRefundable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRefundable.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("amount", TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("personName", TableInfo.Column("personName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("phoneNumber", TableInfo.Column("phoneNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("givenDate", TableInfo.Column("givenDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("dueDate", TableInfo.Column("dueDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("note", TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("isPaid", TableInfo.Column("isPaid", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("remindMe", TableInfo.Column("remindMe", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("entryType", TableInfo.Column("entryType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRefundable.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRefundable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRefundable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoRefundable: TableInfo = TableInfo("refundable", _columnsRefundable, _foreignKeysRefundable, _indicesRefundable)
        val _existingRefundable: TableInfo = read(connection, "refundable")
        if (!_infoRefundable.equals(_existingRefundable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |refundable(com.monetra.data.local.entity.RefundableEntity).
              | Expected:
              |""".trimMargin() + _infoRefundable + """
              |
              | Found:
              |""".trimMargin() + _existingRefundable)
        }
        val _columnsSavings: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSavings.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("bankName", TableInfo.Column("bankName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("amount", TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("interestRate", TableInfo.Column("interestRate", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("version", TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("deviceId", TableInfo.Column("deviceId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavings.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSavings: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSavings: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSavings: TableInfo = TableInfo("savings", _columnsSavings, _foreignKeysSavings, _indicesSavings)
        val _existingSavings: TableInfo = read(connection, "savings")
        if (!_infoSavings.equals(_existingSavings)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |savings(com.monetra.data.local.entity.SavingEntity).
              | Expected:
              |""".trimMargin() + _infoSavings + """
              |
              | Found:
              |""".trimMargin() + _existingSavings)
        }
        val _columnsDeletedEntities: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDeletedEntities.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDeletedEntities.put("entityType", TableInfo.Column("entityType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDeletedEntities.put("deletedAt", TableInfo.Column("deletedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDeletedEntities: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDeletedEntities: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDeletedEntities: TableInfo = TableInfo("deleted_entities", _columnsDeletedEntities, _foreignKeysDeletedEntities, _indicesDeletedEntities)
        val _existingDeletedEntities: TableInfo = read(connection, "deleted_entities")
        if (!_infoDeletedEntities.equals(_existingDeletedEntities)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |deleted_entities(com.monetra.data.local.entity.DeletedEntity).
              | Expected:
              |""".trimMargin() + _infoDeletedEntities + """
              |
              | Found:
              |""".trimMargin() + _existingDeletedEntities)
        }
        val _columnsPendingDeletes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPendingDeletes.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPendingDeletes.put("entityId", TableInfo.Column("entityId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPendingDeletes.put("remoteId", TableInfo.Column("remoteId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPendingDeletes.put("entityType", TableInfo.Column("entityType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPendingDeletes.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPendingDeletes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPendingDeletes: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPendingDeletes: TableInfo = TableInfo("pending_deletes", _columnsPendingDeletes, _foreignKeysPendingDeletes, _indicesPendingDeletes)
        val _existingPendingDeletes: TableInfo = read(connection, "pending_deletes")
        if (!_infoPendingDeletes.equals(_existingPendingDeletes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |pending_deletes(com.monetra.data.local.entity.PendingDeleteEntity).
              | Expected:
              |""".trimMargin() + _infoPendingDeletes + """
              |
              | Found:
              |""".trimMargin() + _existingPendingDeletes)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "transactions", "user_preferences", "category_budgets", "goals", "investments", "monthly_reports", "loans", "monthly_expenses", "bill_instances", "refundable", "savings", "deleted_entities", "pending_deletes")
  }

  public override fun clearAllTables() {
    super.performClear(true, "transactions", "user_preferences", "category_budgets", "goals", "investments", "monthly_reports", "loans", "monthly_expenses", "bill_instances", "refundable", "savings", "deleted_entities", "pending_deletes")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(TransactionDao::class, TransactionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(UserPreferencesDao::class, UserPreferencesDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CategoryBudgetDao::class, CategoryBudgetDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(GoalDao::class, GoalDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(InvestmentDao::class, InvestmentDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MonthlyReportDao::class, MonthlyReportDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(LoanDao::class, LoanDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MonthlyExpenseDao::class, MonthlyExpenseDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RefundableDao::class, RefundableDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SavingDao::class, SavingDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DeletedEntityDao::class, DeletedEntityDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PendingDeleteDao::class, PendingDeleteDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }
}
