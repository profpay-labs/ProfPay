package com.profpay.domain.wallet.model.local

import java.math.BigInteger

/**
 * Адрес с токенами.
 */
data class AddressWithTokensLocal(
    val address: WalletAddressLocal,
    val tokens: List<TokenBalanceLocal>,
) {
    val totalBalance: BigInteger
        get() = tokens.sumOf { it.balance }
}

/**
 * Баланс токена.
 */
data class TokenBalanceLocal(
    val tokenId: Long,
    val tokenName: String,
    val balance: BigInteger,
    val frozenBalance: BigInteger,
) {
    val availableBalance: BigInteger
        get() = balance - frozenBalance
}
