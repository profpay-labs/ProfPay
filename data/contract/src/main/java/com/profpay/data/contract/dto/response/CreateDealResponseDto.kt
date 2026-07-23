package com.profpay.data.contract.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response от создания сделки
 */
@Serializable
data class CreateDealResponseDto(
    @SerialName("dealId")
    val dealId: Long,
    @SerialName("contractAddress")
    val contractAddress: String,
    @SerialName("arbiterAddresses")
    val arbiterAddresses: List<String>,
    @SerialName("timestamp")
    val timestamp: Long,
)
