package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentStatusDto(
    @SerialName("buyerDepositAndExpertFeePaid")
    val buyerDepositAndExpertFeePaid: Boolean,
    @SerialName("sellerExpertFeePaid")
    val sellerExpertFeePaid: Boolean,
)
