package com.profpay.data.market.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinGeckoMarketDto(
    @SerialName("id")
    val id: String,
    @SerialName("market_data")
    val marketData: MarketDataDto,
)

@Serializable
data class MarketDataDto(
    @SerialName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
)
