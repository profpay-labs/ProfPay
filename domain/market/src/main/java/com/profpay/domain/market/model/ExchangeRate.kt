package com.profpay.domain.market.model

import java.math.BigDecimal

/**
 * Курс обмена криптовалюты.
 */
data class ExchangeRate(
    val symbol: String,
    val price: BigDecimal,
    val timestamp: Long,
)
