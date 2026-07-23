package com.profpay.domain.contract.model

data class DealParticipant(
    val userId: Long,
    val telegramId: Long,
    val username: String,
    val walletAddress: String,
    val tier: UserTier,
)
