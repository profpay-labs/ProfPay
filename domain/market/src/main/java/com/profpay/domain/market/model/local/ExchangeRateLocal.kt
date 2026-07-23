package com.profpay.domain.market.model.local

/**
 * Локальная модель курса валюты.
 */
data class ExchangeRateLocal(
    val id: Long? = null,
    val symbol: String,
    val rate: Double,
)
