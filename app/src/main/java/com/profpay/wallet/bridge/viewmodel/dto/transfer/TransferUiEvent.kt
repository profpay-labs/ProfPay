package com.profpay.wallet.bridge.viewmodel.dto.transfer

/**
 * Унифицированное UI-событие для операций перевода.
 * Используется во всех ViewModel, связанных с отправкой транзакций.
 *
 * События (в отличие от State) — одноразовые: показали snackbar/dialog и сбросили в Idle.
 */
sealed class TransferUiEvent {
    /** Начальное/сброшенное состояние */
    data object Idle : TransferUiEvent()

    /** Транзакция в процессе отправки */
    data object Loading : TransferUiEvent()

    /** Транзакция успешно отправлена */
    data object Success : TransferUiEvent()

    /** Ошибка при отправке */
    data class Error(
        val title: String,
        val message: String,
    ) : TransferUiEvent()

    /** Проверка: является ли событие терминальным (требует обработки UI) */
    val isTerminal: Boolean
        get() = this is Success || this is Error
}
