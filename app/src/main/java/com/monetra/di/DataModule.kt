package com.monetra.di

import com.monetra.data.repository.BudgetRepositoryImpl
import com.monetra.data.repository.InvestmentRepositoryImpl
import com.monetra.data.repository.LoanRepositoryImpl
import com.monetra.data.repository.MonthlyExpenseRepositoryImpl
import com.monetra.data.repository.NoteRepositoryImpl
import com.monetra.data.repository.PendingTransactionRepositoryImpl
import com.monetra.data.repository.RefundableRepositoryImpl
import com.monetra.data.repository.SavingRepositoryImpl
import com.monetra.data.repository.SubscriptionRepositoryImpl
import com.monetra.data.repository.TransactionRepositoryImpl
import com.monetra.data.repository.UserPreferenceRepositoryImpl
import com.monetra.domain.repository.BudgetRepository
import com.monetra.domain.repository.InvestmentRepository
import com.monetra.domain.repository.LoanRepository
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.NoteRepository
import com.monetra.domain.repository.PendingTransactionRepository
import com.monetra.domain.repository.RefundableRepository
import com.monetra.domain.repository.SavingRepository
import com.monetra.domain.repository.SubscriptionRepository
import com.monetra.domain.repository.TransactionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferenceRepository(impl: UserPreferenceRepositoryImpl): UserPreferenceRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository


    @Binds
    @Singleton
    abstract fun bindInvestmentRepository(impl: InvestmentRepositoryImpl): InvestmentRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindLoanRepository(impl: LoanRepositoryImpl): LoanRepository

    @Binds
    @Singleton
    abstract fun bindMonthlyExpenseRepository(impl: MonthlyExpenseRepositoryImpl): MonthlyExpenseRepository

    @Binds
    @Singleton
    abstract fun bindRefundableRepository(impl: RefundableRepositoryImpl): RefundableRepository

    @Binds
    @Singleton
    abstract fun bindSavingRepository(impl: SavingRepositoryImpl): SavingRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindPendingTransactionRepository(impl: PendingTransactionRepositoryImpl): PendingTransactionRepository
}
