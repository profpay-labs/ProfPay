
package com.profpay.data.wallet.repository

import android.util.Log
import com.profpay.core.database.AppDatabase
import com.profpay.core.database.entities.wallet.AddressEntity
import com.profpay.core.database.entities.wallet.WalletProfileEntity
import com.profpay.core.network.exception.GrpcRequestException
import com.profpay.core.network.exception.GrpcResponseException
import com.profpay.core.security.CryptoManager
import com.profpay.domain.user.model.local.UserProfile
import com.profpay.domain.user.repository.UserRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.model.TokenType
import com.profpay.domain.wallet.model.WalletAddressesData
import com.profpay.domain.wallet.repository.WalletCreationRepository
import java.util.UUID
import javax.inject.Inject

class WalletCreationRepositoryImpl @Inject constructor(
    private val profileLocalRepository: ProfileLocalRepository,
    private val userRepository: UserRepository,
    private val cryptoManager: CryptoManager,
    private val database: AppDatabase,
) : WalletCreationRepository {

    override fun getWalletAlias(addressesData: WalletAddressesData): String? {
        return addressesData.addresses
            .firstOrNull { it.derivationIndex == 0 }
            ?.address
    }

    override suspend fun insertNewCryptoAddresses(addressesData: WalletAddressesData): Long {
        val walletAlias = getWalletAlias(addressesData)
            ?: throw IllegalStateException("Главный адрес кошелька не найден")

        val (iv, cipherText) = cryptoManager.encrypt(walletAlias, addressesData.entropy)

        val defaultTokenNames = TokenType.entries.map { it.tokenName }
        val blockchainName = TokenType.TRX.blockchainName

        val addressList = addressesData.addresses.map { addr ->
            AddressEntity(
                walletId = 0,
                blockchainName = blockchainName,
                address = addr.address,
                publicKey = addr.publicKey,
                isGeneralAddress = addr.derivationIndex == 0,
                sotIndex = addr.sotIndex,
                sotDerivationIndex = addr.derivationIndex,
            )
        }

        return database.insertWalletWithAddressesAndTokens(
            walletProfile = WalletProfileEntity(
                name = "",
                iv = iv,
                cipherText = cipherText,
            ),
            addresses = addressList,
            defaultTokenNames = defaultTokenNames,
        )
    }

    override suspend fun registerUserDevice(
        userId: Long,
        deviceToken: String,
    ) {
        val uuid = UUID.randomUUID().toString()

        runCatching {
            userRepository.registerDevice(userId, deviceToken, uuid)
                .fold(
                    onSuccess = {
                        profileLocalRepository.create(
                            UserProfile(
                                userId = userId,
                                appId = uuid,
                                deviceToken = deviceToken,
                                isActive = true
                            ),
                        )
                        Log.i(TAG, "Device successfully registered for userId=$userId")
                    },
                    onFailure = { e ->
                        throw GrpcResponseException("Failed to registerUserDevice", e)
                            .also {
                                Log.e(TAG, it.message, e)
                            }
                    },
                )
        }.onFailure { e ->
            throw GrpcRequestException("Unexpected error while registerUserDevice", e)
                .also {
                    Log.e(TAG, it.message, e)
                }
        }
    }

    private companion object {
        const val TAG = "WalletCreationRepositoryImpl"
    }
}
