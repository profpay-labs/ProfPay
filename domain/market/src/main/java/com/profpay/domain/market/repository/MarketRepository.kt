package com.profpay.domain.market.repository

import com.profpay.domain.market.model.BinanceSymbol
import com.profpay.domain.market.model.CoinSymbol
import com.profpay.domain.market.model.ExchangeRate
import com.profpay.domain.market.model.PriceChange24h

/**
 * Репозиторий для рыночных данных.
 */
interface MarketRepository {

    /**
     * Получить курс обмена с Binance.
     */
    suspend fun getExchangeRate(symbol: BinanceSymbol): Result<ExchangeRate>

    /**
     * Получить изменение цены за 24h с CoinGecko.
     */
    suspend fun getPriceChange24h(coin: CoinSymbol): Result<PriceChange24h>

    /**
     * Синхронизировать все курсы (для background sync).
     */
    suspend fun syncAllRates(): Result<Unit>
}
