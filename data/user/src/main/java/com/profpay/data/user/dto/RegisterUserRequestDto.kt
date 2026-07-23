package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequestDto(
    @SerialName("deviceToken")
    val deviceToken: String,
    @SerialName("appId")
    val appId: String,
)
