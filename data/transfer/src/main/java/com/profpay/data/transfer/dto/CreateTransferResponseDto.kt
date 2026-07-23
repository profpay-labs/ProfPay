package com.profpay.data.transfer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response от создания перевода
 */
@Serializable
data class CreateTransferResponseDto(
    @SerialName("operationId")
    val operationId: Long,
    @SerialName("timestamp")
    val timestamp: Long,
)
