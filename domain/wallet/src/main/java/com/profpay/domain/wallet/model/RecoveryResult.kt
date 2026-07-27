package com.profpay.domain.wallet.model

/**
 * Результат восстановления кошелька по мнемонике.
 */
sealed class RecoveryResult {
    data class Success(
        val addressData: RecoveredAddressData,
        val accountWasFound: Boolean,
        val userId: Long? = null,
    ) : RecoveryResult()

    data object InvalidMnemonic : RecoveryResult()
    data object RepeatingMnemonic : RecoveryResult()
    data object AddressNotFound : RecoveryResult()
    data class Error(val throwable: Throwable) : RecoveryResult()
    data object Empty : RecoveryResult()
}

/**
 * Данные восстановленного адреса.
 * Domain-модель, не зависящая от core:tron.
 */
data class RecoveredAddressData(
    val entropy: ByteArray,
    val addresses: List<AddressInfo>,
) {
    data class AddressInfo(
        val address: String,
        val publicKey: String,
        val sotIndex: Byte,
        val derivationIndex: Int,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RecoveredAddressData
        return entropy.contentEquals(other.entropy) && addresses == other.addresses
    }

    override fun hashCode(): Int {
        var result = entropy.contentHashCode()
        result = 31 * result + addresses.hashCode()
        return result
    }
}
