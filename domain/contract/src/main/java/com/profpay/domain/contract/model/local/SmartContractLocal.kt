package com.profpay.domain.contract.model.local

/**
 * Локальная модель смарт-контракта.
 */
data class SmartContractLocal(
    val id: Long? = null,
    val contractAddress: String,
    val ownerAddress: String,
    val openDealsCount: Long = 0,
    val closedDealsCount: Long = 0,
) {
    /**
     * Общее количество сделок.
     */
    val totalDealsCount: Long
        get() = openDealsCount + closedDealsCount
}
