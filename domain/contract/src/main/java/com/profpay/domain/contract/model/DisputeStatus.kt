package com.profpay.domain.contract.model

data class DisputeStatus(
    val decisionAdmin: String,
    val amountToSeller: Long,
    val amountToBuyer: Long,
    val adminsAgreed: List<String>,
    val sellerAgreed: Boolean,
    val buyerAgreed: Boolean,
    val adminAgreedVoted: Int,
    val adminsDeclined: List<String>,
    val sellerDeclined: Boolean,
    val buyerDeclined: Boolean,
    val adminDeclinedVoted: Int,
)
