package com.profpay.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request для обновления derived index SOT-адреса
 */
@Serializable
data class UpdateDerivedIndexRequest(
    @SerialName("appId")
    val appId: String,
    @SerialName("oldSotAddress")
    val oldSotAddress: String,
    @SerialName("newSotAddress")
    val newSotAddress: SotAddressDto,
    @SerialName("generalAddress")
    val generalAddress: GeneralAddressUpdateDto,
)
