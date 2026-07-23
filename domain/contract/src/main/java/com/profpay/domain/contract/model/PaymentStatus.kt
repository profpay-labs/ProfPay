package com.profpay.domain.contract.model

data class PaymentStatus(
    val buyerDepositAndExpertFeePaid: Boolean,
    val sellerExpertFeePaid: Boolean,
)
