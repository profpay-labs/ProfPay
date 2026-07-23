package com.profpay.domain.contract.model.params

import com.profpay.domain.contract.model.DealChangeStatus

/**
 * Параметры для изменения статуса сделки
 */
data class DealStatusChangeParams(
    /** ID сделки в базе */
    val dealId: Long,
    /** ID пользователя */
    val userId: Long,
    /** Адрес контракта */
    val contractAddress: String,
    /** ID сделки в блокчейне */
    val blockchainDealId: Long,
    /** Новый статус */
    val changeStatus: DealChangeStatus,
)
