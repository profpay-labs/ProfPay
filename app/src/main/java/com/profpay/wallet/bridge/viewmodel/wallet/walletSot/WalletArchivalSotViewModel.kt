package com.profpay.wallet.bridge.viewmodel.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.profpay.core.common.di.IoDispatcher
import com.profpay.domain.wallet.ActiveWalletManager
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletArchivalSotViewModel
    @Inject
    constructor(
        private val addressLocalRepository: AddressLocalRepository,
        private val activeWalletManager: ActiveWalletManager,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        fun getAddressWithTokensArchivalByBlockchain(
            blockchainName: String,
        ): LiveData<List<AddressWithTokensLocal>> {
            return liveData(ioDispatcher) {
                val walletId = activeWalletManager.activeWalletId
                emitSource(addressLocalRepository.observeArchivalAddressesWithTokens(walletId, blockchainName).asLiveData())
            }
        }

        fun getAddressesWTAWithFunds(
            listAddressWithTokens: List<AddressWithTokensLocal>,
            tokenName: String,
        ): List<AddressWithTokensLocal> =
            listAddressWithTokens.filter { addressWT ->
                addressWT.tokens.any { token ->
                    token.tokenName == tokenName && token.availableBalance > BigInteger.ZERO
                }
            }
    }
