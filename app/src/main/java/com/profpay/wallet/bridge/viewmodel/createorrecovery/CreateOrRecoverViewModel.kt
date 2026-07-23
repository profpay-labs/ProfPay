package com.profpay.wallet.bridge.viewmodel.createorrecovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.user.model.AppState
import com.profpay.domain.user.repository.AppStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CreateOrRecoverViewModel @Inject constructor(
    appStateRepository: AppStateRepository,
) : ViewModel() {

    /**
     * Является ли это первым запуском (пользователь не зарегистрирован).
     */
    val isFirstStart: StateFlow<Boolean> = appStateRepository
        .observeAppState()
        .map { it is AppState.NotRegistered }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )
}
