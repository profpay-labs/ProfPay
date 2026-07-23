package com.profpay.wallet.bridge.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.user.repository.local.SettingsLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data object Success : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

// Для одноразовых событий (Snackbar, Toast)
sealed interface SettingsEvent {
    data class ShowError(val message: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsLocalRepository: SettingsLocalRepository,
    private val profileLocalRepository: ProfileLocalRepository,
) : ViewModel() {

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _appId = MutableStateFlow("")
    val appId: StateFlow<String> = _appId.asStateFlow()

    init {
        loadAppId()
        loadTelegramData()
    }

    private fun loadAppId() = viewModelScope.launch(Dispatchers.IO) {
        _appId.value = profileLocalRepository.getAppId()
    }

    fun loadTelegramData() {
        viewModelScope.launch {
            settingsLocalRepository.get()
        }
    }
}
