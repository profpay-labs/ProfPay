package com.profpay.domain.contract.model

data class AgreementStatus(
    val sellerAgreed: Boolean,
    val buyerAgreed: Boolean,
    val disputed: Boolean,
)
