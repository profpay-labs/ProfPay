package com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.estimate

import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.core.tron.model.EstimateApproveParams
import com.profpay.core.tron.model.EstimateAssignDecisionParams
import com.profpay.core.tron.model.EstimateCreateDealParams
import com.profpay.core.tron.model.EstimateDealOperationParams
import com.profpay.core.tron.model.TransactionCostEstimate
import com.profpay.domain.contract.model.Deal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

class TransactionFeeEstimator @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val tron: Tron,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun createDeal(deal: Deal): TransactionEstimatorResult {
        val addressData = withContext(ioDispatcher) {
            addressLocalRepository.getByAddress(deal.buyer.walletAddress)
        } ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Address data is null")

        val params = EstimateCreateDealParams(
            ownerAddress = addressData.address,
            contractAddress = deal.contractAddress,
            sellerAddress = deal.seller.walletAddress,
            buyerAddress = deal.buyer.walletAddress,
            amount = deal.amount,
            admins = deal.admins.map { admin ->
                EstimateCreateDealParams.AdminInfo(
                    walletAddress = admin.walletAddress,
                    tierName = admin.tier.name,
                )
            },
        )

        val estimate = tron.estimates.estimateCreateDeal(params)
        return estimate.toSuccessResult(addressData.address, EstimateType.DEFAULT)
    }

    suspend fun approveAndDepositDeal(deal: Deal): TransactionEstimatorResult {
        val addressData = withContext(ioDispatcher) {
            addressLocalRepository.getByAddress(deal.buyer.walletAddress)
        } ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Address data is null")

        val isAllowanceUnlimited = tron.accounts.isAllowanceUnlimited(
            spender = deal.contractAddress,
            ownerAddress = addressData.address,
            privateKey = generateTempPrivateKey(),
        )

        if (!isAllowanceUnlimited) {
            val approveEstimate = tron.estimates.estimateApprove(
                EstimateApproveParams(
                    ownerAddress = addressData.address,
                    spenderAddress = deal.contractAddress,
                ),
            )
            return approveEstimate.toSuccessResult(addressData.address, EstimateType.APPROVE)
        }

        val depositEstimate = tron.estimates.estimateDepositDeal(
            EstimateDealOperationParams(
                ownerAddress = addressData.address,
                contractAddress = deal.contractAddress,
                dealId = deal.blockchainDealId,
            ),
        )
        return depositEstimate.toSuccessResult(addressData.address, EstimateType.DEFAULT)
    }

    suspend fun approveAndPaySellerExpertFee(deal: Deal, userId: Long): TransactionEstimatorResult {
        val address = resolveUserAddress(deal, userId)
        val addressData = withContext(ioDispatcher) {
            addressLocalRepository.getByAddress(address)
        } ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Address data is null")

        val allowanceValue = tron.accounts.allowance(
            spender = deal.contractAddress,
            ownerAddress = addressData.address,
            privateKey = generateTempPrivateKey(),
        )

        val approveAmount = deal.blockchainData!!.totalExpertCommissions / 2
        val needsApprove = allowanceValue == null || allowanceValue < approveAmount.toBigInteger()

        if (needsApprove) {
            val approveEstimate = tron.estimates.estimateApprove(
                EstimateApproveParams(
                    ownerAddress = addressData.address,
                    spenderAddress = deal.contractAddress,
                    amount = approveAmount.toBigInteger(),
                ),
            )
            return approveEstimate.toSuccessResult(addressData.address, EstimateType.APPROVE)
        }

        val payFeeEstimate = tron.estimates.estimatePaySellerExpertFee(
            EstimateDealOperationParams(
                ownerAddress = addressData.address,
                contractAddress = deal.contractAddress,
                dealId = deal.blockchainDealId,
            ),
        )
        return payFeeEstimate.toSuccessResult(addressData.address, EstimateType.DEFAULT)
    }

    suspend fun confirmDeal(deal: Deal, userId: Long): TransactionEstimatorResult {
        return estimateSimpleDealOperation(deal, userId, "voteDeal") { params ->
            tron.estimates.estimateVoteDeal(params)
        }
    }

    suspend fun rejectCancelDeal(deal: Deal, userId: Long): TransactionEstimatorResult {
        return estimateSimpleDealOperation(deal, userId, "cancelDeal") { params ->
            tron.estimates.estimateCancelDeal(params)
        }
    }

    suspend fun executeDisputed(deal: Deal, userId: Long): TransactionEstimatorResult {
        return estimateSimpleDealOperation(deal, userId, "executeDisputed") { params ->
            tron.estimates.estimateExecuteDisputed(params)
        }
    }

    suspend fun assignDecisionAdminAndSetAmounts(
        deal: Deal,
        userId: Long,
        sellerValue: Long,
        buyerValue: Long,
    ): TransactionEstimatorResult {
        val admin = deal.admins.find { it.userId == userId }
            ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "None admin")

        val addressData = withContext(ioDispatcher) {
            addressLocalRepository.getByAddress(admin.walletAddress)
        } ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Address data is null")

        val estimate = tron.estimates.estimateAssignDecisionAdminAndSetAmounts(
            EstimateAssignDecisionParams(
                ownerAddress = addressData.address,
                contractAddress = deal.contractAddress,
                dealId = deal.blockchainDealId,
                sellerValue = sellerValue,
                buyerValue = buyerValue,
            ),
        )
        return estimate.toSuccessResult(addressData.address, EstimateType.DEFAULT)
    }

    suspend fun voteOnDisputeResolution(deal: Deal, userId: Long): TransactionEstimatorResult {
        return estimateDisputeOperation(deal, userId) { params ->
            tron.estimates.estimateVoteOnDisputeResolution(params)
        }
    }

    suspend fun declineDisputeResolution(deal: Deal, userId: Long): TransactionEstimatorResult {
        return estimateDisputeOperation(deal, userId) { params ->
            tron.estimates.estimateDeclineDisputeResolution(params)
        }
    }

    private suspend fun estimateSimpleDealOperation(
        deal: Deal,
        userId: Long,
        operationName: String,
        estimator: (EstimateDealOperationParams) -> TransactionCostEstimate,
    ): TransactionEstimatorResult {
        val address = resolveUserAddress(deal, userId)
        val addressData = withContext(ioDispatcher) {
            addressLocalRepository.getByAddress(address)
        } ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Address data is null")

        val params = EstimateDealOperationParams(
            ownerAddress = addressData.address,
            contractAddress = deal.contractAddress,
            dealId = deal.blockchainDealId,
        )

        val estimate = estimator(params)
        return estimate.toSuccessResult(addressData.address, EstimateType.DEFAULT)
    }

    private suspend fun estimateDisputeOperation(
        deal: Deal,
        userId: Long,
        estimator: (EstimateDealOperationParams) -> TransactionCostEstimate,
    ): TransactionEstimatorResult {
        val address = deal.admins.firstOrNull { it.userId == userId }?.walletAddress
            ?: when (userId) {
                deal.buyer.userId -> deal.buyer.walletAddress
                deal.seller.userId -> deal.seller.walletAddress
                else -> return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "None address")
            }

        val addressData = withContext(ioDispatcher) {
            addressLocalRepository.getByAddress(address)
        } ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Address data is null")

        val params = EstimateDealOperationParams(
            ownerAddress = addressData.address,
            contractAddress = deal.contractAddress,
            dealId = deal.blockchainDealId,
        )

        val estimate = estimator(params)
        return estimate.toSuccessResult(addressData.address, EstimateType.DEFAULT)
    }

    private fun resolveUserAddress(deal: Deal, userId: Long): String {
        return if (userId == deal.buyer.userId) {
            deal.buyer.walletAddress
        } else {
            deal.seller.walletAddress
        }
    }

    private fun generateTempPrivateKey(): String {
        // Для операций чтения (allowance) не нужен реальный ключ
        return "0000000000000000000000000000000000000000000000000000000000000001"
    }

    private fun TransactionCostEstimate.toSuccessResult(
        executorAddress: String,
        estimateType: EstimateType,
    ): TransactionEstimatorResult.Success {
        return TransactionEstimatorResult.Success(
            executorAddress = executorAddress,
            requiredEnergyInTrx = energyInTrx,
            requiredEnergy = energyRequired,
            requiredBandwidthInTrx = bandwidthInTrx,
            requiredBandwidth = bandwidthRequired,
            estimateType = estimateType,
        )
    }

    private fun getBalance(address: String): BigInteger = tron.addressUtilities.getTrxBalance(address)

    data class EstimateResult(
        val requiredEnergyInTrx: BigInteger,
        val requiredEnergy: Long,
        val requiredBandwidthInTrx: Double,
        val requiredBandwidth: Long,
    )
}

sealed class TransactionEstimatorResult(
    open val estimateType: EstimateType,
    open val executorAddress: String? = null,
    open val requiredEnergyInTrx: BigInteger? = null,
    open val requiredEnergy: Long? = null,
    open val requiredBandwidthInTrx: Double? = null,
    open val requiredBandwidth: Long? = null,
    open val errorMessage: String? = null,
) {
    data class Success(
        override val executorAddress: String,
        override val requiredEnergyInTrx: BigInteger,
        override val requiredEnergy: Long,
        override val requiredBandwidthInTrx: Double,
        override val requiredBandwidth: Long,
        override val estimateType: EstimateType,
    ) : TransactionEstimatorResult(
            estimateType,
            executorAddress,
            requiredEnergyInTrx,
            requiredEnergy,
            requiredBandwidthInTrx,
            requiredBandwidth,
        )

    data class Error(
        override val estimateType: EstimateType,
        override val errorMessage: String,
    ) : TransactionEstimatorResult(estimateType, errorMessage = errorMessage)
}

enum class EstimateType {
    DEFAULT,
    APPROVE,
}
