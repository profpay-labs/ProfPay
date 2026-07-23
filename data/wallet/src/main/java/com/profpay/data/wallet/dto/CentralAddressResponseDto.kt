package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CentralAddressResponseDto(
    @SerialName("id")
    val id: Long,
    @SerialName("userId")
    val userId: Long,
    @SerialName("address")
    val address: String,
    @SerialName("publicKey")
    val publicKey: String,
    @SerialName("type")
    val type: String,
    @SerialName("active")
    val active: Boolean,
)
