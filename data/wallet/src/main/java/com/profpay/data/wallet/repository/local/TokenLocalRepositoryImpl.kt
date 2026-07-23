package com.profpay.data.wallet.repository.local

import com.profpay.core.database.dao.wallet.TokenDao
import com.profpay.data.wallet.mapper.TokenMapper.toEntity
import com.profpay.domain.wallet.model.local.TokenLocal
import com.profpay.domain.wallet.repository.local.TokenLocalRepository
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenLocalRepositoryImpl @Inject constructor(
    private val tokenDao: TokenDao,
) : TokenLocalRepository {

    override suspend fun insert(token: TokenLocal): Long {
        return tokenDao.insert(token.toEntity())
    }

    override suspend fun updateBalance(
        addressId: Long,
        tokenName: String,
        balance: BigInteger,
    ) {
        tokenDao.updateTronBalanceViaId(balance, addressId, tokenName)
    }

    override suspend fun getTokenId(
        addressId: Long,
        tokenName: String,
    ): Long {
        return tokenDao.getTokenIdByAddressIdAndTokenName(addressId, tokenName)
    }
}
