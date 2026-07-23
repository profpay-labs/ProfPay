package com.profpay.wallet.bridge.viewmodel.smartcontract

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Состояние модального окна прогресса операций смарт-контракта.
 */
data class SmartContractModalState(
    val isVisible: Boolean = false,
    val message: String = "",
) {
    companion object {
        val Hidden = SmartContractModalState()
    }
}

/**
 * Держатель UI-состояния модалки смарт-контракта.
 * Singleton, т.к. состояние шарится между use case (запись) и экраном (чтение).
 */
@Singleton
class SmartContractModalStateHolder @Inject constructor() {

    private val _state = MutableStateFlow(SmartContractModalState.Hidden)
    val state: StateFlow<SmartContractModalState> = _state.asStateFlow()

    fun show(message: String) {
        _state.value = SmartContractModalState(isVisible = true, message = message)
    }

    fun hide() {
        _state.value = SmartContractModalState.Hidden
    }
}
