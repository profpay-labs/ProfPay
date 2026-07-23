package com.profpay.data.aml.repository.local

import com.profpay.core.database.dao.wallet.PendingAmlTransactionDao
import com.profpay.data.aml.mapper.PendingAmlTransactionMapper.toEntity
import com.profpay.domain.aml.model.local.PendingAmlTransactionLocal
import com.profpay.domain.aml.repository.PendingAmlTransactionLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingAmlTransactionLocalRepositoryImpl @Inject constructor(
    private val pendingAmlTransactionDao: PendingAmlTransactionDao,
) : PendingAmlTransactionLocalRepository {

    override suspend fun insert(pendingTransaction: PendingAmlTransactionLocal): Long {
        return pendingAmlTransactionDao.insert(pendingTransaction.toEntity())
    }

    override suspend fun markAsSuccessful(txId: String) {
        pendingAmlTransactionDao.markAsSuccessful(txId)
    }

    override suspend fun markAsError(txId: String) {
        pendingAmlTransactionDao.markAsError(txId)
    }

    override suspend fun exists(txId: String): Boolean {
        return pendingAmlTransactionDao.isPendingAmlTransactionExists(txId)
    }
}
