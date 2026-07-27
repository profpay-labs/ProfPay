package com.profpay.wallet.presentation.viewmodel.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.domain.wallet.model.local.CentralAddressLocal
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletSystemTRXScreenViewModel
    @Inject
    constructor(
        val centralAddressLocalRepository: CentralAddressLocalRepository,
        private val tron: Tron,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        init {
            viewModelScope.launch(ioDispatcher) {
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
            }
        }

        fun getCentralAddressLiveData(): LiveData<CentralAddressLocal?> =
            liveData(ioDispatcher) {
                emitSource(centralAddressLocalRepository.observe().asLiveData())
            }
    }
