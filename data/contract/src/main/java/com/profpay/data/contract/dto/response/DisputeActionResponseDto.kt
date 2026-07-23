package com.profpay.data.contract.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DisputeActionResponseDto(
    @SerialName("dealId")
    val dealId: Long,
    @SerialName("action")
    val action: String,
    @SerialName("participantsNotified")
    val participantsNotified: Int,
    @SerialName("timestamp")
    val timestamp: Long,
)
