package com.profpay.core.security.exception

/**
 * Исключения криптографических операций.
 */
sealed class CryptoException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause) {

    /**
     * Ключ не найден в Keystore.
     */
    data class KeyNotFound(val alias: String) : CryptoException(
        message = "Key not found in Keystore: $alias"
    )

    /**
     * Ошибка расшифровки данных.
     */
    data class DecryptionFailed(
        val alias: String,
        override val cause: Throwable,
    ) : CryptoException(
        message = "Failed to decrypt data for alias: $alias",
        cause = cause,
    )
}
