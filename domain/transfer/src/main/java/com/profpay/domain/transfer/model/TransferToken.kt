package com.profpay.domain.transfer.model

enum class TransferToken {
    TRX,
    USDT_TRC20;

    companion object {
        fun fromString(value: String): TransferToken =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: USDT_TRC20
    }
}
