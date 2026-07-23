package com.profpay.domain.transfer.model

data class TransactionData(
    val address: String,
    val receiverAddress: String,
    val amount: String,
    val bandwidthRequired: Long,
    val estimateEnergy: Long,
    val txnBytes: String,
)
