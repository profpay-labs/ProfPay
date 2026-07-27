package com.profpay.data.wallet.repository

import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.data.wallet.mapper.WalletGenerationMapper
import com.profpay.domain.wallet.model.GeneratedWalletData
import com.profpay.domain.wallet.model.RecoveryResult
import com.profpay.domain.wallet.repository.WalletGenerationRepository
import com.profpay.domain.wallet.repository.WalletRepository
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WalletGenerationRepositoryImpl @Inject constructor(
    private val tron: Tron,
    private val walletRepository: WalletRepository,
    private val addressLocalRepository: AddressLocalRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WalletGenerationRepository {

    private val _generatedWallet = MutableSharedFlow<GeneratedWalletData>(replay = 1)
    override val generatedWallet: Flow<GeneratedWalletData> = _generatedWallet.asSharedFlow()

    private val _recoveryResult = MutableSharedFlow<RecoveryResult>(replay = 1)
    override val recoveryResult: Flow<RecoveryResult> = _recoveryResult.asSharedFlow()

    override suspend fun generateNewWallet() {
        withContext(ioDispatcher) {
            val result = tron.addressUtilities.generateAddressAndMnemonic()
            val domainModel = WalletGenerationMapper.toGeneratedWalletData(result)
            _generatedWallet.emit(domainModel)
        }
    }

    override suspend fun recoverWalletFromMnemonic(mnemonic: String) {
        withContext(ioDispatcher) {
            try {
                val generalAddress = tron.addressUtilities.getGeneralAddressBySeedPhrase(mnemonic)
                val existingAddress = addressLocalRepository.getByAddress(generalAddress)

                if (existingAddress != null) {
                    _recoveryResult.emit(RecoveryResult.RepeatingMnemonic)
                    return@withContext
                }

                performWalletRecovery(generalAddress, mnemonic)
            } catch (e: Exception) {
                _recoveryResult.emit(RecoveryResult.InvalidMnemonic)
            }
        }
    }

    private suspend fun performWalletRecovery(address: String, mnemonic: String) {
        try {
            val result = walletRepository.getWalletData(address = address)

            result.fold(
                onSuccess = { walletData ->
                    val recoveryResult = try {
                        val addressFromSeedPhrase = tron.addressUtilities.recoveryKeysAndAddressBySeedPhrase(
                            mnemonic,
                            walletData.derivedIndices,
                        )
                        val recoveredData = WalletGenerationMapper.toRecoveredAddressData(addressFromSeedPhrase)
                        RecoveryResult.Success(
                            addressData = recoveredData,
                            accountWasFound = true,
                            userId = walletData.userId,
                        )
                    } catch (_: Exception) {
                        RecoveryResult.InvalidMnemonic
                    }
                    _recoveryResult.emit(recoveryResult)
                },
                onFailure = { error ->
                    handleRecoveryFailure(error, mnemonic)
                },
            )
        } catch (e: Exception) {
            _recoveryResult.emit(RecoveryResult.Error(RuntimeException("Failed to fetch wallet data", e)))
        }
    }

    private suspend fun handleRecoveryFailure(error: Throwable, mnemonic: String) {
        if (error.message == "INTERNAL: Address not found in database") {
            try {
                // generateNextAddressGroup возвращает AddressesWithKeysForM
                val generatedAddress = tron.addressUtilities.generateNextAddressGroup(mnemonic)
                val recoveredData = WalletGenerationMapper.toRecoveredAddressData(generatedAddress)
                _recoveryResult.emit(
                    RecoveryResult.Success(
                        addressData = recoveredData,
                        accountWasFound = false,
                    )
                )
            } catch (_: Exception) {
                _recoveryResult.emit(RecoveryResult.InvalidMnemonic)
            }
        } else {
            _recoveryResult.emit(RecoveryResult.Error(RuntimeException(error)))
        }
    }

    override suspend fun clearRecoveryResult() {
        _recoveryResult.emit(RecoveryResult.Empty)
    }
}
