package com.profpay.data.contract.dto.response

import com.profpay.data.contract.dto.DealDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDealsResponseDto(
    @SerialName("deals")
    val deals: List<DealDto>,
    @SerialName("timestamp")
    val timestamp: Long,
)
