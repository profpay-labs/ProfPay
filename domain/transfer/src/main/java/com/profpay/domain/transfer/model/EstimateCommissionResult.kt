package com.profpay.domain.transfer.model

/**
 * Результат оценки комиссии
 */
data class EstimateCommissionResult(
    val commission: String,
    val categories: List<CommissionBreakdown>,
    val timestampSeconds: Long,
) {
    /**
     * Общая комиссия в SUN как Long
     */
    val commissionSun: Long
        get() = commission.toLongOrNull() ?: 0L

    companion object {
        val EMPTY = EstimateCommissionResult(
            commission = "0",
            categories = emptyList(),
            timestampSeconds = 0L
        )
    }
}
