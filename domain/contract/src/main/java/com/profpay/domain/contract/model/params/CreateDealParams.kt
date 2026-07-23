package com.profpay.domain.contract.model.params

/**
 * Параметры для создания сделки
 */
data class CreateDealParams(
    val buyerUserId: Long,
    val sellerUserId: Long,
    val arbiterGroupId: Long,
    val amount: Long,
)
