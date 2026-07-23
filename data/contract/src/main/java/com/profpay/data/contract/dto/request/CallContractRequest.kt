package com.profpay.data.contract.dto.request

import com.profpay.data.contract.dto.CommissionData
import com.profpay.data.contract.dto.ContractDeployData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request для вызова метода контракта
 */
@Serializable
data class CallContractRequest(
    @SerialName("userId")
    val userId: Long,
    @SerialName("appId")
    val appId: String,
    @SerialName("ownerAddress")
    val ownerAddress: String,
    @SerialName("changeStatus")
    val changeStatus: String,
    @SerialName("contract")
    val contract: ContractDeployData,
    @SerialName("commission")
    val commission: CommissionData,
)
