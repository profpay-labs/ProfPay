package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTelegramResponseDto(
    @SerialName("userId")
    val userId: Long,
    @SerialName("telegramId")
    val telegramId: Long,
    @SerialName("timestamp")
    val timestamp: Long,
)
