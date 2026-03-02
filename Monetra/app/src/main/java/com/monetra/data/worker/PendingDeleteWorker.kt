package com.monetra.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.monetra.data.local.dao.PendingDeleteDao
import com.monetra.data.local.entity.PendingDeleteEntity
import com.monetra.domain.repository.TransactionRepository
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.LoanRepository
import com.monetra.domain.repository.GoalRepository
import com.monetra.domain.repository.InvestmentRepository
import com.monetra.domain.repository.RefundableRepository
import com.monetra.domain.repository.SavingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class PendingDeleteWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingDeleteDao: PendingDeleteDao,
    private val transactionRepository: TransactionRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository,
    private val loanRepository: LoanRepository,
    private val goalRepository: GoalRepository,
    private val investmentRepository: InvestmentRepository,
    private val refundableRepository: RefundableRepository,
    private val savingRepository: SavingRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Process all pending deletes older than 5 seconds
            val cutoff = System.currentTimeMillis() - GRACE_PERIOD_MS
            val staleDeletes = pendingDeleteDao.getStaleDeletes(cutoff)

            if (staleDeletes.isEmpty()) return Result.success()

            for (entry in staleDeletes) {
                commitDelete(entry)
            }

            // Clean up processed entries
            pendingDeleteDao.deleteByIds(staleDeletes.map { it.id })

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun commitDelete(entry: PendingDeleteEntity) {
        when (entry.entityType) {
            "TRANSACTION" -> transactionRepository.deleteTransaction(entry.entityId)
            "MONTHLY_EXPENSE" -> {
                monthlyExpenseRepository.getMonthlyExpenseById(entry.entityId)?.let {
                    monthlyExpenseRepository.deleteMonthlyExpense(it)
                }
            }
            "LOAN" -> loanRepository.deleteLoan(entry.entityId)
            "GOAL" -> goalRepository.deleteGoal(entry.entityId)
            "INVESTMENT" -> investmentRepository.deleteInvestment(entry.entityId)
            "REFUNDABLE" -> {
                refundableRepository.getRefundableById(entry.entityId)?.let {
                    refundableRepository.deleteRefundable(it)
                }
            }
            "SAVING" -> {
                savingRepository.getSavingById(entry.entityId)?.let {
                    savingRepository.deleteSaving(it)
                }
            }
        }
    }

    companion object {
        private const val WORK_NAME = "pending_delete_gc"
        private const val GRACE_PERIOD_MS = 3_000L

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<PendingDeleteWorker>()
                .setInitialDelay(GRACE_PERIOD_MS + 1_000L, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
