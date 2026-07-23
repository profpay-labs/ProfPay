package com.profpay.domain.wallet.repository.local

import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.model.local.WalletAddressLocal
import kotlinx.coroutines.flow.Flow

/**
 * Локальный репозиторий адресов кошелька.
 */
interface AddressLocalRepository {

    // ══════════════════════════════════════════════════════════════════════
    // CRUD операции
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Вставить новый адрес.
     * @return ID вставленного адреса
     */
    suspend fun insert(address: WalletAddressLocal): Long

    /**
     * Обновить SOT index адреса.
     */
    suspend fun updateSotIndex(addressId: Long, index: Int)

    // ══════════════════════════════════════════════════════════════════════
    // Получение адресов
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Получить адрес по строковому значению.
     */
    suspend fun getByAddress(address: String): WalletAddressLocal?

    /**
     * Получить адрес по ID.
     */
    suspend fun getById(id: Long): WalletAddressLocal?

    /**
     * Наблюдать за адресом.
     */
    fun observeByAddress(address: String): Flow<WalletAddressLocal>

    /**
     * Проверить, является ли адрес general.
     */
    suspend fun isGeneralAddress(address: String): Boolean

    // ══════════════════════════════════════════════════════════════════════
    // General Address
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Получить строковое значение general address по walletId.
     */
    suspend fun getGeneralAddressByWalletId(walletId: Long): String

    /**
     * Получить публичный ключ general address.
     */
    suspend fun getGeneralPublicKeyByWalletId(walletId: Long): String

    /**
     * Получить entity general address.
     */
    suspend fun getGeneralAddressEntityByWalletId(walletId: Long): WalletAddressLocal

    /**
     * Получить все general addresses.
     */
    suspend fun getAllGeneralAddresses(): List<WalletAddressLocal>

    // ══════════════════════════════════════════════════════════════════════
    // Address with Tokens
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Наблюдать за адресами SOT с токенами по blockchain.
     */
    fun observeSotsWithTokensByBlockchain(
        walletId: Long,
        blockchainName: String,
    ): Flow<List<AddressWithTokensLocal>>

    /**
     * Получить адреса SOT с токенами по blockchain.
     */
    suspend fun getSotsWithTokensByBlockchain(
        blockchainName: String,
        walletId: Long,
    ): List<AddressWithTokensLocal>

    /**
     * Получить все адреса SOT с токенами по blockchain (без walletId).
     */
    suspend fun getAllSotsWithTokensByBlockchain(
        blockchainName: String,
    ): List<AddressWithTokensLocal>

    /**
     * Наблюдать за всеми адресами SOT с токенами.
     */
    fun observeAllSotsWithTokens(walletId: Long): Flow<List<AddressWithTokensLocal>>

    /**
     * Наблюдать за general address с токенами.
     */
    fun observeGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): Flow<AddressWithTokensLocal>

    /**
     * Получить general address с токенами.
     */
    suspend fun getGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): AddressWithTokensLocal

    /**
     * Наблюдать за адресом с токенами по ID.
     */
    fun observeAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): Flow<AddressWithTokensLocal>

    /**
     * Наблюдать за адресом с токенами по строке адреса.
     */
    fun observeAddressWithTokensByAddress(address: String): Flow<AddressWithTokensLocal>

    /**
     * Наблюдать за архивными адресами с токенами.
     */
    fun observeArchivalAddressesWithTokens(
        walletId: Long,
        blockchainName: String,
    ): Flow<List<AddressWithTokensLocal>>

    // ══════════════════════════════════════════════════════════════════════
    // Derivation indices
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Получить максимальный derivation index для SOT.
     */
    suspend fun getMaxSotDerivationIndex(walletId: Long): Int

    /**
     * Получить отсортированные derivation indices.
     */
    suspend fun getSortedDerivationIndices(walletId: Long): List<Int>
}
