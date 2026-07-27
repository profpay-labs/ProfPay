package com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.estimate

import com.profpay.domain.contract.model.Deal
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.getOppositeUserId
import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.isBuyerNotDeposited
import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.isBuyerRequestInitialized
import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.isContractAwaitingUserConfirmation
import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.isDisputeNotAgreed
import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.isDisputeNotDeclined
import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.isExpertNotDecision
import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.isSellerNotPayedExpertFee
import javax.inject.Inject

class ProcessContractEstimatorUseCase
    @Inject
    constructor(
        private val profileLocalRepository: ProfileLocalRepository,
        private val transactionFeeEstimator: TransactionFeeEstimator,
    ) {
        suspend fun processCompleteSmartContract(deal: Deal): TransactionEstimatorResult? {
            val userId = profileLocalRepository.getUserId()
            return when {
                isBuyerRequestInitialized(deal, userId) ->
                    transactionFeeEstimator.createDeal(deal)
                isBuyerNotDeposited(deal, userId) ->
                    transactionFeeEstimator.approveAndDepositDeal(deal)
                isSellerNotPayedExpertFee(deal, userId) ->
                    transactionFeeEstimator.approveAndPaySellerExpertFee(deal, userId)
                isContractAwaitingUserConfirmation(deal, userId) ->
                    transactionFeeEstimator.confirmDeal(deal, userId)
                isExpertNotDecision(deal, userId) ->
                    null
                isDisputeNotAgreed(deal, userId) ->
                    transactionFeeEstimator.voteOnDisputeResolution(deal, userId)
                else ->
                    TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Unknown contract state")
            }
        }

        suspend fun processRejectSmartContract(deal: Deal): TransactionEstimatorResult? {
            val userId = profileLocalRepository.getUserId()

            return when {
                isBuyerRequestInitialized(deal, userId) ->
                    null
                isBuyerNotDeposited(deal, userId) ->
                    transactionFeeEstimator.rejectCancelDeal(deal, userId)
                isSellerNotPayedExpertFee(deal, userId) ||
                        isSellerNotPayedExpertFee(
                            deal,
                            getOppositeUserId(deal, userId),
                        ) ->
                    transactionFeeEstimator.rejectCancelDeal(deal, userId)
                isContractAwaitingUserConfirmation(deal, userId) ->
                    transactionFeeEstimator.executeDisputed(deal, userId)
                isDisputeNotDeclined(deal, userId) ->
                    transactionFeeEstimator.declineDisputeResolution(deal, userId)
                else ->
                    TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Unknown contract state")
            }
        }
    }
