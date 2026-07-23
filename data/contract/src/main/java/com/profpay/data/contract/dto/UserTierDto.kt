package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserTierDto(
    @SerialName("name")
    val name: String,
    @SerialName("code")
    val code: String,
    @SerialName("commissionPercent")
    val commissionPercent: Int,
)
