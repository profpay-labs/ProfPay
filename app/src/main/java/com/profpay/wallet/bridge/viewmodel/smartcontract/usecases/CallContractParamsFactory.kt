package com.profpay.wallet.bridge.viewmodel.smartcontract.usecases

import com.google.protobuf.ByteString
import com.profpay.core.crypto.util.toBase64
import com.profpay.domain.contract.model.Deal
import com.profpay.domain.contract.model.DealChangeStatus
import com.profpay.domain.contract.model.params.CallContractParams
import com.profpay.domain.contract.model.params.CommissionParams
import com.profpay.domain.contract.model.params.ContractParams
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.estimate.TransactionEstimatorResult
import java.math.BigInteger
import javax.inject.Inject

/**
 * Собирает [CallContractParams] из результатов оценки комиссии и подписанной транзакции.
 * Валидирует входные данные явно, вместо разбросанных по коду `!!`.
 */
class CallContractParamsFactory @Inject constructor() {

    fun create(
        userId: Long,
        appId: String,
        deal: Deal,
        changeStatus: DealChangeStatus,
        contractTxn: ByteString,
        estimatorResult: TransactionEstimatorResult?,
        commissionFee: CommissionFeeBuilderResult,
        commissionAmount: BigInteger,
    ): Result<CallContractParams> {
        val estimate = estimatorResult as? TransactionEstimatorResult.Success
            ?: return Result.failure(
                IllegalStateException(
                    "Fee estimation failed: ${estimatorResult?.errorMessage ?: "estimate is missing"}",
                ),
            )

        val commission = commissionFee as? CommissionFeeBuilderResult.Success
            ?: return Result.failure(
                IllegalStateException(
                    "Commission fee build failed: ${commissionFee.errorMessage ?: "unknown error"}",
                ),
            )

        return Result.success(
            CallContractParams(
                userId = userId,
                appId = appId,
                ownerAddress = commission.executorAddress,
                changeStatus = changeStatus,
                contract = ContractParams(
                    address = deal.contractAddress,
                    contractName = "",
                    estimateEnergy = estimate.requiredEnergy,
                    bandwidthRequired = estimate.requiredBandwidth,
                    txnBytes = contractTxn.toBase64(),
                ),
                commission = CommissionParams(
                    address = commission.executorAddress,
                    amount = commissionAmount.toString(),
                    bandwidthRequired = commission.requiredBandwidth,
                    txnBytes = commission.transaction.toBase64(),
                ),
            ),
        )
    }
}
