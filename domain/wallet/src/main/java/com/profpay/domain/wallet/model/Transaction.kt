package com.profpay.domain.wallet.model

import java.math.BigInteger

/**
 * Domain model транзакции.
 */
data class Transaction(
    val id: Long,
    val txId: String,
    val senderAddressId: Long?,
    val receiverAddressId: Long?,
    val senderAddress: String,
    val receiverAddress: String,
    val walletId: Long,
    val tokenName: String,
    val amount: BigInteger,
    val timestamp: Long,
    val status: String,
    val isProcessed: Boolean,
    val type: TransactionType,
    val statusCode: TransactionStatusCode,
    val commission: BigInteger,
)

enum class TransactionType(val code: Int) {
    RECEIVE(0),
    SEND(1),
    BETWEEN_YOURSELF(2),
    TRIGGER_SMART_CONTRACT(3),
    CENTRAL_ADDRESS(4),
    UNKNOWN(-1);

    companion object {
        fun fromCode(code: Int): TransactionType =
            entries.find { it.code == code } ?: UNKNOWN
    }
}

enum class TransactionStatusCode(val code: Int) {
    PENDING(0),
    SUCCESS(1),
    FAILED(2),
    UNKNOWN(3);

    companion object {
        fun fromCode(code: Int): TransactionStatusCode =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
