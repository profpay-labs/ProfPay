package com.profpay.wallet.presentation.viewmodel.wallet.walletSot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.security.CryptoManager
import com.profpay.core.tron.Tron
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.ActiveWalletManager
import com.profpay.domain.wallet.model.GeneralAddressUpdateParams
import com.profpay.domain.wallet.model.SotAddressParams
import com.profpay.domain.wallet.model.UpdateDerivedIndexParams
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.model.local.CentralAddressLocal
import com.profpay.domain.wallet.model.local.TokenLocal
import com.profpay.domain.wallet.model.local.WalletAddressLocal
import com.profpay.domain.wallet.repository.WalletRepository
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.repository.local.TokenLocalRepository
import com.profpay.domain.wallet.repository.local.WalletProfileLocalRepository
import com.profpay.wallet.presentation.viewmodel.dto.BlockchainName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletSotViewModel @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val walletProfileLocalRepository: WalletProfileLocalRepository,
    private val tokenLocalRepository: TokenLocalRepository,
    private val profileLocalRepository: ProfileLocalRepository,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val tron: Tron,
    private val cryptoManager: CryptoManager,
    private val activeWalletManager: ActiveWalletManager,
    private val walletRepository: WalletRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    // Получение списка адресов и балансов в формате Flow
    fun getAddressesSotsWithTokensByBlockchain(
        blockchainName: String,
    ): LiveData<List<AddressWithTokensLocal>> {
        val walletId = activeWalletManager.activeWalletId
        return liveData(ioDispatcher) {
            emitSource(addressLocalRepository.observeSotsWithTokensByBlockchain(walletId, blockchainName).asLiveData())
        }
    }

    fun creationOfANewCell(addressEntity: WalletAddressLocal) = viewModelScope.launch(ioDispatcher) {
        val walletId = activeWalletManager.activeWalletId
        val generalAddress = addressLocalRepository.getGeneralAddressByWalletId(walletId)
        val cipherData = walletProfileLocalRepository.getCipherData(walletId)

        val entropy =
            cryptoManager.decrypt(
                alias = generalAddress,
                iv = cipherData.iv,
                cipherText = cipherData.cipherText,
            )

        val newSotDerivationIndex = addressLocalRepository.getMaxSotDerivationIndex(walletId) + 1
        val userAppId = profileLocalRepository.getAppId()

        val result =
            tron.addressUtilities.deriveKeyAtIndex(
                entropy = entropy,
                index = newSotDerivationIndex.toLong(),
            )

        val address = tron.addressUtilities.publicKeyToAddress(result.publicKeyBytes)
            ?: throw Exception("The public address has not been created!")

        try {
            val params = UpdateDerivedIndexParams(
                appId = userAppId,
                oldSotAddress = addressEntity.address,
                newSotAddress = SotAddressParams(
                    address = address,
                    pubKey = result.publicKeyHex,
                    index = addressEntity.sotIndex,
                    derivationIndex = newSotDerivationIndex,
                ),
                generalAddress = GeneralAddressUpdateParams(
                    address = generalAddress,
                    oldSotDerivationIndex = addressEntity.sotDerivationIndex,
                    newSotDerivationIndex = newSotDerivationIndex,
                )
            )

            walletRepository.updateDerivedIndex(params).fold(
                onSuccess = { /* успешно */ },
                onFailure = { error ->
                    Log.e("WalletSotViewModel", "Failed to update derived index", error)
                    return@launch
                }
            )
        } catch (e: Exception) {
            Log.e("ERROR", e.message!!)
            return@launch
        }

        addressLocalRepository.updateSotIndex(
            index = -1,
            addressId = addressEntity.id,
        )

        BlockchainName.entries.forEach { blockchain ->
            val addressId =
                addressLocalRepository.insert(
                    WalletAddressLocal(
                        walletId = walletId,
                        blockchainName = blockchain.blockchainName,
                        address = address,
                        publicKey = result.publicKeyHex,
                        isGeneralAddress = false,
                        sotIndex = addressEntity.sotIndex,
                        sotDerivationIndex = newSotDerivationIndex,
                    ),
                )
            blockchain.tokens.forEach { token ->
                tokenLocalRepository.insert(
                    TokenLocal(
                        addressId = addressId,
                        tokenName = token.tokenName,
                        balance = BigInteger.ZERO,
                    ),
                )
            }
        }

        val isCentralAddressExists = centralAddressLocalRepository.exists()
        if (!isCentralAddressExists) {
            val address = tron.addressUtilities.generateSingleAddress()
            centralAddressLocalRepository.insert(
                CentralAddressLocal(
                    address = address.address,
                    publicKey = address.publicKey,
                    privateKey = address.privateKey,
                ),
            )
        }

        val centralAddress = centralAddressLocalRepository.get()
        if (centralAddress != null) {
            val balance = tron.addressUtilities.getTrxBalance(centralAddress.address)
            if (balance >= BigInteger.valueOf(1_500_000)) {
                val newBalance = tron.addressUtilities.getTrxBalance(centralAddress.address)
                if (newBalance < BigInteger.valueOf(1_000_000)) return@launch
                if (!tron.addressUtilities.isAddressActivated(address)) {
                    tron.transactions.trxTransfer(
                        fromAddress = centralAddress.address,
                        toAddress = address,
                        privateKey = centralAddress.privateKey,
                        amount = 1_000,
                    )
                }
            }
        }
    }
}
