package com.profpay.wallet.presentation.viewmodel.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.security.CryptoManager
import com.profpay.core.tron.Tron
import com.profpay.domain.wallet.ActiveWalletManager
import com.profpay.domain.wallet.model.local.WalletProfileSummary
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.WalletProfileLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletSystemViewModel @Inject constructor(
    private val walletProfileLocalRepository: WalletProfileLocalRepository,
    private val addressLocalRepository: AddressLocalRepository,
    private val activeWalletManager: ActiveWalletManager,
    private val tron: Tron,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val cryptoManager: CryptoManager,
) : ViewModel() {
    val walletList: StateFlow<List<WalletProfileSummary>> =
        walletProfileLocalRepository.observeAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val currentWalletId: StateFlow<Long> = activeWalletManager.activeWalletIdFlow

    fun selectWallet(walletId: Long) {
        viewModelScope.launch {
            activeWalletManager.setActiveWallet(walletId)
        }
    }

    fun getListAllWallets(): LiveData<List<WalletProfileSummary>> =
        liveData(ioDispatcher) {
            emitSource(walletProfileLocalRepository.observeAll().asLiveData())
        }

    fun updateNameWalletById(
        id: Long,
        newName: String,
    ) = viewModelScope.launch(ioDispatcher) {
        walletProfileLocalRepository.updateName(id, newName)
    }

    suspend fun getSeedPhrase(walletId: Long): String {
        val generalAddress = addressLocalRepository.getGeneralAddressByWalletId(walletId)
        val cipherData = walletProfileLocalRepository.getCipherData(walletId)

        val entropy =
            cryptoManager.decrypt(
                alias = generalAddress,
                iv = cipherData.iv,
                cipherText = cipherData.cipherText,
            )

        return tron.addressUtilities.getSeedPhraseByEntropy(entropy)
    }

    fun deleteWalletProfile(walletId: Long) =
        viewModelScope.launch(ioDispatcher) {
            walletProfileLocalRepository.delete(walletId)
        }
}
