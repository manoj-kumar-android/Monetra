package com.monetra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monetra.data.local.dao.*
import com.monetra.data.local.entity.*

@Database(
    entities = [
        TransactionEntity::class, 
        UserPreferencesEntity::class, 
        CategoryBudgetEntity::class,
        GoalEntity::class,
        InvestmentEntity::class,
        MonthlyReportEntity::class,
        LoanEntity::class,
        MonthlyExpenseEntity::class,
        BillInstanceEntity::class,
        RefundableEntity::class,
        SavingEntity::class
    ], 
    version = 3, 
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MonetraDatabase : RoomDatabase() {
    
    abstract val transactionDao: TransactionDao
    abstract val userPreferencesDao: UserPreferencesDao
    abstract val categoryBudgetDao: CategoryBudgetDao
    abstract val goalDao: GoalDao
    abstract val investmentDao: InvestmentDao
    abstract val monthlyReportDao: MonthlyReportDao
    abstract val loanDao: LoanDao
    abstract val monthlyExpenseDao: MonthlyExpenseDao
    abstract val refundableDao: RefundableDao
    abstract val savingDao: SavingDao

    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `savings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `bankName` TEXT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `interestRate` REAL, 
                        `note` TEXT
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
