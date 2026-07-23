package com.profpay.data.aml.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AmlSignalDto(
    @SerialName("name")
    val name: String,
    @SerialName("percentage")
    val percentage: Double,
    @SerialName("riskLevel")
    val riskLevel: String,
)
