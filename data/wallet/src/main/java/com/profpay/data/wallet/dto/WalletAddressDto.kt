package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletAddressDto(
    @SerialName("id")
    val id: Long,
    @SerialName("address")
    val address: String,
    @SerialName("publicKey")
    val publicKey: String,
    @SerialName("type")
    val type: String,
    @SerialName("derivedIndices")
    val derivedIndices: List<Int> = emptyList(),
    @SerialName("sot")
    val sot: SotInfoDto? = null,
    @SerialName("parentAddressId")
    val parentAddressId: Long? = null,
    @SerialName("active")
    val active: Boolean,
)
