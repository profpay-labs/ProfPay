package com.profpay.core.tron.model

import com.google.protobuf.ByteString
import java.math.BigInteger

/**
 * Результат генерации нового кошелька.
 * Не содержит типов из внешних библиотек — безопасно использовать в UI.
 */
data class AddressGenerateResult(
    val addressesWithKeysForM: AddressesWithKeysForM,
    val mnemonicWords: List<String>, // Список слов мнемоники
    val mnemonicPhrase: String,      // Полная фраза через пробел (для копирования)
)

/**
 * Данные адресов с ключами для хранения.
 */
data class AddressesWithKeysForM(
    val addresses: List<AddressDataWithoutPrivKey>,
    val entropy: ByteArray,
    val derivedIndices: List<Int>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AddressesWithKeysForM
        if (addresses != other.addresses) return false
        if (!entropy.contentEquals(other.entropy)) return false
        if (derivedIndices != other.derivedIndices) return false
        return true
    }

    override fun hashCode(): Int {
        var result = addresses.hashCode()
        result = 31 * result + entropy.contentHashCode()
        result = 31 * result + derivedIndices.hashCode()
        return result
    }
}

/**
 * Данные адреса без приватного ключа.
 */
data class AddressDataWithoutPrivKey(
    val address: String,
    val publicKey: String,
    val indexDerivationSot: Int,
    val indexSot: Byte,
)

/**
 * Данные адреса с приватным ключом (для central address).
 */
data class AddressDataWithPrivKey(
    val address: String,
    val publicKey: String,
    val privateKey: String,
)

/**
 * Результат восстановления кошелька из seed-фразы.
 */
data class AddressGenerateFromSeedPhr(
    val addressesWithKeysForM: AddressesWithKeysForM,
)

data class BandwidthEstimate(val bandwidth: Long)

data class EnergyEstimate(
    val energy: Long,
    val energyInTrx: BigInteger,
)

data class SignedTransactionData(
    val txid: String,
    val signedTxn: ByteString?,
)

