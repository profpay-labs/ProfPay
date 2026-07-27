package com.profpay.domain.wallet.model

/**
 * Данные сгенерированного кошелька (новый кошелёк).
 */
data class GeneratedWalletData(
    val mnemonicPhrase: String,
    val mnemonicWords: List<String>,
    val entropy: ByteArray,
    val addresses: List<GeneratedAddress>,
) {
    data class GeneratedAddress(
        val address: String,
        val publicKey: String,
        val sotIndex: Byte,
        val derivationIndex: Int,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GeneratedWalletData
        return mnemonicPhrase == other.mnemonicPhrase &&
            mnemonicWords == other.mnemonicWords &&
            entropy.contentEquals(other.entropy) &&
            addresses == other.addresses
    }

    override fun hashCode(): Int {
        var result = mnemonicPhrase.hashCode()
        result = 31 * result + mnemonicWords.hashCode()
        result = 31 * result + entropy.contentHashCode()
        result = 31 * result + addresses.hashCode()
        return result
    }
}
