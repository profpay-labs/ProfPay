package com.profpay.domain.wallet.model.local

/**
 * Полная локальная модель профиля кошелька.
 */
data class WalletProfileLocal(
    val id: Long? = null,
    val name: String,
    val iv: ByteArray,
    val cipherText: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletProfileLocal

        if (id != other.id) return false
        if (name != other.name) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!cipherText.contentEquals(other.cipherText)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + cipherText.contentHashCode()
        return result
    }
}

/**
 * Краткая информация о профиле кошелька (для списков).
 */
data class WalletProfileSummary(
    val id: Long,
    val name: String,
)

/**
 * Зашифрованные данные кошелька.
 */
data class WalletCipherData(
    val iv: ByteArray,
    val cipherText: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletCipherData

        if (!iv.contentEquals(other.iv)) return false
        if (!cipherText.contentEquals(other.cipherText)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + cipherText.contentHashCode()
        return result
    }
}
