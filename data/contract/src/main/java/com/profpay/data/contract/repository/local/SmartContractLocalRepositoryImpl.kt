package com.profpay.data.contract.repository.local

import com.profpay.core.database.dao.wallet.SmartContractDao
import com.profpay.data.contract.mapper.SmartContractMapper.toEntity
import com.profpay.data.contract.mapper.SmartContractMapper.toLocal
import com.profpay.domain.contract.model.local.SmartContractLocal
import com.profpay.domain.contract.repository.SmartContractLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartContractLocalRepositoryImpl @Inject constructor(
    private val smartContractDao: SmartContractDao,
) : SmartContractLocalRepository {

    override suspend fun insert(smartContract: SmartContractLocal): Long {
        return smartContractDao.insert(smartContract.toEntity())
    }

    override suspend fun get(): SmartContractLocal? {
        return smartContractDao.getSmartContract()?.toLocal()
    }

    override fun observe(): Flow<SmartContractLocal?> {
        return smartContractDao.getSmartContractFlow().map { it?.toLocal() }
    }

    override suspend fun restore(contractAddress: String) {
        smartContractDao.restoreSmartContract(contractAddress)
    }
}
