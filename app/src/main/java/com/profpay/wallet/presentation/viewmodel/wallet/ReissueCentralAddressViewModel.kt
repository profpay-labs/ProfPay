package com.profpay.wallet.presentation.viewmodel.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.wallet.repository.ReissueCentralAddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReissueCentralAddressViewModel @Inject constructor(
    private val repository: ReissueCentralAddressRepository,
) : ViewModel() {

    fun reissueCentralAddress() = viewModelScope.launch {
        repository.changeCentralAddress()
    }
}
