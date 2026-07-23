package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckPermissionsResponseDto(
    @SerialName("appAllowed")
    val appAllowed: Boolean,
)
