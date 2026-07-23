package com.profpay.data.transfer.repository.local

import com.profpay.core.database.dao.wallet.PendingTransactionDao
import com.profpay.data.transfer.mapper.PendingTransactionMapper.toEntity
import com.profpay.data.transfer.mapper.PendingTransactionMapper.toLocal
import com.profpay.domain.transfer.model.local.PendingTransactionLocal
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingTransactionLocalRepositoryImpl @Inject constructor(
    private val pendingTransactionDao: PendingTransactionDao,
) : PendingTransactionLocalRepository {

    override suspend fun insert(pendingTransaction: PendingTransactionLocal): Long {
        return pendingTransactionDao.insert(pendingTransaction.toEntity())
    }

    override suspend fun exists(txId: String): Boolean {
        return pendingTransactionDao.pendingTransactionIsExistsByTxId(txId)
    }

    override suspend fun deleteByTxId(txId: String) {
        pendingTransactionDao.deletePendingTransactionByTxId(txId)
    }

    override suspend fun getExpired(currentTime: Long): List<PendingTransactionLocal> {
        return pendingTransactionDao.getExpiredTransactions(currentTime).map { it.toLocal() }
    }
}
