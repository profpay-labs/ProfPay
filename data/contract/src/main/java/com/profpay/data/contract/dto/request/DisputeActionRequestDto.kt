package com.profpay.data.contract.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DisputeActionRequestDto(
    @SerialName("dealId")
    val dealId: Long,
    @SerialName("initiatorUserId")
    val initiatorUserId: Long,
    @SerialName("contractAddress")
    val contractAddress: String,
    @SerialName("action")
    val action: String,
)
