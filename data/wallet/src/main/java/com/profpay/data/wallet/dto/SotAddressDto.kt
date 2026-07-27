package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SotAddressDto(
    @SerialName("address")
    val address: String,
    @SerialName("pubKey")
    val pubKey: String,
    @SerialName("index")
    val index: Byte,
    @SerialName("derivationIndex")
    val derivationIndex: Int,
)
