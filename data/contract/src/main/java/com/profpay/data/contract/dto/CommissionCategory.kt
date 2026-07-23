package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommissionCategory(
    @SerialName("type")
    val type: String,
    @SerialName("amount")
    val amount: Long,
    @SerialName("description")
    val description: String,
)
