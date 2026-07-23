package com.profpay.domain.wallet.model

/**
 * Результат создания кошелька
 */
data class WalletResult(
    val id: Long,
    val userId: Long,
    val addresses: List<WalletAddress>,
)
