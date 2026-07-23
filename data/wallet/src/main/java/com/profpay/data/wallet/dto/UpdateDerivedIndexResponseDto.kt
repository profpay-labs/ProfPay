package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response от обновления derived index
 */
@Serializable
data class UpdateDerivedIndexResponseDto(
    @SerialName("timestamp")
    val timestamp: String,
)
