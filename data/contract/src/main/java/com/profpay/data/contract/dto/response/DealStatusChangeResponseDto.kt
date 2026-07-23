package com.profpay.data.contract.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DealStatusChangeResponseDto(
    @SerialName("dealId")
    val dealId: Long,
    @SerialName("newStatus")
    val newStatus: String,
    @SerialName("buyerNotified")
    val buyerNotified: Boolean,
    @SerialName("sellerNotified")
    val sellerNotified: Boolean,
    @SerialName("timestamp")
    val timestamp: Long,
)
