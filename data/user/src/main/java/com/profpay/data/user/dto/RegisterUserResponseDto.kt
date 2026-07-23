package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserResponseDto(
    @SerialName("userId")
    val userId: Long,
    @SerialName("timestamp")
    val timestamp: Long,
)
