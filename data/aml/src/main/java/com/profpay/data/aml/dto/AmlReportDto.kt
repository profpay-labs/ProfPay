package com.profpay.data.aml.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для AML отчёта от API
 */
@Serializable
data class AmlReportDto(
    @SerialName("id")
    val id: Long,
    @SerialName("amlId")
    val amlId: String,
    @SerialName("riskScore")
    val riskScore: Double,
    @SerialName("status")
    val status: String,
    @SerialName("signals")
    val signals: List<AmlSignalDto>,
    @SerialName("createdAt")
    val createdAt: Long,
    @SerialName("requestedAt")
    val requestedAt: Long = 0,
)
