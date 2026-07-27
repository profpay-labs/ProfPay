package com.profpay.wallet.presentation.viewmodel.createorrecovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.wallet.model.GeneratedWalletData
import com.profpay.domain.wallet.repository.WalletGenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SeedPhraseConfirmationViewModel @Inject constructor(
    walletGenerationRepository: WalletGenerationRepository,
) : ViewModel() {

    val state: StateFlow<SeedPhraseConfirmationState> =
        walletGenerationRepository.generatedWallet
            .map { walletData ->
                SeedPhraseConfirmationState.Success(walletData)
            }
            .stateIn(
                scope = viewModelScope,
                initialValue = SeedPhraseConfirmationState.Loading,
                started = SharingStarted.WhileSubscribed(5_000),
            )
}

sealed interface SeedPhraseConfirmationState {
    data object Loading : SeedPhraseConfirmationState

    data class Success(
        val walletData: GeneratedWalletData,
    ) : SeedPhraseConfirmationState
}
