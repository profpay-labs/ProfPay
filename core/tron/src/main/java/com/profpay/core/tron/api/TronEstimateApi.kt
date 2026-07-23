package com.profpay.core.tron.api

import com.profpay.core.tron.model.EstimateApproveParams
import com.profpay.core.tron.model.EstimateAssignDecisionParams
import com.profpay.core.tron.model.EstimateCreateDealParams
import com.profpay.core.tron.model.EstimateDealOperationParams
import com.profpay.core.tron.model.TransactionCostEstimate

/**
 * API для оценки стоимости транзакций смарт-контрактов
 */
interface TronEstimateApi {

    fun estimateCreateDeal(params: EstimateCreateDealParams): TransactionCostEstimate

    fun estimateDepositDeal(params: EstimateDealOperationParams): TransactionCostEstimate

    fun estimateVoteDeal(params: EstimateDealOperationParams): TransactionCostEstimate

    fun estimateCancelDeal(params: EstimateDealOperationParams): TransactionCostEstimate

    fun estimateExecuteDisputed(params: EstimateDealOperationParams): TransactionCostEstimate

    fun estimatePaySellerExpertFee(params: EstimateDealOperationParams): TransactionCostEstimate

    fun estimateVoteOnDisputeResolution(params: EstimateDealOperationParams): TransactionCostEstimate

    fun estimateDeclineDisputeResolution(params: EstimateDealOperationParams): TransactionCostEstimate

    fun estimateAssignDecisionAdminAndSetAmounts(params: EstimateAssignDecisionParams): TransactionCostEstimate

    fun estimateApprove(params: EstimateApproveParams): TransactionCostEstimate
}
