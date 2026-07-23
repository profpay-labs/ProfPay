package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckPermissionsRequestDto(
    @SerialName("appId")
    val appId: String,
    @SerialName("deviceToken")
    val deviceToken: String,
)
