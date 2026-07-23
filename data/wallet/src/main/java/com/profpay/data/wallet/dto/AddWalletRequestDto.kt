package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddWalletRequestDto(
    @SerialName("generalAddress")
    val generalAddress: GeneralAddressDto,
    @SerialName("sotAddresses")
    val sotAddresses: List<SotAddressDto> = emptyList(),
)
