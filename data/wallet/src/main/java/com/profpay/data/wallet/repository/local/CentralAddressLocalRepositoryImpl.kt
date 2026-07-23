package com.profpay.data.wallet.repository.local

import com.profpay.core.database.dao.wallet.CentralAddressDao
import com.profpay.data.wallet.mapper.CentralAddressMapper.toEntity
import com.profpay.data.wallet.mapper.CentralAddressMapper.toLocal
import com.profpay.domain.wallet.model.local.CentralAddressLocal
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CentralAddressLocalRepositoryImpl @Inject constructor(
    private val centralAddressDao: CentralAddressDao,
) : CentralAddressLocalRepository {

    override suspend fun insert(centralAddress: CentralAddressLocal): Long {
        return centralAddressDao.insertNewCentralAddress(centralAddress.toEntity())
    }

    override suspend fun get(): CentralAddressLocal? {
        return centralAddressDao.getCentralAddress()?.toLocal()
    }

    override fun observe(): Flow<CentralAddressLocal?> {
        return centralAddressDao.getCentralAddressFlow().map { it?.toLocal() }
    }

    override suspend fun updateTrxBalance(balance: BigInteger) {
        centralAddressDao.updateTrxBalance(balance)
    }

    override suspend fun change(
        address: String,
        publicKey: String,
        privateKey: String,
    ) {
        centralAddressDao.changeCentralAddress(
            address = address,
            publicKey = publicKey,
            privateKey = privateKey,
        )
    }

    override suspend fun exists(): Boolean {
        return centralAddressDao.isCentralAddressExists()
    }
}
