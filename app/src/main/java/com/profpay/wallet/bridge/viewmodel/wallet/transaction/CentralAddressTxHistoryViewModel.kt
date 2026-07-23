package com.profpay.wallet.bridge.viewmodel.wallet.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.di.IoDispatcher
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.local.CentralAddressLocal
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CentralAddressTxHistoryViewModel
    @Inject
    constructor(
        private val transactionLocalRepository: TransactionLocalRepository,
        private val centralAddressLocalRepository: CentralAddressLocalRepository,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _groupedAllTransaction = MutableStateFlow<List<List<TransactionSummary>>>(emptyList())
        val groupedAllTransaction: StateFlow<List<List<TransactionSummary>>> = _groupedAllTransaction

        fun getTransactionsByAddressAndTokenLD(
            walletId: Long,
            address: String,
            tokenName: String,
            isSender: Boolean,
            isCentralAddress: Boolean,
        ): LiveData<List<TransactionSummary>> =
            liveData(ioDispatcher) {
                emitSource(
                    transactionLocalRepository
                        .observeByAddressAndToken(
                            walletId = walletId,
                            address = address,
                            tokenName = tokenName,
                            isSender = isSender,
                            isCentralAddress = isCentralAddress,
                        ).asLiveData(),
                )
            }

        fun groupTransactions(list: List<TransactionSummary>) =
            viewModelScope.launch(ioDispatcher) {
                val groupedList =
                    if (list.isEmpty()) {
                        emptyList()
                    } else {
                        list
                            .sortedByDescending { it.timestamp }
                            .groupBy { it.transactionDate }
                            .values
                            .toList()
                    }

                _groupedAllTransaction.value = groupedList
            }

        fun getCentralAddressLiveData(): LiveData<CentralAddressLocal?> =
            liveData(ioDispatcher) {
                emitSource(centralAddressLocalRepository.observe().asLiveData())
            }
    }
