package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigInteger

@Serializable
data class CommissionData(
    @SerialName("address")
    val address: String,
    @SerialName("amount")
    val amount: String,
    @SerialName("bandwidthRequired")
    val bandwidthRequired: Long = 0,
    @SerialName("txnBytes")
    val txnBytes: String,
)
