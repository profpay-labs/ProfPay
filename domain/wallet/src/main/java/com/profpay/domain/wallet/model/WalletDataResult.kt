package com.profpay.domain.wallet.model

/**
 * Данные кошелька по адресу
 */
data class WalletDataResult(
    val userId: Long,
    val derivedIndices: List<Int>,
    val timestamp: String,
)
