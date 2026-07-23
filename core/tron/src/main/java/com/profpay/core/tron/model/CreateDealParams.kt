package com.profpay.core.tron.model

/**
 * Параметры для создания сделки в смарт-контракте.
 * Доменная модель без зависимостей от Trident SDK.
 */
data class CreateDealParams(
    val sellerAddress: String,
    val buyerAddress: String,
    val amount: Long,
    val admins: List<AdminInfo>,
) {
    data class AdminInfo(
        val walletAddress: String,
        val tierName: String,
    )

    init {
        require(admins.size == 3) { "Exactly 3 admins required" }
    }
}
