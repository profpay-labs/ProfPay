package com.profpay.wallet.presentation.viewmodel.dto

import com.profpay.domain.wallet.model.TokenType
import com.profpay.wallet.R

/**
 * UI-представление токена с иконками.
 * Маппится из domain TokenType.
 */
enum class TokenName(
    val tokenType: TokenType,
    val paintIconId: Int,
) {
    TRX(TokenType.TRX, R.drawable.trx_tron),
    USDT(TokenType.USDT, R.drawable.usdt_tron),
    ;

    // Делегируем в domain
    val tokenName: String get() = tokenType.tokenName
    val shortName: String get() = tokenType.shortName
    val blockchainName: String get() = tokenType.blockchainName

    companion object {
        fun fromTokenType(type: TokenType): TokenName =
            entries.first { it.tokenType == type }
    }
}

enum class BlockchainName(
    val blockchainName: String,
    val tokens: List<TokenName>,
) {
    TRON("Tron", tokens = listOf(TokenName.TRX, TokenName.USDT)),
}
