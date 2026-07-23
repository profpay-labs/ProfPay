package com.profpay.domain.user.model.local

/**
 * Настройки пользователя.
 */
data class UserSettings(
    val id: Long,
    val languageCode: String,
    val isBotActive: Boolean,
    val botToken: String?,
    val isAutoAmlEnabled: Boolean,
)
