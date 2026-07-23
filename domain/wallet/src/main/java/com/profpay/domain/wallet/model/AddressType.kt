package com.profpay.domain.wallet.model

enum class AddressType {
    USER,
    CENTRAL,
    SOT,
    UNKNOWN;

    companion object {
        fun fromString(value: String): AddressType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}
