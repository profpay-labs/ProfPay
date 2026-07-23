package com.profpay.data.wallet.repository.local

import com.profpay.core.database.dao.wallet.WalletProfileDao
import com.profpay.data.wallet.mapper.WalletProfileMapper.toDomain
import com.profpay.data.wallet.mapper.WalletProfileMapper.toEntity
import com.profpay.data.wallet.mapper.WalletProfileMapper.toSummary
import com.profpay.domain.wallet.model.local.WalletCipherData
import com.profpay.domain.wallet.model.local.WalletProfileLocal
import com.profpay.domain.wallet.model.local.WalletProfileSummary
import com.profpay.domain.wallet.repository.local.WalletProfileLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletProfileLocalRepositoryImpl @Inject constructor(
    private val walletProfileDao: WalletProfileDao,
) : WalletProfileLocalRepository {

    override suspend fun insert(walletProfile: WalletProfileLocal): Long {
        val number = getCount() + 1
        val entityWithName = walletProfile.toEntity().copy(name = "Wallet $number")
        return walletProfileDao.insert(entityWithName)
    }

    override suspend fun getNameById(walletId: Long): String? {
        return walletProfileDao.getWalletNameById(walletId)
    }

    override fun observeAll(): Flow<List<WalletProfileSummary>> {
        return walletProfileDao.getListAllWalletsFlow()
            .map { list -> list.map { it.toSummary() } }
    }

    override suspend fun getCount(): Long {
        return walletProfileDao.getCountRecords()
    }

    override suspend fun updateName(id: Long, newName: String) {
        walletProfileDao.updateNameById(id, newName)
    }

    override suspend fun delete(id: Long) {
        walletProfileDao.deleteWalletProfile(id)
    }

    override suspend fun hasAny(): Boolean {
        return walletProfileDao.hasAnyWalletProfile()
    }

    override suspend fun getCipherData(id: Long): WalletCipherData {
        return walletProfileDao.getWalletCipherData(id).toDomain()
    }
}
