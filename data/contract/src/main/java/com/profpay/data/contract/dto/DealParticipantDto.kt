package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DealParticipantDto(
    @SerialName("userId")
    val userId: Long,
    @SerialName("telegramId")
    val telegramId: Long,
    @SerialName("username")
    val username: String,
    @SerialName("walletAddress")
    val walletAddress: String,
    @SerialName("tier")
    val tier: UserTierDto,
)
