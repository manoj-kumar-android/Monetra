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
        SavingEntity::class,
        NoteEntity::class,
        DeletedEntity::class,
        PendingDeleteEntity::class,
        PendingTransactionEntity::class,
        AccountEntity::class
    ],
    version = 1, 
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
    abstract val noteDao: NoteDao
    abstract val deletedEntityDao: DeletedEntityDao
    abstract val pendingDeleteDao: PendingDeleteDao
    abstract val pendingTransactionDao: PendingTransactionDao
    abstract val accountDao: AccountDao

    companion object {
        val CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                val defaultAccounts = listOf("CASH", "HDFC", "ICICI", "SBI", "OTHER")
                defaultAccounts.forEach { name ->
                    db.execSQL("INSERT INTO accounts (name) VALUES ('$name')")
                }
            }
        }
    }
}
