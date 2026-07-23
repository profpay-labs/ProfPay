package com.profpay.domain.transfer.model

/**
 * Параметры для создания перевода
 */
data class CreateTransferParams(
    val userId: Long,
    val txId: String,
    val token: TransferToken,
    val transactionData: TransactionData,
    val commissionData: TransferCommissionData,
)
