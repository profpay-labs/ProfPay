package com.profpay.domain.contract.model.params

data class CommissionParams(
    val address: String,
    val amount: String,
    val bandwidthRequired: Long = 0,
    val txnBytes: String,
)
