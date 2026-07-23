package com.profpay.data.transfer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response от оценки комиссии
 */
@Serializable
data class EstimateCommissionResponseDto(
    @SerialName("commission")
    val commission: String,
    @SerialName("categories")
    val categories: List<CommissionCategoryDto>,
    @SerialName("timestamp")
    val timestamp: Long,
)
