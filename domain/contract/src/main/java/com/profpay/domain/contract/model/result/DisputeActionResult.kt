package com.profpay.domain.contract.model.result

import com.profpay.domain.contract.model.DealChangeStatus

/**
 * Результат действия по диспуту
 */
data class DisputeActionResult(
    /** ID сделки */
    val dealId: Long,
    /** Выполненное действие */
    val action: DealChangeStatus,
    /** Количество уведомлённых участников */
    val participantsNotified: Int,
    /** Timestamp обработки */
    val timestamp: Long,
)
