package com.profpay.data.contract.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response от вызова контракта
 */
@Serializable
data class CallContractResponseDto(
    @SerialName("operationId")
    val operationId: Long,
    @SerialName("timestamp")
    val timestamp: Long,
)
