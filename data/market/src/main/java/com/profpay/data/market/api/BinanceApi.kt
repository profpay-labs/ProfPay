package com.profpay.data.market.api

import com.profpay.data.market.dto.BinancePriceDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Binance API для получения курсов криптовалют.
 */
interface BinanceApi {

    @GET("api/v3/ticker/price")
    suspend fun getPrice(
        @Query("symbol") symbol: String,
    ): Response<BinancePriceDto>
}
