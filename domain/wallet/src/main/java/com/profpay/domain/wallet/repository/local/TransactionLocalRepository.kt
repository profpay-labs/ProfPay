package com.profpay.domain.wallet.repository.local

import com.profpay.domain.wallet.model.Transaction
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.TransactionStatusCode
import kotlinx.coroutines.flow.Flow

/**
 * Локальный репозиторий транзакций.
 */
interface TransactionLocalRepository {

    suspend fun insert(transaction: Transaction)

    suspend fun countByTxId(txId: String): Int

    fun observeAllByWalletId(walletId: Long): Flow<List<TransactionSummary>>

    fun observeById(transactionId: Long): Flow<Transaction>

    suspend fun getByTxId(txId: String): Transaction

    fun observeByAddressAndToken(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean,
    ): Flow<List<TransactionSummary>>

    suspend fun isPending(txId: String): Boolean

    suspend fun isSuccessful(txId: String): Boolean

    suspend fun updateStatusAndTimestamp(
        txId: String,
        statusCode: TransactionStatusCode,
        timestamp: Long,
    )

    suspend fun markAsProcessed(transactionId: Long)

    suspend fun markAsProcessedByTxId(txId: String)

    suspend fun markAsUnprocessed(transactionId: Long)

    suspend fun markAsUnprocessedByTxId(txId: String)

    suspend fun deleteByTxId(txId: String)
}
