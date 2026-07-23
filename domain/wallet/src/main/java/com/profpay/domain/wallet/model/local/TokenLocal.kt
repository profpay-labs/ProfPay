package com.profpay.domain.wallet.model.local

import java.math.BigInteger

/**
 * Локальная модель токена.
 */
data class TokenLocal(
    val id: Long? = null,
    val addressId: Long,
    val tokenName: String,
    val balance: BigInteger = BigInteger.ZERO,
    val frozenBalance: BigInteger = BigInteger.ZERO,
) {
    /**
     * Доступный баланс (без замороженных средств).
     */
    val availableBalance: BigInteger
        get() = balance - frozenBalance
}
