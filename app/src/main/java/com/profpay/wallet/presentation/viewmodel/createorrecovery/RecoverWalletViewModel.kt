package com.profpay.wallet.presentation.viewmodel.createorrecovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.wallet.model.RecoveryResult
import com.profpay.domain.wallet.repository.WalletGenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoverWalletViewModel @Inject constructor(
    private val walletGenerationRepository: WalletGenerationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<RecoverWalletState>(RecoverWalletState.Loading)
    val state: StateFlow<RecoverWalletState> = _state.asStateFlow()

    private val _uiEvent = MutableStateFlow<RecoverUiEvent?>(null)
    val uiEvent: StateFlow<RecoverUiEvent?> = _uiEvent.asStateFlow()

    init {
        observeRecoveryResult()
    }

    private fun observeRecoveryResult() {
        viewModelScope.launch {
            walletGenerationRepository.recoveryResult.collect { result ->
                _state.value = RecoverWalletState.Success(result)
            }
        }
    }

    fun recoverWallet(mnemonic: String) {
        viewModelScope.launch {
            try {
                walletGenerationRepository.recoverWalletFromMnemonic(mnemonic)
                _uiEvent.emit(RecoverUiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.emit(RecoverUiEvent.Error(e.message ?: "Ошибка восстановления"))
            }
        }
    }

    fun clearRecoveryResult() {
        viewModelScope.launch {
            walletGenerationRepository.clearRecoveryResult()
        }
    }

    fun consumeUiEvent() {
        _uiEvent.value = null
    }
}

sealed interface RecoverWalletState {
    data object Loading : RecoverWalletState

    data class Success(
        val recoveryResult: RecoveryResult,
    ) : RecoverWalletState
}

sealed class RecoverUiEvent {
    data object Success : RecoverUiEvent()

    data class Error(val message: String) : RecoverUiEvent()
}
