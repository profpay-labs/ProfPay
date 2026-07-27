package com.profpay.domain.config.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Репозиторий для управления темой приложения.
 */
interface ThemeRepository {

    /**
     * Текущее значение темы как StateFlow.
     * - 0 = светлая
     * - 1 = тёмная
     * - 2 = системная
     */
    val themeValue: StateFlow<Int>

    /**
     * Установить тему.
     * @param value 0 = светлая, 1 = тёмная, 2 = системная
     */
    fun setTheme(value: Int)
}
