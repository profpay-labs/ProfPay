// core/tron/src/main/java/com/profpay/core/tron/api/TronHttpApi.kt
package com.profpay.core.tron.api

import com.profpay.core.tron.model.Trc20TransactionData
import com.profpay.core.tron.model.TronTransaction
import com.profpay.core.tron.model.TrxTransactionData

/**
 * API для HTTP-запросов к TronGrid/TronScan.
 * Используется для получения истории транзакций и другой информации,
 * недоступной через gRPC.
 */
interface TronHttpApi {

    /**
     * Получает историю TRX-транзакций для адреса.
     *
     * @param address TRON адрес в Base58 формате
     * @param limit максимальное количество транзакций (по умолчанию 200)
     * @return Result со списком транзакций или ошибкой
     */
    suspend fun getTrxTransactions(
        address: String,
        limit: Int = 200,
    ): Result<List<TrxTransactionData>>

    /**
     * Получает историю TRC20-транзакций для адреса.
     *
     * @param address TRON адрес в Base58 формате
     * @param contractAddress адрес TRC20 контракта (null = все токены)
     * @param limit максимальное количество транзакций (по умолчанию 200)
     * @return Result со списком транзакций или ошибкой
     */
    suspend fun getTrc20Transactions(
        address: String,
        contractAddress: String? = null,
        limit: Int = 200,
    ): Result<List<Trc20TransactionData>>

    /**
     * Получает унифицированную историю всех транзакций.
     *
     * @param address TRON адрес
     * @param limit лимит на каждый тип транзакций
     * @return объединённый список транзакций, отсортированный по времени
     */
    suspend fun getAllTransactions(
        address: String,
        limit: Int = 100,
    ): Result<List<TronTransaction>>
}
