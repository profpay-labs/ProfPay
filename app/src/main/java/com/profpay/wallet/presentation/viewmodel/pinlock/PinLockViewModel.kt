package com.profpay.wallet.presentation.viewmodel.pinlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.security.repository.PinManager
import com.profpay.core.security.lock.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinLockViewModel @Inject constructor(
    private val pinManager: PinManager,
    private val appLockManager: AppLockManager,
) : ViewModel() {

    // ══════════════════════════════════════════════════════════════════════
    // State
    // ══════════════════════════════════════════════════════════════════════

    private val _uiState = MutableStateFlow<PinUiState>(PinUiState.Idle)
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<LockState>(replay = 1)
    val navigationEvents = _navigationEvents.asSharedFlow()

    // ══════════════════════════════════════════════════════════════════════
    // Init
    // ══════════════════════════════════════════════════════════════════════

    init {
        appLockManager.lock()
    }

    // ══════════════════════════════════════════════════════════════════════
    // Public API
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Проверяет состояние PIN и отправляет navigation event.
     * Вызывается при foreground и при инициализации.
     */
    fun checkPinState() {
        viewModelScope.launch {
            val state = determineLockState()
            _navigationEvents.emit(state)
        }
    }

    /**
     * Разблокировка через биометрию.
     * Вызывается после успешной биометрической аутентификации.
     */
    fun unlockWithBiometric() {
        viewModelScope.launch {
            _uiState.value = PinUiState.Success
            unlockSession()
        }
    }

    /**
     * Сохраняет новый PIN-код.
     */
    fun saveNewPin(pin: String) {
        viewModelScope.launch {
            _uiState.value = PinUiState.Loading

            try {
                pinManager.savePin(pin)
                _uiState.value = PinUiState.Success
                unlockSession()
            } catch (e: Exception) {
                Sentry.captureException(e)
                _uiState.value = PinUiState.Error(
                    message = "Не удалось сохранить PIN-код. Попробуйте ещё раз.",
                )
            }
        }
    }

    /**
     * Валидирует введённый PIN-код.
     */
    fun validatePin(pin: String) {
        viewModelScope.launch {
            _uiState.value = PinUiState.Loading

            try {
                val isValid = pinManager.validatePin(pin)

                if (isValid) {
                    _uiState.value = PinUiState.Success
                    unlockSession()
                } else {
                    _uiState.value = PinUiState.ValidationFailed
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                _uiState.value = PinUiState.Error(
                    message = "Ошибка проверки PIN-кода.",
                )
            }
        }
    }

    /**
     * Сбрасывает UI state (например, после показа ошибки).
     */
    fun resetState() {
        _uiState.value = PinUiState.Idle
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun determineLockState(): LockState {
        return when {
            !appLockManager.isLocked.value -> LockState.None
            !pinManager.hasPin() -> LockState.RequireCreation
            else -> LockState.RequireUnlock
        }
    }

    private suspend fun unlockSession() {
        appLockManager.unlock()
        _navigationEvents.emit(LockState.None)
    }
}

// ══════════════════════════════════════════════════════════════════════
// UI State
// ══════════════════════════════════════════════════════════════════════

sealed interface PinUiState {
    data object Idle : PinUiState
    data object Loading : PinUiState
    data object Success : PinUiState
    data object ValidationFailed : PinUiState
    data class Error(val message: String) : PinUiState
}

// ══════════════════════════════════════════════════════════════════════
// Lock State (Navigation)
// ══════════════════════════════════════════════════════════════════════

enum class LockState {
    RequireUnlock,
    RequireCreation,
    None,
}
