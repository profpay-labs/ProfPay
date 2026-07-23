package com.profpay.domain.transfer.repository

import com.profpay.domain.transfer.model.local.PendingTransactionLocal

/**
 * Локальный репозиторий ожидающих транзакций.
 */
interface PendingTransactionLocalRepository {

    /**
     * Вставить новую ожидающую транзакцию.
     * @return ID вставленной записи
     */
    suspend fun insert(pendingTransaction: PendingTransactionLocal): Long

    /**
     * Проверить, существует ли ожидающая транзакция по txId.
     */
    suspend fun exists(txId: String): Boolean

    /**
     * Удалить ожидающую транзакцию по txId.
     */
    suspend fun deleteByTxId(txId: String)

    /**
     * Получить истёкшие транзакции.
     * @param currentTime текущее время в миллисекундах
     */
    suspend fun getExpired(currentTime: Long): List<PendingTransactionLocal>
}
