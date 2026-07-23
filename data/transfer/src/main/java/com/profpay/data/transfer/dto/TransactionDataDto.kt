package com.profpay.data.transfer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDataDto(
    @SerialName("address")
    val address: String,
    @SerialName("receiverAddress")
    val receiverAddress: String,
    @SerialName("amount")
    val amount: String,
    @SerialName("bandwidthRequired")
    val bandwidthRequired: Long,
    @SerialName("estimateEnergy")
    val estimateEnergy: Long,
    @SerialName("txnBytes")
    val txnBytes: String,
)
