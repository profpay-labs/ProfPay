package com.profpay.wallet.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.profpay.data.transfer.service.PendingTransactionCleanup
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker для периодической очистки просроченных pending транзакций.
 *
 * Запускается каждые 15 минут (минимальный интервал WorkManager).
 * Правильно работает с Doze mode и battery optimizations.
 */
@HiltWorker
class PendingTransactionCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingTransactionCleanup: PendingTransactionCleanup,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            pendingTransactionCleanup.cleanupExpiredTransactions()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "pending_transaction_cleanup"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<PendingTransactionCleanupWorker>(
                15, TimeUnit.MINUTES, // Минимальный интервал
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest,
            )
        }
    }
}
