package com.profpay.domain.contract.model.params

data class ContractParams(
    val address: String,
    val contractName: String,
    val amount: Long = 0,
    val estimateEnergy: Long,
    val bandwidthRequired: Long,
    val txnBytes: String,
)
