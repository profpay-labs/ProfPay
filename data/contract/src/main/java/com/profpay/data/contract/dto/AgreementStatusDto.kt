package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgreementStatusDto(
    @SerialName("sellerAgreed")
    val sellerAgreed: Boolean,
    @SerialName("buyerAgreed")
    val buyerAgreed: Boolean,
    @SerialName("disputed")
    val disputed: Boolean,
)
