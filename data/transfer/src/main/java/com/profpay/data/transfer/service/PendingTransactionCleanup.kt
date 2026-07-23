package com.profpay.data.transfer.service

import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Сервис очистки просроченных pending транзакций.
 *
 * Удаляет транзакции, которые "зависли" в pending состоянии
 * дольше допустимого времени (timeout истёк).
 */
@Singleton
class PendingTransactionCleanup @Inject constructor(
    private val pendingTransactionLocalRepository: PendingTransactionLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
) {

    /**
     * Очищает просроченные pending транзакции.
     *
     * Транзакция считается просроченной если она находится в pending
     * дольше установленного timeout (определяется в репозитории).
     */
    suspend fun cleanupExpiredTransactions() {
        val now = System.currentTimeMillis()
        val expiredTxs = pendingTransactionLocalRepository.getExpired(now)

        for (tx in expiredTxs) {
            pendingTransactionLocalRepository.deleteByTxId(tx.txId)
            transactionLocalRepository.deleteByTxId(tx.txId)
        }
    }
}
