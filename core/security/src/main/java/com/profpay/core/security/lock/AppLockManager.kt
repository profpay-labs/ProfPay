package com.profpay.core.security.lock

import com.profpay.domain.security.repository.PinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Менеджер блокировки приложения.
 *
 * Отвечает за in-memory состояние: заблокировано приложение или нет.
 * При уходе в background — блокируем, при успешном вводе PIN — разблокируем.
 */
@Singleton
class AppLockManager @Inject constructor(
    private val pinManager: PinManager,
) {
    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    /**
     * Блокирует приложение (при уходе в background).
     */
    fun lock() {
        _isLocked.value = true
    }

    /**
     * Разблокирует приложение (после успешного ввода PIN).
     */
    fun unlock() {
        _isLocked.value = false
    }

    /**
     * Проверяет, требуется ли блокировка.
     * Если PIN не установлен — блокировка не нужна.
     */
    suspend fun shouldLock(): Boolean = pinManager.hasPin()

    /**
     * Проверяет, требуется ли разблокировка прямо сейчас.
     */
    suspend fun requiresUnlock(): Boolean = pinManager.hasPin() && _isLocked.value
}
