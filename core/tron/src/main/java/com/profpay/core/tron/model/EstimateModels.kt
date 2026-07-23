package com.profpay.core.tron.model

import java.math.BigInteger

/**
 * Результат оценки стоимости транзакции
 */
data class TransactionCostEstimate(
    val energyRequired: Long,
    val energyInTrx: BigInteger,
    val bandwidthRequired: Long,
    val bandwidthInTrx: Double,
)

/**
 * Параметры для оценки createDeal
 */
data class EstimateCreateDealParams(
    val ownerAddress: String,
    val contractAddress: String,
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

/**
 * Параметры для оценки простых операций с dealId
 */
data class EstimateDealOperationParams(
    val ownerAddress: String,
    val contractAddress: String,
    val dealId: Long,
)

/**
 * Параметры для оценки approve
 */
data class EstimateApproveParams(
    val ownerAddress: String,
    val spenderAddress: String,
    val amount: BigInteger? = null, // null = unlimited
)

/**
 * Параметры для оценки assignDecisionAdminAndSetAmounts
 */
data class EstimateAssignDecisionParams(
    val ownerAddress: String,
    val contractAddress: String,
    val dealId: Long,
    val sellerValue: Long,
    val buyerValue: Long,
)
