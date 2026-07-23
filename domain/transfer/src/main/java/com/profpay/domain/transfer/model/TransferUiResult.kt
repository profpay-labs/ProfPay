package com.profpay.domain.transfer.model

/**
 * Результат выполнения транзакции перевода.
 */
sealed class TransferUiResult {
    data object Success : TransferUiResult()

    data class Failure(
        val error: Throwable,
    ) : TransferUiResult()
}
