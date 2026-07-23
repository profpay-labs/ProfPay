package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceResponseDto(
    @SerialName("successful")
    val successful: Boolean,
    @SerialName("timestamp")
    val timestamp: Long,
)
