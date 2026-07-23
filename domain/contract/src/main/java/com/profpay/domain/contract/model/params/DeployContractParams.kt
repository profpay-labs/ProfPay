package com.profpay.domain.contract.model.params

import com.profpay.domain.contract.model.params.ContractParams

/**
 * Параметры для деплоя контракта
 */
data class DeployContractParams(
    val userId: Long,
    val appId: String,
    val ownerAddress: String,
    val contract: ContractParams,
    val commission: CommissionParams,
)
