package com.profpay.domain.wallet.model

/**
 * Domain-модель для адресов кошелька при создании.
 */
data class WalletAddressesData(
    val entropy: ByteArray,
    val addresses: List<AddressData>,
) {
    data class AddressData(
        val address: String,
        val publicKey: String,
        val sotIndex: Byte,
        val derivationIndex: Int,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as WalletAddressesData
        if (!entropy.contentEquals(other.entropy)) return false
        if (addresses != other.addresses) return false
        return true
    }

    override fun hashCode(): Int {
        var result = entropy.contentHashCode()
        result = 31 * result + addresses.hashCode()
        return result
    }
}
