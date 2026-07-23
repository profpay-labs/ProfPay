package com.profpay.core.security.model

/**
 * Результат шифрования: зашифрованные данные + IV.
 */
data class CipherData(
    val iv: ByteArray,
    val cipherText: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CipherData
        return iv.contentEquals(other.iv) && cipherText.contentEquals(other.cipherText)
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + cipherText.contentHashCode()
        return result
    }
}
