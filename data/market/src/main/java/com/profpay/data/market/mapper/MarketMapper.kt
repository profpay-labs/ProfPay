package com.profpay.data.market.mapper

import com.profpay.data.market.dto.BinancePriceDto
import com.profpay.data.market.dto.CoinGeckoMarketDto
import com.profpay.domain.market.model.ExchangeRate
import com.profpay.domain.market.model.PriceChange24h
import java.math.BigDecimal

fun BinancePriceDto.toDomain(): ExchangeRate {
    return ExchangeRate(
        symbol = symbol,
        price = BigDecimal(price),
        timestamp = System.currentTimeMillis(),
    )
}

fun CoinGeckoMarketDto.toDomain(): PriceChange24h {
    return PriceChange24h(
        coinId = id,
        percentageChange = marketData.priceChangePercentage24h ?: 0.0,
    )
}
