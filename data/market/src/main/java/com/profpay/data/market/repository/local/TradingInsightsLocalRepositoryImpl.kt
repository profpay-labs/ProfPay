package com.profpay.data.market.repository.local

import com.profpay.core.database.dao.wallet.TradingInsightsDao
import com.profpay.data.market.mapper.TradingInsightsMapper.toEntity
import com.profpay.domain.market.model.local.TradingInsightsLocal
import com.profpay.domain.market.repository.TradingInsightsLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradingInsightsLocalRepositoryImpl @Inject constructor(
    private val tradingInsightsDao: TradingInsightsDao,
) : TradingInsightsLocalRepository {

    override suspend fun insert(tradingInsights: TradingInsightsLocal): Long {
        return tradingInsightsDao.insert(tradingInsights.toEntity())
    }

    override suspend fun exists(symbol: String): Boolean {
        return tradingInsightsDao.doesSymbolExist(symbol)
    }

    override suspend fun updatePriceChange24h(symbol: String, percentage: Double) {
        tradingInsightsDao.updatePriceChangePercentage24h(symbol, percentage)
    }

    override suspend fun getPriceChange24h(symbol: String): Double {
        return tradingInsightsDao.getPriceChangePercentage24h(symbol)
    }
}
