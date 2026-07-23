package com.profpay.data.contract.dto.request

import com.profpay.data.contract.dto.CommissionData
import com.profpay.data.contract.dto.ContractDeployData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request для деплоя смарт-контракта
 */
@Serializable
data class DeployContractRequest(
    @SerialName("userId")
    val userId: Long,
    @SerialName("appId")
    val appId: String,
    @SerialName("ownerAddress")
    val ownerAddress: String,
    @SerialName("contract")
    val contract: ContractDeployData,
    @SerialName("commission")
    val commission: CommissionData,
)
