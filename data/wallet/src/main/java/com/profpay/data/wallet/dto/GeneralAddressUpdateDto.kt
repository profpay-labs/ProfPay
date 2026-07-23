package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneralAddressUpdateDto(
    @SerialName("address")
    val address: String,
    @SerialName("oldSotDerivationIndex")
    val oldSotDerivationIndex: Int,
    @SerialName("newSotDerivationIndex")
    val newSotDerivationIndex: Int,
)
