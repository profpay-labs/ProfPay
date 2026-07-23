package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetCentralAddressRequestDto(
    @SerialName("address")
    val address: String,
    @SerialName("publicKey")
    val publicKey: String,
)
