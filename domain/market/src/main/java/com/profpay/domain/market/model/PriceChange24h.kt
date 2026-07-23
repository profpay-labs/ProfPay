package com.profpay.domain.market.model

/**
 * Изменение цены за 24 часа.
 */
data class PriceChange24h(
    val coinId: String,
    val percentageChange: Double,
)
