package com.profpay.data.contract.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DealStatusChangeRequestDto(
    @SerialName("dealId")
    val dealId: Long,
    @SerialName("userId")
    val userId: Long,
    @SerialName("contractAddress")
    val contractAddress: String,
    @SerialName("blockchainDealId")
    val blockchainDealId: Long,
    @SerialName("changeStatus")
    val changeStatus: String,
)
