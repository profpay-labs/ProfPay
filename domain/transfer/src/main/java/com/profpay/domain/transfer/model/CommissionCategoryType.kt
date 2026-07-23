package com.profpay.domain.transfer.model

enum class CommissionCategoryType {
    ENERGY_PRICE,
    BANDWIDTH_PRICE,
    USER_GROUP_FEE;

    companion object {
        fun fromString(value: String): CommissionCategoryType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ENERGY_PRICE
    }
}
