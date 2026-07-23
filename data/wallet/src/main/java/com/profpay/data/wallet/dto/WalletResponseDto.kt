package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response от создания кошелька
 */
@Serializable
data class WalletResponseDto(
    @SerialName("id")
    val id: Long,
    @SerialName("userId")
    val userId: Long,
    @SerialName("addresses")
    val addresses: List<WalletAddressDto>,
)
