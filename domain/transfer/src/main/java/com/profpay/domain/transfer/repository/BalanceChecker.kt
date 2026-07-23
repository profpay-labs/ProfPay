package com.profpay.domain.transfer.repository

import java.math.BigDecimal

/**
 * Абстракция для проверки балансов и состояния адресов.
 * Реализация в data-слое (через Tron SDK).
 */
interface BalanceChecker {
    suspend fun isAddressActivated(address: String): Boolean
    suspend fun getTrxBalance(address: String): BigDecimal
}
