package com.profpay.domain.market.model.local

/**
 * Локальная модель торговой аналитики.
 */
data class TradingInsightsLocal(
    val id: Long? = null,
    val symbol: String,
    val priceChangePercentage24h: Double,
)
