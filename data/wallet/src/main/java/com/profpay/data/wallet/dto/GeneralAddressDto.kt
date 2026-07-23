package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneralAddressDto(
    @SerialName("address")
    val address: String,
    @SerialName("pubKey")
    val pubKey: String,
    @SerialName("derivedIndices")
    val derivedIndices: List<Int>,
)
