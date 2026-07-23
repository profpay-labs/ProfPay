package com.profpay.data.aml.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body для обновления AML отчёта
 */
@Serializable
data class RenewAmlReportRequest(
    @SerialName("address")
    val address: String,
    @SerialName("userId")
    val userId: Long,
    @SerialName("tokenName")
    val tokenName: String = "USDT",
)
