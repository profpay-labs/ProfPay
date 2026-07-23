package com.profpay.data.market.repository.local

import com.profpay.core.database.dao.wallet.ExchangeRatesDao
import com.profpay.data.market.mapper.ExchangeRatesMapper.toEntity
import com.profpay.domain.market.model.local.ExchangeRateLocal
import com.profpay.domain.market.repository.ExchangeRatesLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRatesLocalRepositoryImpl @Inject constructor(
    private val exchangeRatesDao: ExchangeRatesDao,
) : ExchangeRatesLocalRepository {

    override suspend fun insert(exchangeRate: ExchangeRateLocal): Long {
        return exchangeRatesDao.insert(exchangeRate.toEntity())
    }

    override suspend fun exists(symbol: String): Boolean {
        return exchangeRatesDao.doesSymbolExist(symbol)
    }

    override suspend fun update(symbol: String, rate: Double) {
        exchangeRatesDao.updateExchangeRate(symbol, rate)
    }

    override suspend fun getRate(symbol: String): Double {
        return exchangeRatesDao.getExchangeRateValue(symbol)
    }
}
