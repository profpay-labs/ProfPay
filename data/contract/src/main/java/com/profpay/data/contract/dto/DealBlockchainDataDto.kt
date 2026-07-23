package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DealBlockchainDataDto(
    @SerialName("seller")
    val seller: String,
    @SerialName("buyer")
    val buyer: String,
    @SerialName("amount")
    val amount: Long,
    @SerialName("score")
    val score: Int,
    @SerialName("ended")
    val ended: Boolean,
    @SerialName("totalExpertCommissions")
    val totalExpertCommissions: Long,
    @SerialName("paymentStatus")
    val paymentStatus: PaymentStatusDto,
    @SerialName("agreementStatus")
    val agreementStatus: AgreementStatusDto,
)
