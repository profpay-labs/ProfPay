package com.profpay.domain.aml.model

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun fromString(value: String): RiskLevel =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}
