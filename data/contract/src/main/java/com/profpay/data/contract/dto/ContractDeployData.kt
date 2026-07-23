package com.profpay.data.contract.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContractDeployData(
    @SerialName("address")
    val address: String,
    @SerialName("contractName")
    val contractName: String,
    @SerialName("amount")
    val amount: Long = 0,
    @SerialName("estimateEnergy")
    val estimateEnergy: Long,
    @SerialName("bandwidthRequired")
    val bandwidthRequired: Long,
    @SerialName("txnBytes")
    val txnBytes: String,
)
