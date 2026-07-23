package com.profpay.domain.contract.model.params

import com.profpay.domain.contract.model.DealChangeStatus

/**
 * Параметры для действия по диспуту
 */
data class DisputeActionParams(
    /** ID сделки */
    val dealId: Long,
    /** ID пользователя-инициатора */
    val initiatorUserId: Long,
    /** Адрес контракта */
    val contractAddress: String,
    /** Тип действия */
    val action: DealChangeStatus,
)
