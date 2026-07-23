package com.profpay.data.aml.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request для создания AML платежа
 */
@Serializable
data class CreateAmlPaymentRequest(
    @SerialName("userId")
    val userId: Long,
    @SerialName("tx")
    val txHash: String,
    @SerialName("address")
    val address: String,
    @SerialName("paymentAddress")
    val paymentAddress: String,
    @SerialName("bandwidthRequired")
    val bandwidthRequired: Long,
    @SerialName("txnBytes")
    val txnBytes: String,
)
