package com.profpay.domain.transfer.model.local

import java.math.BigInteger

/**
 * Локальная модель ожидающей транзакции.
 */
data class PendingTransactionLocal(
    val id: Long? = null,
    val tokenId: Long,
    val txId: String,
    val amount: BigInteger,
    val timestamp: Long = System.currentTimeMillis(),
    val ttlMillis: Long = DEFAULT_TTL_MILLIS,
) {
    /**
     * Время истечения транзакции.
     */
    val expirationTime: Long
        get() = timestamp + ttlMillis

    /**
     * Проверить, истекла ли транзакция.
     */
    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean =
        currentTime > expirationTime

    companion object {
        const val DEFAULT_TTL_MILLIS = 15 * 60 * 1000L // 15 минут
    }
}
