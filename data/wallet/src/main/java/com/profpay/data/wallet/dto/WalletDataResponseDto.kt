package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response с данными кошелька по адресу
 */
@Serializable
data class WalletDataResponseDto(
    @SerialName("userId")
    val userId: Long,
    @SerialName("derivedIndices")
    val derivedIndices: List<Int>,
    @SerialName("timestamp")
    val timestamp: String,
)
