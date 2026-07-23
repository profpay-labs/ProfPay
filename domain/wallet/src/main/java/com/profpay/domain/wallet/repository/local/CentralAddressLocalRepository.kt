package com.profpay.domain.wallet.repository.local

import com.profpay.domain.wallet.model.local.CentralAddressLocal
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

/**
 * Локальный репозиторий центрального адреса.
 */
interface CentralAddressLocalRepository {

    /**
     * Вставить новый центральный адрес.
     * @return ID вставленной записи
     */
    suspend fun insert(centralAddress: CentralAddressLocal): Long

    /**
     * Получить центральный адрес.
     */
    suspend fun get(): CentralAddressLocal?

    /**
     * Наблюдать за центральным адресом.
     */
    fun observe(): Flow<CentralAddressLocal?>

    /**
     * Обновить баланс TRX.
     */
    suspend fun updateTrxBalance(balance: BigInteger)

    /**
     * Изменить центральный адрес.
     */
    suspend fun change(
        address: String,
        publicKey: String,
        privateKey: String,
    )

    /**
     * Проверить, существует ли центральный адрес.
     */
    suspend fun exists(): Boolean
}
