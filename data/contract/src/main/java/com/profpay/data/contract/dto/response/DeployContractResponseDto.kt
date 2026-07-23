package com.profpay.data.contract.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response от деплоя контракта
 */
@Serializable
data class DeployContractResponseDto(
    @SerialName("operationId")
    val operationId: Long,
    @SerialName("timestamp")
    val timestamp: Long,
)
