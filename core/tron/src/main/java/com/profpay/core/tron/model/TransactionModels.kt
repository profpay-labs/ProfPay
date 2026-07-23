package com.profpay.core.tron.model

/**
 * Результат отправки транзакции.
 */
sealed class TransactionResult {
    data class Success(
        val txId: String,
        val blockNumber: Long? = null,
    ) : TransactionResult()

    data class Failure(
        val errorCode: String,
        val errorMessage: String,
    ) : TransactionResult()
}
