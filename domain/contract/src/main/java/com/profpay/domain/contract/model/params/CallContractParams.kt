package com.profpay.domain.contract.model.params

import com.profpay.domain.contract.model.DealChangeStatus

/**
 * Параметры для вызова метода контракта
 */
data class CallContractParams(
    val userId: Long,
    val appId: String,
    val ownerAddress: String,
    val changeStatus: DealChangeStatus,
    val contract: ContractParams,
    val commission: CommissionParams,
)
