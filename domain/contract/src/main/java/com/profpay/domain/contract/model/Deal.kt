package com.profpay.domain.contract.model

data class Deal(
    val dealId: Long,
    val blockchainDealId: Long,
    val contractId: Long,
    val contractAddress: String,
    val amount: Long,
    val buyer: DealParticipant,
    val seller: DealParticipant,
    val admins: List<DealParticipant>,
    val blockchainData: DealBlockchainData?,
    val disputeStatus: DisputeStatus?,
)
