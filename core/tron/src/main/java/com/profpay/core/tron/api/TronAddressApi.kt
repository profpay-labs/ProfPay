package com.profpay.core.tron.api

import com.profpay.core.tron.model.AddressDataWithPrivKey
import com.profpay.core.tron.model.AddressDataWithoutPrivKey
import com.profpay.core.tron.model.AddressGenerateFromSeedPhr
import com.profpay.core.tron.model.AddressGenerateResult
import com.profpay.core.tron.model.DerivedKeyData
import org.bitcoinj.crypto.DeterministicKey
import java.math.BigInteger

/**
 * API для работы с TRON-адресами.
 * Все методы thread-safe, сетевые вызовы выполняются через TronNodeManager с failover.
 */
interface TronAddressApi {

    // ══════════════════════════════════════════════════════════════════════
    // Validation
    // ══════════════════════════════════════════════════════════════════════

    /** Проверяет валидность TRON-адреса (Base58Check, checksum). */
    fun isValidTronAddress(address: String): Boolean

    /** Проверяет, активирован ли адрес в блокчейне (имеет активные permissions). */
    suspend fun isAddressActivated(address: String): Boolean

    /** Проверяет, является ли адрес смарт-контрактом. */
    fun isContractAddress(address: String): Boolean

    // ══════════════════════════════════════════════════════════════════════
    // Balances
    // ══════════════════════════════════════════════════════════════════════

    /** Получает баланс TRX (в SUN). */
    fun getTrxBalance(address: String): BigInteger

    /** Получает баланс USDT TRC20 (в минимальных единицах). */
    fun getUsdtBalance(address: String): BigInteger

    // ══════════════════════════════════════════════════════════════════════
    // Chain Parameters
    // ══════════════════════════════════════════════════════════════════════

    /** Комиссия за создание нового аккаунта из chain parameters. */
    fun getCreateNewAccountFeeInSystemContract(): BigInteger

    // ══════════════════════════════════════════════════════════════════════
    // Key Derivation (BIP39/BIP44)
    // ══════════════════════════════════════════════════════════════════════

    /** Генерирует новый кошелёк: мнемоника + 7 адресов. */
    fun generateAddressAndMnemonic(): AddressGenerateResult

    fun deriveKeyAtIndex(entropy: ByteArray, index: Long): DerivedKeyData

    /** Генерирует один адрес с приватным ключом. */
    fun generateSingleAddress(): AddressDataWithPrivKey

    /** Получает главный адрес (index=0) из seed phrase. */
    fun getGeneralAddressBySeedPhrase(seed: String): String

    /** Восстанавливает адреса из seed phrase по известным индексам. */
    fun recoveryKeysAndAddressBySeedPhrase(
        seed: String,
        derivedIndices: List<Int>,
    ): AddressGenerateFromSeedPhr

    /** Генерирует следующую группу из 6 адресов (сканирует активированные). */
    suspend fun generateNextAddressGroup(seed: String): AddressGenerateFromSeedPhr

    /** Деривирует приватный ключ из entropy + index. */
    fun derivePrivateKeyFromEntropy(entropy: ByteArray, index: Int): ByteArray

    /** Деривирует приватный ключ (hex) из entropy + index. */
    fun deriveHexPrivateKeyFromEntropy(entropy: ByteArray, index: Int): String

    /** Преобразует entropy в seed phrase. */
    fun getSeedPhraseByEntropy(entropy: ByteArray): String

    // ══════════════════════════════════════════════════════════════════════
    // Address Conversion
    // ══════════════════════════════════════════════════════════════════════

    /** HEX → Base58Check address. */
    fun hexToBase58CheckAddress(hex: String): String

    /** Public key → TRON address. */
    fun publicKeyToAddress(publicKey: ByteArray): String?
}
