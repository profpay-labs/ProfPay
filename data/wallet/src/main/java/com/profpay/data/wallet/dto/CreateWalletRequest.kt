package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request для создания кошелька
 */
@Serializable
data class CreateWalletRequest(
    @SerialName("appId")
    val appId: String,
    @SerialName("generalAddress")
    val generalAddress: GeneralAddressDto,
    @SerialName("centralAddress")
    val centralAddress: CentralAddressDto,
    @SerialName("sotAddresses")
    val sotAddresses: List<SotAddressDto>,
)
