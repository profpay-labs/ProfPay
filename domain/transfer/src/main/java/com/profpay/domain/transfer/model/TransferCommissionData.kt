package com.profpay.domain.transfer.model

data class TransferCommissionData(
    val address: String,
    val amount: String,
    val bandwidthRequired: Long,
    val categories: List<CommissionBreakdown>,
    val txnBytes: String,
)
