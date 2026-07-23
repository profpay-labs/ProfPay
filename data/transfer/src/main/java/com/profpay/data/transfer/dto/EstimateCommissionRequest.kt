package com.profpay.data.transfer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request для оценки комиссии
 */
@Serializable
data class EstimateCommissionRequest(
    @SerialName("userId")
    val userId: Long,
    @SerialName("address")
    val address: String,
    @SerialName("energyRequired")
    val energyRequired: Long,
    @SerialName("bandwidthRequired")
    val bandwidthRequired: Long,
)
