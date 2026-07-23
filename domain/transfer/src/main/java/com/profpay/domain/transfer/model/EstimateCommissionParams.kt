package com.profpay.domain.transfer.model

/**
 * Параметры для оценки комиссии
 */
data class EstimateCommissionParams(
    val userId: Long,
    val address: String,
    val energyRequired: Long,
    val bandwidthRequired: Long,
)
