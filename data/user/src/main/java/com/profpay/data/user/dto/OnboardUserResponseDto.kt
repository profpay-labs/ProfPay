package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnboardUserResponseDto(
    @SerialName("userId")
    val userId: Long,
    @SerialName("walletId")
    val walletId: Long? = null,
    @SerialName("timestamp")
    val timestamp: Long,
)
