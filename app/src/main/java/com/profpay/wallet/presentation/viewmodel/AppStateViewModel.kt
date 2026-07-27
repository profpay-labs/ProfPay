package com.profpay.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.user.model.AppState
import com.profpay.domain.user.repository.AppStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для определения состояния приложения.
 * Используется для навигации на стартовый экран.
 */
@HiltViewModel
class AppStateViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
) : ViewModel() {

    private val _appState = MutableStateFlow<AppStateUiState>(AppStateUiState.Loading)
    val appState: StateFlow<AppStateUiState> = _appState.asStateFlow()

    init {
        loadAppState()
    }

    private fun loadAppState() {
        viewModelScope.launch {
            val state = appStateRepository.getAppState()
            _appState.value = AppStateUiState.Loaded(state)
        }
    }

    /**
     * Перезагрузить состояние (например, после онбординга).
     */
    fun refresh() {
        loadAppState()
    }
}

sealed interface AppStateUiState {
    data object Loading : AppStateUiState
    data class Loaded(val appState: AppState) : AppStateUiState
}
