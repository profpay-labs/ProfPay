package com.profpay.data.transfer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommissionCategoryDto(
    @SerialName("type")
    val type: String,
    @SerialName("amount")
    val amount: String,
    @SerialName("description")
    val description: String? = null,
)
