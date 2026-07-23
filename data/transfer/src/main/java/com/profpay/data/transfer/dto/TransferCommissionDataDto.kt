package com.profpay.data.transfer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransferCommissionDataDto(
    @SerialName("address")
    val address: String,
    @SerialName("amount")
    val amount: String,
    @SerialName("bandwidthRequired")
    val bandwidthRequired: Long,
    @SerialName("categories")
    val categories: List<CommissionCategoryDto>,
    @SerialName("txnBytes")
    val txnBytes: String,
)
