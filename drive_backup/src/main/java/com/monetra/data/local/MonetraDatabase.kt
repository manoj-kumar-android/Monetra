package com.monetra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monetra.data.local.dao.AccountDao
import com.monetra.data.local.dao.CategoryBudgetDao
import com.monetra.data.local.dao.DeletedEntityDao
import com.monetra.data.local.dao.InvestmentDao
import com.monetra.data.local.dao.LoanDao
import com.monetra.data.local.dao.MonthlyExpenseDao
import com.monetra.data.local.dao.NoteDao
import com.monetra.data.local.dao.PendingDeleteDao
import com.monetra.data.local.dao.PendingTransactionDao
import com.monetra.data.local.dao.RefundableDao
import com.monetra.data.local.dao.SavingDao
import com.monetra.data.local.dao.TransactionDao
import com.monetra.data.local.dao.UserPreferencesDao
import com.monetra.data.local.entity.AccountEntity
import com.monetra.data.local.entity.BillInstanceEntity
import com.monetra.data.local.entity.CategoryBudgetEntity
import com.monetra.data.local.entity.DeletedEntity
import com.monetra.data.local.entity.InvestmentEntity
import com.monetra.data.local.entity.LoanEntity
import com.monetra.data.local.entity.MonthlyExpenseEntity
import com.monetra.data.local.entity.NoteEntity
import com.monetra.data.local.entity.PendingDeleteEntity
import com.monetra.data.local.entity.PendingTransactionEntity
import com.monetra.data.local.entity.RefundableEntity
import com.monetra.data.local.entity.SavingEntity
import com.monetra.data.local.entity.TransactionEntity
import com.monetra.data.local.entity.UserPreferencesEntity

@Database(
    entities = [
        TransactionEntity::class, 
        UserPreferencesEntity::class, 
        CategoryBudgetEntity::class,
        InvestmentEntity::class,
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
    abstract val investmentDao: InvestmentDao
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
