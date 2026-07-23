package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SotInfoDto(
    @SerialName("index")
    val index: Int,
    @SerialName("derivationIndex")
    val derivationIndex: Int,
)
