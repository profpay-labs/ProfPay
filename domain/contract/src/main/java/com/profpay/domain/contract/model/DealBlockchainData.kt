package com.profpay.domain.contract.model

data class DealBlockchainData(
    val seller: String,
    val buyer: String,
    val amount: Long,
    val score: Int,
    val ended: Boolean,
    val totalExpertCommissions: Long,
    val paymentStatus: PaymentStatus,
    val agreementStatus: AgreementStatus,
)
