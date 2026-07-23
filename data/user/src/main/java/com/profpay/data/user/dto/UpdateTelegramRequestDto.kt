package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTelegramRequestDto(
    @SerialName("username")
    val username: String,
    @SerialName("telegramId")
    val telegramId: Long,
)
