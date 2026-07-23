package com.profpay.data.config.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeeConfigurationDto(
    @SerialName("trxFeeAddress")
    val trxFeeAddress: String,
    @SerialName("amlFee")
    val amlFee: Long,
    @SerialName("timestamp")
    val timestamp: Long,
)
