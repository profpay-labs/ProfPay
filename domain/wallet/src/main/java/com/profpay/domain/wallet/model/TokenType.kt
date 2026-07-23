package com.profpay.domain.wallet.model

/**
 * Типы токенов, поддерживаемые приложением.
 * Domain-level enum без UI-зависимостей.
 */
enum class TokenType(
    val tokenName: String,
    val shortName: String,
    val blockchainName: String,
) {
    TRX(tokenName = "TRX", shortName = "TRX", blockchainName = "Tron"),
    USDT(tokenName = "USDT", shortName = "USDT", blockchainName = "Tron"),
    ;

    companion object {
        fun fromName(name: String): TokenType? =
            entries.find { it.tokenName == name || it.shortName == name }
    }
}
