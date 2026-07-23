package com.profpay.domain.contract.repository

import com.profpay.domain.contract.model.local.SmartContractLocal
import kotlinx.coroutines.flow.Flow

/**
 * Локальный репозиторий смарт-контрактов.
 */
interface SmartContractLocalRepository {

    /**
     * Вставить новый смарт-контракт.
     * @return ID вставленной записи
     */
    suspend fun insert(smartContract: SmartContractLocal): Long

    /**
     * Получить смарт-контракт.
     */
    suspend fun get(): SmartContractLocal?

    /**
     * Наблюдать за смарт-контрактом.
     */
    fun observe(): Flow<SmartContractLocal?>

    /**
     * Восстановить смарт-контракт (обновить адрес контракта).
     */
    suspend fun restore(contractAddress: String)
}
