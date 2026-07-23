package com.profpay.domain.user.model.local

/**
 * Профиль пользователя.
 */
data class UserProfile(
    val userId: Long?,
    val appId: String?,
    val telegramId: Long? = null,
    val telegramUsername: String? = null,
    val deviceToken: String?,
    val isActive: Boolean,
)
