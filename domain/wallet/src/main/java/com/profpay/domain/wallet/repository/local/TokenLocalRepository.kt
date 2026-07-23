package com.profpay.domain.wallet.repository.local

import com.profpay.domain.wallet.model.local.TokenLocal
import java.math.BigInteger

/**
 * Локальный репозиторий токенов.
 */
interface TokenLocalRepository {

    /**
     * Вставить новый токен.
     * @return ID вставленной записи
     */
    suspend fun insert(token: TokenLocal): Long

    /**
     * Обновить баланс токена.
     */
    suspend fun updateBalance(
        addressId: Long,
        tokenName: String,
        balance: BigInteger,
    )

    /**
     * Получить ID токена по addressId и имени токена.
     */
    suspend fun getTokenId(
        addressId: Long,
        tokenName: String,
    ): Long
}
