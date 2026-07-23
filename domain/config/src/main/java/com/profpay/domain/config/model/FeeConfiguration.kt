package com.profpay.domain.config.model

/**
 * Конфигурация комиссий с сервера
 */
data class FeeConfiguration(
    /** Адрес для оплаты комиссий */
    val trxFeeAddress: String,
    /** Размер AML комиссии в SUN */
    val amlFee: Long,
    val timestamp: Long,
)
