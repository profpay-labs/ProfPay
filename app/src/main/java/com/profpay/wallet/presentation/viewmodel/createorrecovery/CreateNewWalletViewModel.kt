package com.profpay.wallet.presentation.viewmodel.createorrecovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.wallet.model.GeneratedWalletData
import com.profpay.domain.wallet.repository.WalletGenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNewWalletViewModel @Inject constructor(
    private val walletGenerationRepository: WalletGenerationRepository,
) : ViewModel() {

    private val _state: MutableStateFlow<CreateNewWalletState> =
        MutableStateFlow(CreateNewWalletState.Loading)

    val state: StateFlow<CreateNewWalletState> = _state.asStateFlow()

    init {
        createNewWallet()
        observeGeneratedWallet()
    }

    private fun createNewWallet() {
        viewModelScope.launch {
            walletGenerationRepository.generateNewWallet()
        }
    }

    private fun observeGeneratedWallet() {
        viewModelScope.launch {
            walletGenerationRepository.generatedWallet.collect { walletData ->
                _state.value = CreateNewWalletState.Success(walletData)
            }
        }
    }
}

sealed interface CreateNewWalletState {
    data object Loading : CreateNewWalletState

    data class Success(
        val walletData: GeneratedWalletData,
    ) : CreateNewWalletState
}
