package com.profpay.domain.aml.model

enum class PaymentStatus {
    SUCCESS,
    PENDING,
    FAILED;

    companion object {
        fun fromString(value: String): PaymentStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: FAILED
    }
}
