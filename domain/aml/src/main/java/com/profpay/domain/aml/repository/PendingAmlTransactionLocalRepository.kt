package com.profpay.domain.aml.repository

import com.profpay.domain.aml.model.local.PendingAmlTransactionLocal

/**
 * Локальный репозиторий ожидающих AML транзакций.
 */
interface PendingAmlTransactionLocalRepository {

    /**
     * Вставить новую ожидающую транзакцию.
     * @return ID вставленной записи
     */
    suspend fun insert(pendingTransaction: PendingAmlTransactionLocal): Long

    /**
     * Отметить транзакцию как успешную.
     */
    suspend fun markAsSuccessful(txId: String)

    /**
     * Отметить транзакцию как ошибочную.
     */
    suspend fun markAsError(txId: String)

    /**
     * Проверить, существует ли ожидающая транзакция.
     */
    suspend fun exists(txId: String): Boolean
}
