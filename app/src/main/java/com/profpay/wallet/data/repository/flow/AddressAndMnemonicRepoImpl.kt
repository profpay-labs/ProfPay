package com.profpay.wallet.data.repository.flow

import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.core.tron.model.AddressGenerateFromSeedPhr
import com.profpay.core.tron.model.AddressGenerateResult
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.WalletRepository
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AddressAndMnemonicRepo {
    suspend fun generateNewAddressAndMnemonic()

    val addressAndMnemonic: Flow<AddressGenerateResult>
    val addressFromMnemonic: Flow<RecoveryResult>

    suspend fun generateAddressFromMnemonic(mnemonic: String)

    suspend fun recoveryWallet(
        address: String,
        mnemonic: String,
    )

    suspend fun clearAddressFromMnemonic()
}

class AddressAndMnemonicRepoImpl
    @Inject
    constructor(
        val profileLocalRepository: ProfileLocalRepository,
        val addressLocalRepository: AddressLocalRepository,
        private val tron: Tron,
        private val walletRepository: WalletRepository,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : AddressAndMnemonicRepo {
        private val _addressAndMnemonic = MutableSharedFlow<AddressGenerateResult>(replay = 1)

        // Получение данных нового кошелька
        override val addressAndMnemonic: Flow<AddressGenerateResult> =
            _addressAndMnemonic.asSharedFlow()

        // Триггер на обновление данных нового кошелька
        override suspend fun generateNewAddressAndMnemonic() {
            withContext(ioDispatcher) {
                val addressAndMnemonic = tron.addressUtilities.generateAddressAndMnemonic()
                _addressAndMnemonic.emit(addressAndMnemonic)
            }
        }

        private val _addressFromMnemonic = MutableSharedFlow<RecoveryResult>(replay = 1)

        // Получение данных восстановленного кошелька по мнемонике(сид-фразе)
        override val addressFromMnemonic: Flow<RecoveryResult> =
            _addressFromMnemonic.asSharedFlow()

        override suspend fun clearAddressFromMnemonic() {
            _addressFromMnemonic.emit(RecoveryResult.Empty)
        }

        // Триггер на обновление данных восстановленного кошелька по мнемонике(сид-фразе)
        override suspend fun generateAddressFromMnemonic(mnemonic: String) {
            withContext(ioDispatcher) {
                try {
                    val generalAddress = tron.addressUtilities.getGeneralAddressBySeedPhrase(mnemonic)

                    val byAddressOrNull = addressLocalRepository.getByAddress(generalAddress)
                    if (byAddressOrNull == null) {
                        recoveryWallet(generalAddress, mnemonic)
                    } else {
                        _addressFromMnemonic.emit(RecoveryResult.RepeatingMnemonic)
                    }
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    _addressFromMnemonic.emit(RecoveryResult.InvalidMnemonic)
                }
            }
        }

        override suspend fun recoveryWallet(
            address: String,
            mnemonic: String,
        ) {
            try {
                val result = walletRepository.getWalletData(address = address)

                result.fold(
                    onSuccess = { walletData ->
                        val recoveryResult =
                            try {
                                val addressGenerateFromSeedPhr =
                                    tron.addressUtilities.recoveryKeysAndAddressBySeedPhrase(
                                        mnemonic,
                                        walletData.derivedIndices,
                                    )
                                RecoveryResult.Success(
                                    address = addressGenerateFromSeedPhr,
                                    accountWasFound = true,
                                    userId = walletData.userId,
                                )
                            } catch (_: Exception) {
                                RecoveryResult.InvalidMnemonic
                            }

                        _addressFromMnemonic.emit(recoveryResult)
                    },
                    onFailure = { error ->
                        if (error.message == "INTERNAL: Address not found in database") {
                            val address =
                                try {
                                    tron.addressUtilities.generateNextAddressGroup(mnemonic)
                                } catch (_: Exception) {
                                    _addressFromMnemonic.emit(RecoveryResult.InvalidMnemonic)
                                    return
                                }
                            _addressFromMnemonic.emit(RecoveryResult.Success(address = address, accountWasFound = false))
                        } else {
                            Sentry.captureException(error)
                            _addressFromMnemonic.emit(RecoveryResult.Error(RuntimeException(error)))
                        }
                    },
                )
            } catch (e: Exception) {
                Sentry.captureException(e)
                throw RuntimeException("Failed to fetch smart contracts", e)
            }
        }
    }

sealed class RecoveryResult {
    data class Success(
        val address: AddressGenerateFromSeedPhr,
        val accountWasFound: Boolean,
        val userId: Long? = null,
    ) : RecoveryResult()

    data object InvalidMnemonic : RecoveryResult()

    data object RepeatingMnemonic : RecoveryResult()

    data object AddressNotFound : RecoveryResult()

    data class Error(
        val throwable: Throwable,
    ) : RecoveryResult()

    object Empty : RecoveryResult()
}
