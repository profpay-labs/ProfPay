package com.profpay.data.market.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BinancePriceDto(
    @SerialName("symbol")
    val symbol: String,
    @SerialName("price")
    val price: String,
)
