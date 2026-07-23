package com.profpay.core.tron.impl

import android.util.Log
import com.profpay.core.tron.api.TronEstimateApi
import com.profpay.core.tron.model.EstimateApproveParams
import com.profpay.core.tron.model.EstimateAssignDecisionParams
import com.profpay.core.tron.model.EstimateCreateDealParams
import com.profpay.core.tron.model.EstimateDealOperationParams
import com.profpay.core.tron.model.TransactionCostEstimate
import com.profpay.core.tron.network.TronNodeManager
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.Bool
import org.tron.trident.abi.datatypes.DynamicArray
import org.tron.trident.abi.datatypes.Function
import org.tron.trident.abi.datatypes.Type
import org.tron.trident.abi.datatypes.Utf8String
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.key.KeyPair
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EstimateImpl @Inject constructor() : TronEstimateApi {

    override fun estimateCreateDeal(params: EstimateCreateDealParams): TransactionCostEstimate {
        val admins = params.admins.map { Address(it.walletAddress) }
        val adminStatuses = params.admins.map { Utf8String(it.tierName) }

        val functionParams = listOf<Type<*>>(
            Address(params.sellerAddress),
            Address(params.buyerAddress),
            Uint256(params.amount),
            DynamicArray(Address::class.java, admins),
            DynamicArray(Utf8String::class.java, adminStatuses),
        )

        val function = Function(
            "createDeal",
            functionParams,
            listOf(object : TypeReference<Uint256?>() {}),
        )

        return estimateFunction(
            function = function,
            ownerAddress = params.ownerAddress,
            contractAddress = params.contractAddress,
        )
    }

    override fun estimateDepositDeal(params: EstimateDealOperationParams): TransactionCostEstimate {
        return estimateDealOperation("depositDeal", params)
    }

    override fun estimateVoteDeal(params: EstimateDealOperationParams): TransactionCostEstimate {
        return estimateDealOperation("voteDeal", params)
    }

    override fun estimateCancelDeal(params: EstimateDealOperationParams): TransactionCostEstimate {
        return estimateDealOperation("cancelDeal", params)
    }

    override fun estimateExecuteDisputed(params: EstimateDealOperationParams): TransactionCostEstimate {
        return estimateDealOperation("executeDisputed", params)
    }

    override fun estimatePaySellerExpertFee(params: EstimateDealOperationParams): TransactionCostEstimate {
        return estimateDealOperation("paySellerExpertFee", params)
    }

    override fun estimateVoteOnDisputeResolution(params: EstimateDealOperationParams): TransactionCostEstimate {
        return estimateDealOperation("voteOnDisputeResolution", params)
    }

    override fun estimateDeclineDisputeResolution(params: EstimateDealOperationParams): TransactionCostEstimate {
        return estimateDealOperation("declineDisputeResolution", params)
    }

    override fun estimateAssignDecisionAdminAndSetAmounts(
        params: EstimateAssignDecisionParams,
    ): TransactionCostEstimate {
        val function = Function(
            "assignDecisionAdminAndSetAmounts",
            listOf(
                Uint256(params.dealId),
                Uint256(BigInteger.valueOf(params.sellerValue)),
                Uint256(BigInteger.valueOf(params.buyerValue)),
            ),
            emptyList<TypeReference<*>>(),
        )

        return estimateFunction(
            function = function,
            ownerAddress = params.ownerAddress,
            contractAddress = params.contractAddress,
        )
    }

    override fun estimateApprove(params: EstimateApproveParams): TransactionCostEstimate {
        val amount = params.amount
            ?: BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(10L).pow(USDT_DECIMALS))

        val function = Function(
            "approve",
            listOf(Address(params.spenderAddress), Uint256(amount)),
            emptyList<TypeReference<Bool>>(),
        )

        return estimateFunction(
            function = function,
            ownerAddress = params.ownerAddress,
            contractAddress = USDT_CONTRACT_ADDRESS,
        )
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════════

    private fun estimateDealOperation(
        functionName: String,
        params: EstimateDealOperationParams,
    ): TransactionCostEstimate {
        val function = Function(
            functionName,
            listOf(Uint256(params.dealId)),
            emptyList<TypeReference<*>>(),
        )

        return estimateFunction(
            function = function,
            ownerAddress = params.ownerAddress,
            contractAddress = params.contractAddress,
        )
    }

    private fun estimateFunction(
        function: Function,
        ownerAddress: String,
        contractAddress: String,
    ): TransactionCostEstimate {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                KeyPair.generate().toPrivateKey(),
            )

            try {
                // Estimate energy
                val energyEstimate = wrapper.estimateEnergy(
                    ownerAddress,
                    contractAddress,
                    function,
                )
                val energyRequired = energyEstimate.energyRequired

                // Get energy price
                val energyPrices = wrapper.getEnergyPrices().prices.split(",")
                val lastEnergyPrice = energyPrices.last().split(":").last().toLong()
                val energyInTrx = BigInteger.valueOf(energyRequired * lastEnergyPrice)

                // Estimate bandwidth (approximate based on function complexity)
                val bandwidthRequired = estimateBandwidthForFunction(function)

                // Get bandwidth price
                val bandwidthPrices = wrapper.getBandwidthPrices().prices.split(",")
                val lastBandwidthPrice = bandwidthPrices.last().split(":").last().toLong()
                val bandwidthInTrx = bandwidthRequired * lastBandwidthPrice / 1_000_000.0

                TransactionCostEstimate(
                    energyRequired = energyRequired,
                    energyInTrx = energyInTrx,
                    bandwidthRequired = bandwidthRequired,
                    bandwidthInTrx = bandwidthInTrx,
                )
            } finally {
                safeClose(wrapper)
            }
        }
    }

    private fun estimateBandwidthForFunction(function: Function): Long {
        // Базовая оценка bandwidth на основе размера параметров
        val baseSize = 300L // базовый размер транзакции
        val paramSize = function.inputParameters.sumOf { param ->
            when (param) {
                is DynamicArray<*> -> 32L + param.value.size * 32L
                else -> 32L
            }
        }
        return baseSize + paramSize
    }

    private fun safeClose(wrapper: ApiWrapper) {
        try {
            wrapper.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close ApiWrapper: $e")
        }
    }

    private companion object {
        const val TAG = "EstimateImpl"
        const val USDT_CONTRACT_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        const val USDT_DECIMALS = 6
    }
}
