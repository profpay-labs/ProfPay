package com.profpay.wallet.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.profpay.core.database.AppDatabase
import com.profpay.core.database.entities.wallet.AddressEntity
import com.profpay.core.database.entities.wallet.WalletProfileEntity
import com.profpay.core.security.CryptoManager
import com.profpay.core.tron.model.AddressesWithKeysForM
import com.profpay.domain.user.model.local.UserProfile
import com.profpay.domain.user.repository.UserRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.wallet.bridge.viewmodel.dto.BlockchainName
import com.profpay.wallet.exceptions.grpc.GrpcRequestException
import com.profpay.wallet.exceptions.grpc.GrpcResponseException
import io.sentry.Sentry
import java.util.UUID
import javax.inject.Inject

interface WalletAddedRepo {
    fun getWalletAlias(addressesWithKeys: AddressesWithKeysForM): String?

    suspend fun insertNewCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM): Long

    /**
     * @deprecated Использовать OnboardUserUseCase вместо этого метода.
     * Оставлен для восстановления кошелька на существующий аккаунт.
     */
    suspend fun registerUserDevice(
        userId: Long,
        deviceToken: String,
        sharedPref: SharedPreferences,
    )
}

class WalletAddedRepoImpl @Inject constructor(
    private val profileLocalRepository: ProfileLocalRepository,
    private val userRepository: UserRepository,
    private val cryptoManager: CryptoManager,
    private val database: AppDatabase,
) : WalletAddedRepo {

    override fun getWalletAlias(addressesWithKeys: AddressesWithKeysForM): String? {
        val mainAddress = addressesWithKeys.addresses
            .firstOrNull { it.indexDerivationSot == 0 }
        return mainAddress?.address
    }

    override suspend fun insertNewCryptoAddresses(addressesWithKeysForM: AddressesWithKeysForM): Long {
        val walletAlias = getWalletAlias(addressesWithKeysForM)
            ?: throw IllegalStateException("Главный адрес кошелька не найден")

        val (iv, cipherText) = cryptoManager.encrypt(walletAlias, addressesWithKeysForM.entropy)

        val addressList = mutableListOf<AddressEntity>()

        val defaultTokenNames = BlockchainName.entries
            .flatMap { blockchain -> blockchain.tokens }
            .map { token -> token.tokenName }

        BlockchainName.entries.forEach { blockchain ->
            addressesWithKeysForM.addresses.forEach { currentAddress ->
                val addressEntity = AddressEntity(
                    walletId = 0, // временно 0, будет заменён в DAO
                    blockchainName = blockchain.blockchainName,
                    address = currentAddress.address,
                    publicKey = currentAddress.publicKey,
                    isGeneralAddress = currentAddress.indexDerivationSot == 0,
                    sotIndex = currentAddress.indexSot,
                    sotDerivationIndex = currentAddress.indexDerivationSot,
                )
                addressList.add(addressEntity)
            }
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
        sharedPref: SharedPreferences,
    ) {
        val uuid = UUID.randomUUID().toString()

        runCatching {
            val registerUserDeviceResult = userRepository.registerDevice(userId, deviceToken, uuid)

            registerUserDeviceResult.fold(
                onSuccess = { _ ->
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
                            Sentry.captureException(it)
                        }
                },
            )
        }.onFailure { e ->
            throw GrpcRequestException("Unexpected error while registerUserDevice", e)
                .also {
                    Log.e(TAG, it.message, e)
                    Sentry.captureException(it)
                }
        }
    }

    private companion object {
        const val TAG = "WalletAddedRepoImpl"
    }
}
