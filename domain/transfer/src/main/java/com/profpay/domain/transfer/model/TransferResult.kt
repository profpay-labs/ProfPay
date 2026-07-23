package com.profpay.domain.transfer.model

/**
 * Результат создания перевода
 */
data class TransferResult(
    val operationId: Long,
    val timestampSeconds: Long,
)
