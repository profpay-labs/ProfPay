package com.profpay.domain.wallet.model

import java.math.BigInteger

/**
 * Краткая информация о транзакции для списков.
 * Соответствует TransactionModel из core:database.
 */
data class TransactionSummary(
    val walletId: Long,
    val transactionId: Long,
    val txId: String,
    val senderAddress: String,
    val receiverAddress: String,
    val tokenName: String,
    val amount: BigInteger,
    val timestamp: Long,
    val transactionDate: String,
    val type: TransactionType,
    val statusCode: TransactionStatusCode,
    val isProcessed: Boolean,
)
