package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CentralAddressDto(
    @SerialName("address")
    val address: String,
    @SerialName("pubKey")
    val pubKey: String,
)
