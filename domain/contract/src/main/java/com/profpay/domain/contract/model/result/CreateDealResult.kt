package com.profpay.domain.contract.model.result

/**
 * Результат создания сделки
 */
data class CreateDealResult(
    val dealId: Long,
    val contractAddress: String,
    val arbiterAddresses: List<String>,
    val timestampSeconds: Long,
)
