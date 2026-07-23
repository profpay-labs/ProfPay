package com.profpay.core.tron.model

/**
 * Результат деривации ключа — содержит только необходимые публичные данные.
 * Приватный ключ не возвращается наружу для безопасности.
 */
data class DerivedKeyData(
    val publicKeyHex: String,
    val publicKeyBytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DerivedKeyData
        return publicKeyHex == other.publicKeyHex
    }

    override fun hashCode(): Int = publicKeyHex.hashCode()
}
