package com.profpay.data.wallet.repository.local

import com.profpay.core.database.dao.TransactionsDao
import com.profpay.data.wallet.local.mapper.TransactionMapper.toDomain
import com.profpay.data.wallet.local.mapper.TransactionMapper.toEntity
import com.profpay.domain.wallet.model.Transaction
import com.profpay.domain.wallet.model.TransactionStatusCode
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionLocalRepositoryImpl @Inject constructor(
    private val transactionsDao: TransactionsDao,
) : TransactionLocalRepository {

    override suspend fun insert(transaction: Transaction) {
        transactionsDao.insertNewTransaction(transaction.toEntity())
    }

    override suspend fun countByTxId(txId: String): Int {
        return transactionsDao.transactionExistsViaTxid(txId)
    }

    override fun observeAllByWalletId(walletId: Long): Flow<List<TransactionSummary>> {
        return transactionsDao.getAllRelatedTransactionsFlow(walletId)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun observeById(transactionId: Long): Flow<Transaction> {
        return transactionsDao.getTransactionFlowById(transactionId)
            .map { it.toDomain() }
    }

    override suspend fun getByTxId(txId: String): Transaction {
        return transactionsDao.getTransactionByTxId(txId).toDomain()
    }

    override fun observeByAddressAndToken(
        walletId: Long,
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean,
    ): Flow<List<TransactionSummary>> {
        return transactionsDao.getTransactionsByAddressAndTokenFlow(
            walletId = walletId,
            address = address,
            tokenName = tokenName,
            isSender = isSender,
            isCentralAddress = isCentralAddress,
        ).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun isPending(txId: String): Boolean {
        return transactionsDao.isTransactionPending(txId)
    }

    override suspend fun isSuccessful(txId: String): Boolean {
        return transactionsDao.isTransactionSuccessful(txId)
    }

    override suspend fun updateStatusAndTimestamp(
        txId: String,
        statusCode: TransactionStatusCode,
        timestamp: Long,
    ) {
        transactionsDao.updateStatusAndTimestampByTxId(statusCode.code, timestamp, txId)
    }

    override suspend fun markAsProcessed(transactionId: Long) {
        transactionsDao.transactionSetProcessedUpdateTrueById(transactionId)
    }

    override suspend fun markAsProcessedByTxId(txId: String) {
        transactionsDao.transactionSetProcessedUpdateTrueByTxId(txId)
    }

    override suspend fun markAsUnprocessed(transactionId: Long) {
        transactionsDao.transactionSetProcessedUpdateFalseById(transactionId)
    }

    override suspend fun markAsUnprocessedByTxId(txId: String) {
        transactionsDao.transactionSetProcessedUpdateFalseByTxId(txId)
    }

    override suspend fun deleteByTxId(txId: String) {
        transactionsDao.deleteTransactionByTxId(txId)
    }
}
