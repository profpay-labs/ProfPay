package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DisputeStatusDto(
    @SerialName("decisionAdmin")
    val decisionAdmin: String,
    @SerialName("amountToSeller")
    val amountToSeller: Long,
    @SerialName("amountToBuyer")
    val amountToBuyer: Long,
    @SerialName("adminsAgreed")
    val adminsAgreed: List<String>,
    @SerialName("sellerAgreed")
    val sellerAgreed: Boolean,
    @SerialName("buyerAgreed")
    val buyerAgreed: Boolean,
    @SerialName("adminAgreedVoted")
    val adminAgreedVoted: Int,
    @SerialName("adminsDeclined")
    val adminsDeclined: List<String>,
    @SerialName("sellerDeclined")
    val sellerDeclined: Boolean,
    @SerialName("buyerDeclined")
    val buyerDeclined: Boolean,
    @SerialName("adminDeclinedVoted")
    val adminDeclinedVoted: Int,
)
