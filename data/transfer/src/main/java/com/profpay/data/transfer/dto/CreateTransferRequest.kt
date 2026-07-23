package com.profpay.data.transfer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request для создания перевода
 */
@Serializable
data class CreateTransferRequest(
    @SerialName("userId")
    val userId: Long,
    @SerialName("txId")
    val txId: String,
    @SerialName("token")
    val token: String,
    @SerialName("transactionData")
    val transactionData: TransactionDataDto,
    @SerialName("commissionData")
    val commissionData: TransferCommissionDataDto,
)
