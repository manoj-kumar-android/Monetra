package com.monetra.di

import android.app.Application
import androidx.room.Room
import com.monetra.data.local.MonetraDatabase
import com.monetra.data.local.dao.*
import com.monetra.data.repository.*
import com.monetra.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupDataModule {

    @Binds
    @Singleton
    abstract fun bindCloudBackupRepository(impl: CloudBackupRepositoryImpl): CloudBackupRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    companion object {
        @Provides
        @Singleton
        fun provideMonetraDatabase(app: Application): MonetraDatabase {
            return Room.databaseBuilder(
                app,
                MonetraDatabase::class.java,
                "monetra_db"
            )
            .addMigrations(MonetraDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
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
        fun provideSavingDao(db: MonetraDatabase): SavingDao = db.savingDao

        @Provides
        @Singleton
        fun providePendingDeleteDao(db: MonetraDatabase): PendingDeleteDao = db.pendingDeleteDao

        @Provides
        @Singleton
        fun provideDeletedEntityDao(db: MonetraDatabase): DeletedEntityDao = db.deletedEntityDao
    }
}
