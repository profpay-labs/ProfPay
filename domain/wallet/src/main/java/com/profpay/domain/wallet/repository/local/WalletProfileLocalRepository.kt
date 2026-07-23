package com.profpay.domain.wallet.repository.local

import com.profpay.domain.wallet.model.local.WalletCipherData
import com.profpay.domain.wallet.model.local.WalletProfileLocal
import com.profpay.domain.wallet.model.local.WalletProfileSummary
import kotlinx.coroutines.flow.Flow

/**
 * Локальный репозиторий профилей кошелька.
 */
interface WalletProfileLocalRepository {

    /**
     * Вставить новый профиль кошелька.
     * Имя генерируется автоматически ("Wallet N").
     * @return ID вставленной записи
     */
    suspend fun insert(walletProfile: WalletProfileLocal): Long

    /**
     * Получить имя кошелька по ID.
     */
    suspend fun getNameById(walletId: Long): String?

    /**
     * Наблюдать за списком всех кошельков.
     */
    fun observeAll(): Flow<List<WalletProfileSummary>>

    /**
     * Получить количество профилей.
     */
    suspend fun getCount(): Long

    /**
     * Обновить имя кошелька.
     */
    suspend fun updateName(id: Long, newName: String)

    /**
     * Удалить профиль кошелька.
     */
    suspend fun delete(id: Long)

    /**
     * Проверить, есть ли хотя бы один профиль.
     */
    suspend fun hasAny(): Boolean

    /**
     * Получить зашифрованные данные кошелька.
     */
    suspend fun getCipherData(id: Long): WalletCipherData
}
