package com.profpay.domain.wallet

import kotlinx.coroutines.flow.StateFlow

/**
 * Менеджер активного кошелька.
 * Single Source of Truth для текущего выбранного walletId.
 *
 * Интерфейс в domain-слое, реализация в app/data.
 */
interface ActiveWalletManager {

    /**
     * Flow текущего walletId для реактивных подписок.
     */
    val activeWalletIdFlow: StateFlow<Long>

    /**
     * Синхронный доступ к текущему walletId.
     * Использовать только когда Flow невозможен (например, в Interceptor).
     */
    val activeWalletId: Long

    /**
     * Устанавливает активный кошелёк.
     */
    suspend fun setActiveWallet(walletId: Long)

    /**
     * Проверяет, есть ли активный кошелёк.
     */
    fun hasActiveWallet(): Boolean

    companion object {
        /** Значение по умолчанию для первого кошелька */
        const val DEFAULT_WALLET_ID = 1L

        /** Значение, означающее отсутствие кошелька */
        const val NO_WALLET_ID = -1L
    }
}
