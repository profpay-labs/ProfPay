package com.profpay.domain.contract.model.result

/**
 * Результат вызова контракта
 */
data class CallContractResult(
    val operationId: Long,
    val timestampSeconds: Long,
)
