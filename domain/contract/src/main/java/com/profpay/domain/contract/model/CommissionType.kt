package com.profpay.domain.contract.model

enum class CommissionType {
    USER_GROUP_FEE,
    ENERGY_PRICE,
    BANDWIDTH_PRICE;

    companion object {
        fun fromString(value: String): CommissionType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: USER_GROUP_FEE
    }
}
