package com.monetra.di

import android.app.Application
import androidx.room.Room
import com.monetra.data.local.MonetraDatabase
import com.monetra.data.local.dao.*
import com.monetra.data.repository.*
import com.monetra.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideMonetraDatabase(app: Application): MonetraDatabase {
        return Room.databaseBuilder(
            app,
            MonetraDatabase::class.java,
            "monetra_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: MonetraDatabase): TransactionDao = db.transactionDao

    @Provides
    @Singleton
    fun provideUserPreferencesDao(db: MonetraDatabase): UserPreferencesDao = db.userPreferencesDao

    @Provides
    @Singleton
    fun provideCategoryBudgetDao(db: MonetraDatabase): CategoryBudgetDao = db.categoryBudgetDao

    @Provides
    @Singleton
    fun provideGoalDao(db: MonetraDatabase): GoalDao = db.goalDao

    @Provides
    @Singleton
    fun provideInvestmentDao(db: MonetraDatabase): InvestmentDao = db.investmentDao

    @Provides
    @Singleton
    fun provideMonthlyReportDao(db: MonetraDatabase): MonthlyReportDao = db.monthlyReportDao

    @Provides
    @Singleton
    fun provideLoanDao(db: MonetraDatabase): LoanDao = db.loanDao

    @Provides
    @Singleton
    fun provideMonthlyExpenseDao(db: MonetraDatabase): MonthlyExpenseDao = db.monthlyExpenseDao

    @Provides
    @Singleton
    fun provideRefundableDao(db: MonetraDatabase): RefundableDao = db.refundableDao

    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository = TransactionRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideUserPreferenceRepository(dao: UserPreferencesDao): UserPreferenceRepository = UserPreferenceRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: CategoryBudgetDao,
        transactionDao: TransactionDao
    ): BudgetRepository = BudgetRepositoryImpl(budgetDao, transactionDao)

    @Provides
    @Singleton
    fun provideGoalRepository(dao: GoalDao): GoalRepository = GoalRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideInvestmentRepository(dao: InvestmentDao): InvestmentRepository = InvestmentRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideReportRepository(dao: MonthlyReportDao): ReportRepository = ReportRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideSubscriptionRepository(userPrefsRepo: UserPreferenceRepository): SubscriptionRepository = SubscriptionRepositoryImpl(userPrefsRepo)

    @Provides
    @Singleton
    fun provideLoanRepository(dao: LoanDao): LoanRepository = LoanRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideMonthlyExpenseRepository(dao: MonthlyExpenseDao): MonthlyExpenseRepository = MonthlyExpenseRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideRefundableRepository(dao: RefundableDao): RefundableRepository = RefundableRepositoryImpl(dao)
}
