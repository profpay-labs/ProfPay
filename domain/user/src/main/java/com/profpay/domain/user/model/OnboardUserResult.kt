package com.profpay.domain.user.model

/**
 * Результат онбординга пользователя с созданием кошелька.
 */
data class OnboardUserResult(
    val userId: Long,
    val walletId: Long?,
    val timestamp: Long,
)
