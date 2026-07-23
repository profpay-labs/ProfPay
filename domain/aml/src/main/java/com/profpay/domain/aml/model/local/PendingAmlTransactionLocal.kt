package com.profpay.domain.aml.model.local

/**
 * Локальная модель ожидающей AML транзакции.
 */
data class PendingAmlTransactionLocal(
    val id: Long? = null,
    val txId: String,
    val status: PendingAmlStatus = PendingAmlStatus.PENDING,
)

/**
 * Статус ожидающей AML транзакции.
 */
enum class PendingAmlStatus {
    PENDING,
    SUCCESS,
    ERROR,
}
