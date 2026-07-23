package com.profpay.data.wallet.repository.local

import com.profpay.core.database.dao.wallet.AddressDao
import com.profpay.data.wallet.mapper.AddressMapper.toEntity
import com.profpay.data.wallet.mapper.AddressMapper.toLocal
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.model.local.WalletAddressLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressLocalRepositoryImpl @Inject constructor(
    private val addressDao: AddressDao,
) : AddressLocalRepository {

    // ══════════════════════════════════════════════════════════════════════
    // CRUD операции
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun insert(address: WalletAddressLocal): Long {
        return addressDao.insert(address.toEntity())
    }

    override suspend fun updateSotIndex(addressId: Long, index: Int) {
        addressDao.updateSotIndexByAddressId(index.toByte(), addressId)
    }

    // ══════════════════════════════════════════════════════════════════════
    // Получение адресов
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun getByAddress(address: String): WalletAddressLocal? {
        return addressDao.getAddressEntityByAddress(address)?.toLocal()
    }

    override suspend fun getById(id: Long): WalletAddressLocal? {
        return addressDao.getAddressEntityById(id)?.toLocal()
    }

    override fun observeByAddress(address: String): Flow<WalletAddressLocal> {
        return addressDao.getAddressEntityByAddressFlow(address).map { it.toLocal() }
    }

    override suspend fun isGeneralAddress(address: String): Boolean {
        return addressDao.isGeneralAddress(address)
    }

    // ══════════════════════════════════════════════════════════════════════
    // General Address
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun getGeneralAddressByWalletId(walletId: Long): String {
        return addressDao.getGeneralAddressByWalletId(walletId)
    }

    override suspend fun getGeneralPublicKeyByWalletId(walletId: Long): String {
        return addressDao.getGeneralPublicKeyByWalletId(walletId)
    }

    override suspend fun getGeneralAddressEntityByWalletId(walletId: Long): WalletAddressLocal {
        return addressDao.getGeneralAddressEntityByWalletId(walletId).toLocal()
    }

    override suspend fun getAllGeneralAddresses(): List<WalletAddressLocal> {
        return addressDao.getGeneralAddresses().map { it.toLocal() }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Address with Tokens
    // ══════════════════════════════════════════════════════════════════════

    override fun observeSotsWithTokensByBlockchain(
        walletId: Long,
        blockchainName: String,
    ): Flow<List<AddressWithTokensLocal>> {
        return addressDao.getAddressesSotsWithTokensByBlockchainFlow(walletId, blockchainName)
            .map { list -> list.map { it.toLocal() } }
    }

    override suspend fun getSotsWithTokensByBlockchain(
        blockchainName: String,
        walletId: Long,
    ): List<AddressWithTokensLocal> {
        return addressDao.getAddressesSotsWithTokensByBlockchain(blockchainName, walletId)
            .map { it.toLocal() }
    }

    override suspend fun getAllSotsWithTokensByBlockchain(
        blockchainName: String,
    ): List<AddressWithTokensLocal> {
        return addressDao.getAddressesSotsWithTokensByBlockchain(blockchainName)
            .map { it.toLocal() }
    }

    override fun observeAllSotsWithTokens(walletId: Long): Flow<List<AddressWithTokensLocal>> {
        return addressDao.getAddressesSotsWithTokensFlow(walletId)
            .map { list -> list.map { it.toLocal() } }
    }

    override fun observeGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): Flow<AddressWithTokensLocal> {
        return addressDao.getGeneralAddressWithTokensFlow(addressId, blockchainName)
            .map { it.toLocal() }
    }

    override suspend fun getGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): AddressWithTokensLocal {
        return addressDao.getGeneralAddressWithTokens(addressId, blockchainName).toLocal()
    }

    override fun observeAddressWithTokens(
        addressId: Long,
        blockchainName: String,
    ): Flow<AddressWithTokensLocal> {
        return addressDao.getAddressWithTokensFlow(addressId, blockchainName)
            .map { it.toLocal() }
    }

    override fun observeAddressWithTokensByAddress(address: String): Flow<AddressWithTokensLocal> {
        return addressDao.getAddressWithTokensByAddressFlow(address)
            .map { it.toLocal() }
    }

    override fun observeArchivalAddressesWithTokens(
        walletId: Long,
        blockchainName: String,
    ): Flow<List<AddressWithTokensLocal>> {
        return addressDao.getAddressesWithTokensArchivalByBlockchainFlow(walletId, blockchainName)
            .map { list -> list.map { it.toLocal() } }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Derivation indices
    // ══════════════════════════════════════════════════════════════════════

    override suspend fun getMaxSotDerivationIndex(walletId: Long): Int {
        return addressDao.getMaxSotDerivationIndex(walletId)
    }

    override suspend fun getSortedDerivationIndices(walletId: Long): List<Int> {
        return addressDao.getSortedDerivationIndices(walletId)
    }
}
