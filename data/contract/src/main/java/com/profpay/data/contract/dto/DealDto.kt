package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DealDto(
    @SerialName("dealId")
    val dealId: Long,
    @SerialName("blockchainDealId")
    val blockchainDealId: Long,
    @SerialName("contractId")
    val contractId: Long,
    @SerialName("contractAddress")
    val contractAddress: String,
    @SerialName("amount")
    val amount: Long,
    @SerialName("buyer")
    val buyer: DealParticipantDto,
    @SerialName("seller")
    val seller: DealParticipantDto,
    @SerialName("admins")
    val admins: List<DealParticipantDto>,
    @SerialName("blockchainData")
    val blockchainData: DealBlockchainDataDto? = null,
    @SerialName("disputeStatus")
    val disputeStatus: DisputeStatusDto? = null,
)
