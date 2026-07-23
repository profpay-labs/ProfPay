package com.profpay.data.market.api

import com.profpay.data.market.dto.CoinGeckoMarketDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * CoinGecko API для получения рыночных данных.
 */
interface CoinGeckoApi {

    @GET("api/v3/coins/{id}")
    suspend fun getCoinData(
        @Path("id") coinId: String,
    ): Response<CoinGeckoMarketDto>
}
