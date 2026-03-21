package com.monetra.di

import android.app.Application
import androidx.room.Room
import com.monetra.data.local.MonetraDatabase
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
import com.monetra.data.repository.CloudBackupRepositoryImpl
import com.monetra.data.repository.SyncRepositoryImpl
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.SyncRepository
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
            .addCallback(MonetraDatabase.CALLBACK)
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
        fun provideInvestmentDao(db: MonetraDatabase): InvestmentDao = db.investmentDao

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

        @Provides
        @Singleton
        fun provideNoteDao(db: MonetraDatabase): NoteDao = db.noteDao

        @Provides
        @Singleton
        fun providePendingTransactionDao(db: MonetraDatabase): PendingTransactionDao =
            db.pendingTransactionDao

        @Provides
        @Singleton
        fun provideAccountDao(db: MonetraDatabase): AccountDao = db.accountDao
    }
}
