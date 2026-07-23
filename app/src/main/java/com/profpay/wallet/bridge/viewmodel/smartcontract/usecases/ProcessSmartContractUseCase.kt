package com.profpay.wallet.bridge.viewmodel.smartcontract.usecases

import android.util.Log
import com.profpay.core.common.converter.toSunAmount
import com.profpay.domain.contract.model.Deal
import com.profpay.domain.contract.model.DealChangeStatus
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.wallet.bridge.viewmodel.smartcontract.CompleteReturnData
import com.profpay.wallet.bridge.viewmodel.smartcontract.CompleteStatusesEnum
import com.profpay.wallet.bridge.viewmodel.smartcontract.SmartContractModalStateHolder
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.estimate.ProcessContractEstimatorUseCase
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.estimate.TransactionEstimatorResult
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class ProcessSmartContractUseCase @Inject constructor(
    private val profileLocalRepository: ProfileLocalRepository,
    private val modalState: SmartContractModalStateHolder,
    private val blockchainOperations: BlockchainOperations,
    private val grpcOperations: GrpcOperations,
    private val processContractEstimatorUseCase: ProcessContractEstimatorUseCase,
    private val commissionFeeBuilder: CommissionFeeBuilder,
    private val callContractParamsFactory: CallContractParamsFactory,
) {

    suspend fun processCompleteSmartContract(
        commission: BigDecimal,
        deal: Deal,
    ): CompleteReturnData {
        val userId = profileLocalRepository.getUserId()
        val context = buildContext(
            userId = userId,
            deal = deal,
            commission = commission,
            estimatorResult = processContractEstimatorUseCase.processCompleteSmartContract(deal),
        )

        val result: DealActionResult? = when {
            isBuyerRequestInitialized(deal, userId) -> executeDealAction(
                context = context,
                modalMessage = "Создание сделки в смарт-контракте..",
                changeStatus = DealChangeStatus.BUYER_CREATED,
            ) { blockchainOperations.createDeal(deal) }

            isBuyerNotDeposited(deal, userId) -> executeDealAction(
                context = context,
                modalMessage = "Выдача approve адресу и перевод средств.",
                changeStatus = DealChangeStatus.BUYER_DEPOSITED,
            ) { blockchainOperations.approveAndDepositDeal(deal) }

            isSellerNotPayedExpertFee(deal, userId) -> executeDealAction(
                context = context,
                modalMessage = "Оплата комиссии экспертам",
                changeStatus = DealChangeStatus.SELLER_PAID_EXPERT_FEE,
            ) { blockchainOperations.approveAndPaySellerExpertFee(deal, userId) }

            isContractAwaitingUserConfirmation(deal, userId) -> {
                val changeStatus = when (userId) {
                    deal.buyer.userId -> DealChangeStatus.BUYER_CONFIRMED
                    deal.seller.userId -> DealChangeStatus.SELLER_CONFIRMED
                    else -> return unknownContractState()
                }
                executeDealAction(
                    context = context,
                    modalMessage = "Подтверждение исполнения условий договора",
                    changeStatus = changeStatus,
                ) { blockchainOperations.confirmDeal(deal, userId) }
            }

            isExpertNotDecision(deal, userId) ->
                return CompleteReturnData(status = CompleteStatusesEnum.CALL_EXPERT_AMOUNT_SHEET)

            isDisputeNotAgreed(deal, userId) -> {
                val changeStatus = resolveDisputeStatus(
                    deal = deal,
                    userId = userId,
                    expertStatus = DealChangeStatus.EXPERT_DISPUTE_AGREED,
                    buyerStatus = DealChangeStatus.BUYER_DISPUTE_AGREED,
                    sellerStatus = DealChangeStatus.SELLER_DISPUTE_AGREED,
                ) ?: return unknownContractState()
                executeDealAction(
                    context = context,
                    modalMessage = "Регистрация решения в блокчейне",
                    changeStatus = changeStatus,
                ) { blockchainOperations.voteOnDisputeResolution(deal, userId) }
            }

            else -> return unknownContractState()
        }

        modalState.hide()
        return result.toCompleteReturnData()
    }

    suspend fun processRejectSmartContract(
        commission: BigDecimal,
        deal: Deal,
    ): CompleteReturnData {
        val userId = profileLocalRepository.getUserId()
        val context = buildContext(
            userId = userId,
            deal = deal,
            commission = commission,
            estimatorResult = processContractEstimatorUseCase.processRejectSmartContract(deal),
        )

        val result: DealActionResult? = when {
            isBuyerRequestInitialized(deal, userId) -> {
                modalState.show("Инициирование закрытия контракта..")
                grpcOperations.contractDealStatusChanged(
                    deal,
                    deal.contractAddress,
                    DealChangeStatus.BUYER_DELETE_CONTRACT,
                )
                null
            }

            isBuyerNotDeposited(deal, userId) -> executeDealAction(
                context = context,
                modalMessage = "Инициирование закрытия контракта..",
                changeStatus = DealChangeStatus.BUYER_CANCEL_CONTRACT,
            ) { blockchainOperations.rejectCancelDeal(deal, userId) }

            isSellerNotPayedExpertFee(deal, userId) ||
                isSellerNotPayedExpertFee(deal, getOppositeUserId(deal, userId)) -> {
                val changeStatus = when (userId) {
                    deal.buyer.userId -> DealChangeStatus.BUYER_CANCEL_PAID_CONTRACT
                    deal.seller.userId -> DealChangeStatus.SELLER_CANCEL_CONTRACT
                    else -> return unknownContractState()
                }
                executeDealAction(
                    context = context,
                    modalMessage = "Инициирование закрытия контракта, возврат средств и отправка комиссий..",
                    changeStatus = changeStatus,
                ) { blockchainOperations.rejectCancelDeal(deal, userId) }
            }

            isContractAwaitingUserConfirmation(deal, userId) -> {
                val changeStatus = if (userId == deal.buyer.userId) {
                    DealChangeStatus.BUYER_OPEN_DISPUTE
                } else {
                    DealChangeStatus.SELLER_OPEN_DISPUTE
                }
                executeDealAction(
                    context = context,
                    modalMessage = "Открытие диспута..",
                    changeStatus = changeStatus,
                ) { blockchainOperations.executeDisputed(deal, userId) }
            }

            isDisputeNotDeclined(deal, userId) -> {
                val changeStatus = resolveDisputeStatus(
                    deal = deal,
                    userId = userId,
                    expertStatus = DealChangeStatus.EXPERT_DISPUTE_DECLINE,
                    buyerStatus = DealChangeStatus.BUYER_DISPUTE_DECLINE,
                    sellerStatus = DealChangeStatus.SELLER_DISPUTE_DECLINE,
                ) ?: return unknownContractState()
                executeDealAction(
                    context = context,
                    modalMessage = "Регистрация решения в блокчейне",
                    changeStatus = changeStatus,
                ) { blockchainOperations.declineDisputeResolution(deal, userId) }
            }

            else -> return unknownContractState()
        }

        modalState.hide()
        return result.toCompleteReturnData()
    }

    suspend fun expertSetDecision(
        deal: Deal,
        sellerValue: BigInteger,
        buyerValue: BigInteger,
    ) {
        val userId = profileLocalRepository.getUserId()

        modalState.show("Установка новых условий для контракта")

        val result = blockchainOperations.assignDecisionAdminAndSetAmounts(
            deal = deal,
            userId = userId,
            sellerValue = sellerValue,
            buyerValue = buyerValue,
        )

        when (result) {
            is DealActionResult.Success -> grpcOperations.contractDealStatusExpertChanged(
                deal,
                deal.contractAddress,
                DealChangeStatus.EXPERT_SET_DECISION,
            )
            is DealActionResult.InsufficientFunds ->
                Log.w(TAG, "Insufficient funds (${result.type}), required: ${result.amountRequired}")
            is DealActionResult.Error ->
                Log.e(TAG, "expertSetDecision failed: ${result.reason}")
        }

        modalState.hide()
    }

    /**
     * Общий сценарий: показать модалку → выполнить blockchain-операцию →
     * при успехе собрать параметры и вызвать контракт на сервере.
     */
    private suspend fun executeDealAction(
        context: DealActionContext,
        modalMessage: String,
        changeStatus: DealChangeStatus,
        blockchainAction: suspend () -> DealActionResult,
    ): DealActionResult {
        modalState.show(modalMessage)

        val actionResult = blockchainAction()
        if (actionResult !is DealActionResult.Success) return actionResult

        return callContractParamsFactory.create(
            userId = context.userId,
            appId = profileLocalRepository.getAppId(),
            deal = context.deal,
            changeStatus = changeStatus,
            contractTxn = actionResult.transaction,
            estimatorResult = context.estimatorResult,
            commissionFee = context.commissionFee,
            commissionAmount = context.commissionAmount,
        ).fold(
            onSuccess = { params ->
                grpcOperations.callContract(params)
                actionResult
            },
            onFailure = { error ->
                DealActionResult.Error(error.message ?: "Failed to build contract call params")
            },
        )
    }

    private suspend fun buildContext(
        userId: Long,
        deal: Deal,
        commission: BigDecimal,
        estimatorResult: TransactionEstimatorResult?,
    ): DealActionContext {
        val commissionAmount = commission.toSunAmount()
        return DealActionContext(
            userId = userId,
            deal = deal,
            commissionAmount = commissionAmount,
            estimatorResult = estimatorResult,
            commissionFee = commissionFeeBuilder.build(
                commission = commissionAmount,
                userId = userId,
                deal = deal,
            ),
        )
    }

    private fun resolveDisputeStatus(
        deal: Deal,
        userId: Long,
        expertStatus: DealChangeStatus,
        buyerStatus: DealChangeStatus,
        sellerStatus: DealChangeStatus,
    ): DealChangeStatus? = when {
        deal.admins.any { it.userId == userId } -> expertStatus
        userId == deal.buyer.userId -> buyerStatus
        userId == deal.seller.userId -> sellerStatus
        else -> null
    }

    private fun unknownContractState(): CompleteReturnData {
        Log.e(TAG, "Unknown contract state")
        modalState.hide()
        return CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
    }

    private fun DealActionResult?.toCompleteReturnData(): CompleteReturnData = when (this) {
        is DealActionResult.Success -> CompleteReturnData(status = CompleteStatusesEnum.OK)
        is DealActionResult.InsufficientFunds, is DealActionResult.Error ->
            CompleteReturnData(status = CompleteStatusesEnum.OK, result = this)
        null -> CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
    }

    private data class DealActionContext(
        val userId: Long,
        val deal: Deal,
        val commissionAmount: BigInteger,
        val estimatorResult: TransactionEstimatorResult?,
        val commissionFee: CommissionFeeBuilderResult,
    )

    private companion object {
        const val TAG = "ProcessSmartContractUseCase"
    }
}
