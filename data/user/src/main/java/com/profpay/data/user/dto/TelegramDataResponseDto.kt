package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response с Telegram данными пользователя
 */
@Serializable
data class TelegramDataResponseDto(
    @SerialName("telegramId")
    val telegramId: Long,
    @SerialName("username")
    val username: String,
)
