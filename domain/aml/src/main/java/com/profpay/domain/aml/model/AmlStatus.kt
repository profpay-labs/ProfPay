package com.profpay.domain.aml.model

/**
 * Статус AML проверки
 */
enum class AmlStatus {
    SUCCESS,
    PENDING,
    ERROR;

    companion object {
        fun fromString(value: String): AmlStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ERROR
    }
}
