package com.profpay.domain.transfer.model

data class CommissionBreakdown(
    val type: CommissionCategoryType,
    val amount: String,
    val description: String? = null,
) {
    val amountSun: Long
        get() = amount.toLongOrNull() ?: 0L
}
