package com.monetra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monetra.data.local.dao.TransactionDao
import com.monetra.data.local.dao.UserPreferencesDao
import com.monetra.data.local.dao.CategoryBudgetDao
import com.monetra.data.local.dao.GoalDao
import com.monetra.data.local.dao.InvestmentDao
import com.monetra.data.local.dao.MonthlyReportDao
import com.monetra.data.local.dao.LoanDao
import com.monetra.data.local.dao.MonthlyExpenseDao
import com.monetra.data.local.entity.MonthlyReportEntity
import com.monetra.data.local.entity.TransactionEntity
import com.monetra.data.local.entity.UserPreferencesEntity
import com.monetra.data.local.entity.CategoryBudgetEntity
import com.monetra.data.local.entity.GoalEntity
import com.monetra.data.local.entity.InvestmentEntity
import com.monetra.data.local.entity.LoanEntity
import com.monetra.data.local.entity.MonthlyExpenseEntity

@Database(
    entities = [
        TransactionEntity::class, 
        UserPreferencesEntity::class, 
        CategoryBudgetEntity::class,
        GoalEntity::class,
        InvestmentEntity::class,
        MonthlyReportEntity::class,
        LoanEntity::class,
        MonthlyExpenseEntity::class
    ], 
    version = 2, 
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
}
