package com.profpay.wallet.presentation.viewmodel.dto.transfer

import com.profpay.domain.transfer.model.EstimateCommissionResult

/**
 * UI-состояние запроса комиссии.
 * Используется во всех ViewModel, работающих с оценкой комиссии транзакций.
 */
sealed class CommissionUiState {
    /** Начальное состояние, запрос ещё не выполнялся */
    data object Idle : CommissionUiState()

    /** Запрос в процессе */
    data object Loading : CommissionUiState()

    /** Успешный результат */
    data class Success(val result: EstimateCommissionResult) : CommissionUiState()

    /** Ошибка с сообщением для UI */
    data class Error(val message: String) : CommissionUiState()

    /** Геттер для извлечения результата */
    val resultOrNull: EstimateCommissionResult?
        get() = (this as? Success)?.result
}
