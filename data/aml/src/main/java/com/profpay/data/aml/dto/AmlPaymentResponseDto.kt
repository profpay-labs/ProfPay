package com.profpay.data.aml.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response от создания AML платежа
 */
@Serializable
data class AmlPaymentResponseDto(
    @SerialName("operationId")
    val operationId: Long,
    @SerialName("status")
    val status: String,
    @SerialName("timestamp")
    val timestamp: Long,
)
