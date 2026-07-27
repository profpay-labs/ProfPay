package com.profpay.domain.config.model

/**
 * Тема приложения.
 */
enum class AppTheme(val value: Int) {
    LIGHT(0),
    DARK(1),
    SYSTEM(2);

    companion object {
        fun fromValue(value: Int): AppTheme =
            entries.find { it.value == value } ?: SYSTEM
    }
}
