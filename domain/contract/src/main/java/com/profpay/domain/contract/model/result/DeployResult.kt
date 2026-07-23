package com.profpay.domain.contract.model.result

/**
 * Результат операции деплоя контракта
 */
data class DeployResult(
    val operationId: Long,
    val timestampSeconds: Long,
)
