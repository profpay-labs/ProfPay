package com.profpay.data.market.repository

import com.profpay.core.network.exception.safeApiCall
import com.profpay.data.market.api.BinanceApi
import com.profpay.data.market.api.CoinGeckoApi
import com.profpay.data.market.dto.BinancePriceDto
import com.profpay.data.market.dto.CoinGeckoMarketDto
import com.profpay.data.market.mapper.toDomain
import com.profpay.domain.market.model.BinanceSymbol
import com.profpay.domain.market.model.CoinSymbol
import com.profpay.domain.market.model.ExchangeRate
import com.profpay.domain.market.model.PriceChange24h
import com.profpay.domain.market.repository.MarketRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val binanceApi: BinanceApi,
    private val coinGeckoApi: CoinGeckoApi,
    private val json: Json,
) : MarketRepository {

    override suspend fun getExchangeRate(symbol: BinanceSymbol): Result<ExchangeRate> {
        val apiResult: Result<BinancePriceDto> = safeApiCall(json) {
            binanceApi.getPrice(symbol.symbol)
        }

        return apiResult.map { dto -> dto.toDomain() }
    }

    override suspend fun getPriceChange24h(coin: CoinSymbol): Result<PriceChange24h> {
        val apiResult: Result<CoinGeckoMarketDto> = safeApiCall(json) {
            coinGeckoApi.getCoinData(coin.id)
        }

        return apiResult.map { dto -> dto.toDomain() }
    }

    override suspend fun syncAllRates(): Result<Unit> {
        return runCatching {
            getExchangeRate(BinanceSymbol.TRX_USDT).getOrThrow()
            getPriceChange24h(CoinSymbol.TRON).getOrThrow()
            getPriceChange24h(CoinSymbol.USDT_TRC20).getOrThrow()
        }
    }
}
