// core/tron/src/main/java/com/profpay/core/tron/impl/SmartContractImpl.kt
package com.profpay.core.tron.impl

import android.content.Context
import android.util.Log
import com.google.protobuf.ByteString
import com.profpay.core.tron.api.MultiSigReadApi
import com.profpay.core.tron.api.MultiSigWriteApi
import com.profpay.core.tron.api.TronSmartContractApi
import com.profpay.core.tron.model.CreateDealParams
import com.profpay.core.tron.network.TronNodeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tron.trident.abi.FunctionReturnDecoder
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.Bool
import org.tron.trident.abi.datatypes.DynamicArray
import org.tron.trident.abi.datatypes.Function
import org.tron.trident.abi.datatypes.Type
import org.tron.trident.abi.datatypes.Utf8String
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.transaction.TransactionBuilder
import org.tron.trident.proto.Chain
import org.tron.trident.utils.Numeric
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartContractImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : TronSmartContractApi {

    override val multiSigRead: MultiSigReadApi = MultiSigReadImpl()
    override val multiSigWrite: MultiSigWriteApi = MultiSigWriteImpl()

    override fun deploy(
        ownerAddress: String,
        privateKey: String,
    ): String? {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

            try {
                val byteCode = readAssetFile("bytecode.txt")
                val abi = readAssetFile("abi.txt")

                val cntr: Contract = Contract.Builder()
                    .setName("MultiSigV3")
                    .setOwnerAddr(ApiWrapper.parseAddress(ownerAddress))
                    .setOriginAddr(ApiWrapper.parseAddress(ownerAddress))
                    .setBytecode(ByteString.copyFrom(Numeric.hexStringToByteArray(byteCode)))
                    .setAbi(abi)
                    .setOriginEnergyLimit(DEPLOY_ENERGY_LIMIT)
                    .setConsumeUserResourcePercent(100)
                    .build()
                cntr.wrapper = wrapper

                val builder: TransactionBuilder = cntr.deploy().setFeeLimit(DEPLOY_FEE_LIMIT)
                val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

                wrapper.broadcastTransaction(signedTransaction)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun getSignedDeployTransaction(
        ownerAddress: String,
        privateKey: String,
    ): ByteString? {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

            try {
                val byteCode = readAssetFile("bytecode.txt")
                val abi = readAssetFile("abi.txt")

                val cntr: Contract = Contract.Builder()
                    .setName("MultiSigV3")
                    .setOwnerAddr(ApiWrapper.parseAddress(ownerAddress))
                    .setOriginAddr(ApiWrapper.parseAddress(ownerAddress))
                    .setBytecode(ByteString.copyFrom(Numeric.hexStringToByteArray(byteCode)))
                    .setAbi(abi)
                    .setOriginEnergyLimit(DEPLOY_ENERGY_LIMIT)
                    .setConsumeUserResourcePercent(100)
                    .build()
                cntr.wrapper = wrapper

                val builder: TransactionBuilder = cntr.deploy().setFeeLimit(DEPLOY_FEE_LIMIT)
                val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

                signedTransaction.toByteString()
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override suspend fun estimateDeployingContract(privateKey: String): Pair<Long, Long> {
        return TronNodeManager.executeWithFailoverSuspend { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

            try {
                val entriesEnergy = wrapper.getEnergyPrices().prices.split(",")
                val lastEnergyEntry = entriesEnergy.last()
                val lastEnergyPrice = lastEnergyEntry.split(":").last().toLong()

                val entriesBandwidth = wrapper.getBandwidthPrices().prices.split(",")
                val lastBandwidthEntry = entriesBandwidth.last()
                val lastBandwidthPrice = lastBandwidthEntry.split(":").last().toLong()

                val energyResult = PUBLISH_ENERGY_REQUIRED * lastEnergyPrice
                val bandwidthResult = PUBLISH_BANDWIDTH_REQUIRED * lastBandwidthPrice

                Pair(energyResult, bandwidthResult)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    private fun readAssetFile(fileName: String): String {
        return context.assets.open(fileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        }
    }

    private fun safeClose(wrapper: ApiWrapper) {
        try {
            wrapper.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close ApiWrapper: $e")
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MultiSigRead Implementation
    // ══════════════════════════════════════════════════════════════════════

    private inner class MultiSigReadImpl : MultiSigReadApi {

        override fun getUsdt(
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): String {
            return TronNodeManager.executeWithFailover { node ->
                val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

                try {
                    val usdtFunc = Function(
                        "USDT",
                        emptyList(),
                        listOf(object : TypeReference<Address?>() {})
                    )
                    val extension = wrapper.triggerConstantContract(ownerAddress, contractAddress, usdtFunc)
                    val result = Numeric.toHexString(extension.getConstantResult(0).toByteArray())

                    val decodedResult = FunctionReturnDecoder.decode(result, usdtFunc.outputParameters)
                    decodedResult[0].value.toString()
                } finally {
                    safeClose(wrapper)
                }
            }
        }

        override fun getContractStats(
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): Pair<String, String> {
            return TronNodeManager.executeWithFailover { node ->
                val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

                try {
                    val function = Function(
                        "getContractStats",
                        emptyList(),
                        listOf(
                            object : TypeReference<Uint256?>() {},
                            object : TypeReference<Uint256?>() {}
                        )
                    )
                    val extension = wrapper.triggerConstantContract(ownerAddress, contractAddress, function)
                    val result = Numeric.toHexString(extension.getConstantResult(0).toByteArray())

                    val decodedResult = FunctionReturnDecoder.decode(result, function.outputParameters)
                    Pair(decodedResult[0].value.toString(), decodedResult[1].value.toString())
                } finally {
                    safeClose(wrapper)
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MultiSigWrite Implementation
    // ══════════════════════════════════════════════════════════════════════

    private inner class MultiSigWriteImpl : MultiSigWriteApi {

        override fun createDeal(
            ownerAddress: String,
            contractAddress: String,
            privateKey: String,
            params: CreateDealParams,
        ): ByteString {
            val contractParams = buildCreateDealParams(params)

            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "createDeal",
                params = contractParams,
                outputParams = listOf(object : TypeReference<Uint256?>() {}),
                feeLimit = DEFAULT_FEE_LIMIT,
            )
        }

        override fun depositDeal(
            id: Long,
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): ByteString {
            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "depositDeal",
                params = listOf(Uint256(id)),
                feeLimit = 150_000_000,
            )
        }

        override fun approve(
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): ByteString {
            return TronNodeManager.executeWithFailover { node ->
                val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

                try {
                    val maxAmount = BigInteger.valueOf(Long.MAX_VALUE)
                        .multiply(BigInteger.valueOf(10L).pow(6))

                    val builder = wrapper.triggerCall(
                        ownerAddress,
                        USDT_CONTRACT_ADDRESS,
                        Function(
                            "approve",
                            listOf(Address(contractAddress), Uint256(maxAmount)),
                            emptyList<TypeReference<Bool>>(),
                        ),
                    )
                    builder.setFeeLimit(55_000_000)
                    builder.setMemo("")

                    val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.build())
                    signedTransaction.toByteString()
                } finally {
                    safeClose(wrapper)
                }
            }
        }

        override fun voteDeal(
            id: Long,
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): ByteString {
            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "voteDeal",
                params = listOf(Uint256(id)),
            )
        }

        override fun cancelDeal(
            id: Long,
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): ByteString {
            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "cancelDeal",
                params = listOf(Uint256(id)),
            )
        }

        override fun executeDisputed(
            id: Long,
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): ByteString {
            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "executeDisputed",
                params = listOf(Uint256(id)),
            )
        }

        override fun paySellerExpertFee(
            id: Long,
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): ByteString {
            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "paySellerExpertFee",
                params = listOf(Uint256(id)),
            )
        }

        override fun assignDecisionAdminAndSetAmounts(
            id: Long,
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
            sellerValue: BigInteger,
            buyerValue: BigInteger,
        ): ByteString {
            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "assignDecisionAdminAndSetAmounts",
                params = listOf(Uint256(id), Uint256(sellerValue), Uint256(buyerValue)),
            )
        }

        override fun voteOnDisputeResolution(
            id: Long,
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): ByteString {
            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "voteOnDisputeResolution",
                params = listOf(Uint256(id)),
            )
        }

        override fun declineDisputeResolution(
            id: Long,
            ownerAddress: String,
            privateKey: String,
            contractAddress: String,
        ): ByteString {
            return executeContractCall(
                ownerAddress = ownerAddress,
                contractAddress = contractAddress,
                privateKey = privateKey,
                functionName = "declineDisputeResolution",
                params = listOf(Uint256(id)),
            )
        }

        /**
         * Конвертирует доменную модель в параметры смарт-контракта (Trident types).
         */
        private fun buildCreateDealParams(params: CreateDealParams): List<Type<*>> {
            val admins = params.admins.map { Address(it.walletAddress) }
            val adminStatuses = params.admins.map { Utf8String(it.tierName) }

            return listOf(
                Address(params.sellerAddress),
                Address(params.buyerAddress),
                Uint256(params.amount),
                DynamicArray(Address::class.java, admins),
                DynamicArray(Utf8String::class.java, adminStatuses),
            )
        }

        private fun executeContractCall(
            ownerAddress: String,
            contractAddress: String,
            privateKey: String,
            functionName: String,
            params: List<Type<*>>,
            outputParams: List<TypeReference<*>> = emptyList(),
            feeLimit: Long = DEFAULT_FEE_LIMIT,
        ): ByteString {
            return TronNodeManager.executeWithFailover { node ->
                val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

                try {
                    val function = Function(functionName, params, outputParams)
                    val builder: TransactionBuilder = wrapper
                        .triggerCall(ownerAddress, contractAddress, function)
                        .setFeeLimit(feeLimit)
                    val signedTransaction: Chain.Transaction = wrapper.signTransaction(builder.transaction)

                    signedTransaction.toByteString()
                } finally {
                    safeClose(wrapper)
                }
            }
        }
    }

    private companion object {
        const val TAG = "SmartContractImpl"
        const val USDT_CONTRACT_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        const val DEFAULT_FEE_LIMIT = 140_000_000L
        const val DEPLOY_FEE_LIMIT = 2_000_000_000L
        const val DEPLOY_ENERGY_LIMIT = 10_000_000L
        const val PUBLISH_ENERGY_REQUIRED = 1_000_000L // TODO: взять из конфига
        const val PUBLISH_BANDWIDTH_REQUIRED = 10_000L
    }
}
