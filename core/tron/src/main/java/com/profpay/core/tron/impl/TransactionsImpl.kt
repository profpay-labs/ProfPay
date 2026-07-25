// core/tron/src/main/java/com/profpay/core/tron/impl/TransactionsImpl.kt
package com.profpay.core.tron.impl

import android.util.Log
import com.profpay.core.tron.api.TronTransactionsApi
import com.profpay.core.tron.model.BandwidthEstimate
import com.profpay.core.tron.model.EnergyEstimate
import com.profpay.core.tron.model.SignedTransactionData
import com.profpay.core.tron.network.TronNodeManager
import org.bitcoinj.base.internal.ByteUtils
import org.tron.trident.abi.FunctionEncoder
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.Bool
import org.tron.trident.abi.datatypes.Function
import org.tron.trident.abi.datatypes.Type
import org.tron.trident.abi.datatypes.generated.Uint256
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.contract.Trc20Contract
import org.tron.trident.core.exceptions.IllegalException
import org.tron.trident.core.transaction.BlockId
import org.tron.trident.core.transaction.TransactionBuilder
import org.tron.trident.proto.Chain
import org.tron.trident.proto.Response
import java.math.BigInteger
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsImpl @Inject constructor() : TronTransactionsApi {

    // ══════════════════════════════════════════════════════════════════════
    // Transfers
    // ══════════════════════════════════════════════════════════════════════

    override fun trc20Transfer(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: Long,
    ): String {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val contract: Contract = wrapper.getContract(USDT_CONTRACT_ADDRESS)
                val token = Trc20Contract(contract, fromAddress, wrapper)
                token.transfer(toAddress, amount, 0, "", TRC20_FEE_LIMIT)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun trxTransfer(
        fromAddress: String,
        toAddress: String,
        privateKey: String,
        amount: Long,
    ): String {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

            try {
                val txnExt = wrapper.transfer(fromAddress, toAddress, amount)
                if (Response.TransactionReturn.response_code.SUCCESS != txnExt.result.code) {
                    throw IllegalException(txnExt.result.message.toStringUtf8())
                }

                val signedTransaction: Chain.Transaction = wrapper.signTransaction(txnExt)
                wrapper.broadcastTransaction(signedTransaction)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Signed Transactions (for server broadcast)
    // ══════════════════════════════════════════════════════════════════════

    override fun getSignedTrxTransaction(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): SignedTransactionData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                // Получаем текущий блок и формируем корректный BlockId для TAPOS
                val nowBlock = wrapper.getNowBlock()
                val blockId = createBlockIdFromBlock(nowBlock)
                val blockTimestamp = nowBlock.blockHeader.rawData.timestamp
                val expireTime = blockTimestamp + TRANSACTION_EXPIRE_OFFSET_MS

                wrapper.enableLocalCreate(blockId, expireTime)

                val txnExt = wrapper.transfer(fromAddress, toAddress, amount.toLong())

                if (Response.TransactionReturn.response_code.SUCCESS != txnExt.result.code) {
                    throw IllegalException(txnExt.result.message.toStringUtf8())
                }

                val signedTransaction: Chain.Transaction = wrapper.signTransaction(txnExt)
                val txidHex = calculateTxId(signedTransaction)

                SignedTransactionData(
                    txid = txidHex,
                    signedTxn = signedTransaction.toByteString(),
                )
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun getSignedUsdtTransaction(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): SignedTransactionData {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                // Получаем текущий блок и формируем корректный BlockId для TAPOS
                val nowBlock = wrapper.getNowBlock()
                val blockId = createBlockIdFromBlock(nowBlock)
                val blockTimestamp = nowBlock.blockHeader.rawData.timestamp
                val expireTime = blockTimestamp + TRANSACTION_EXPIRE_OFFSET_MS

                wrapper.enableLocalCreate(blockId, expireTime)

                val transfer = createTransferFunction(toAddress, amount)
                val builder: TransactionBuilder = wrapper.triggerCall(
                    fromAddress,
                    USDT_CONTRACT_ADDRESS,
                    transfer,
                )
                builder.setFeeLimit(TRC20_FEE_LIMIT)
                builder.setMemo("")

                val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())
                val txidHex = calculateTxId(signedTxn)

                SignedTransactionData(
                    txid = txidHex,
                    signedTxn = signedTxn.toByteString(),
                )
            } finally {
                safeClose(wrapper)
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Energy Estimation
    // ══════════════════════════════════════════════════════════════════════

    override fun estimateEnergy(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): EnergyEstimate {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val transfer = createTransferFunction(toAddress, amount)
                val energyRequired = wrapper.estimateEnergy(
                    fromAddress,
                    USDT_CONTRACT_ADDRESS,
                    transfer,
                ).energyRequired

                val energyFee = getEnergyFee(wrapper)
                val energyInTrx = energyRequired.toBigInteger() * energyFee


                EnergyEstimate(
                    energy = energyRequired,
                    energyInTrx = energyInTrx,
                )
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun estimateEnergy(
        function: Function,
        contractAddress: String,
        address: String,
        privateKey: ByteArray,
    ): EnergyEstimate {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val func = FunctionEncoder.encode(function)
                val energyResult = wrapper.estimateEnergyV2(address, contractAddress, func)

                val energyFee = getEnergyFee(wrapper)
                val energyInTrx = energyResult.energyRequired.toBigInteger() * energyFee

                EnergyEstimate(
                    energy = energyResult.energyRequired,
                    energyInTrx = energyInTrx,
                )
            } finally {
                safeClose(wrapper)
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Bandwidth Estimation
    // ══════════════════════════════════════════════════════════════════════

    override fun estimateBandwidth(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): BandwidthEstimate {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val transfer = createTransferFunction(toAddress, amount)
                val builder: TransactionBuilder = wrapper.triggerCall(
                    fromAddress,
                    USDT_CONTRACT_ADDRESS,
                    transfer,
                )
                builder.setFeeLimit(TRC20_FEE_LIMIT)
                builder.setMemo("")

                val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())
                val bandwidthRequired = wrapper.estimateBandwidth(signedTxn)

                BandwidthEstimate(bandwidth = bandwidthRequired + BANDWIDTH_BUFFER)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun estimateBandwidthTrx(
        fromAddress: String,
        toAddress: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): BandwidthEstimate {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val txnExt = wrapper.transfer(fromAddress, toAddress, amount.toLong())
                val bandwidthRequired = wrapper.estimateBandwidth(txnExt.transaction)

                BandwidthEstimate(bandwidth = bandwidthRequired + BANDWIDTH_BUFFER)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun estimateBandwidth(
        function: Function,
        contractAddress: String,
        address: String,
        privateKey: ByteArray,
    ): BandwidthEstimate {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                ByteUtils.formatHex(privateKey)
            )

            try {
                val func = FunctionEncoder.encode(function)
                val builder: TransactionBuilder = wrapper.triggerCallV2(
                    address,
                    contractAddress,
                    func,
                )
                builder.setFeeLimit(TRC20_FEE_LIMIT)
                builder.setMemo("")

                val signedTxn: Chain.Transaction = wrapper.signTransaction(builder.build())
                val bandwidthRequired = wrapper.estimateBandwidth(signedTxn)

                BandwidthEstimate(bandwidth = bandwidthRequired + BANDWIDTH_BUFFER)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════════

    private fun createTransferFunction(toAddress: String, amount: BigInteger): Function {
        return Function(
            "transfer",
            listOf<Type<*>>(Address(toAddress), Uint256(amount)),
            listOf<TypeReference<*>>(object : TypeReference<Bool?>() {}),
        )
    }

    private fun getEnergyFee(wrapper: ApiWrapper): BigInteger {
        for (value in wrapper.chainParameters.chainParameterList) {
            if (value.key == "getEnergyFee") {
                return value.value.toBigInteger()
            }
        }
        return BigInteger.ZERO
    }

    /**
     * Создаёт BlockId из Chain.Block.
     * BlockId = SHA256(blockHeader.rawData) с первыми 8 байтами заменёнными на номер блока (big-endian).
     */
    private fun createBlockIdFromBlock(block: Chain.Block): BlockId {
        val blockNumber = block.blockHeader.rawData.number

        // Вычисляем хэш заголовка блока
        val rawDataBytes = block.blockHeader.rawData.toByteArray()
        val hashBytes = MessageDigest.getInstance("SHA-256").digest(rawDataBytes)

        // Первые 8 байт хэша заменяем номером блока (big-endian)
        val blockIdBytes = hashBytes.copyOf()
        for (i in 0..7) {
            blockIdBytes[i] = (blockNumber shr (56 - i * 8) and 0xFF).toByte()
        }

        return BlockId(blockIdBytes, blockNumber)
    }

    private fun calculateTxId(transaction: Chain.Transaction): String {
        val rawDataBytes = transaction.rawData.toByteArray()
        val txidBytes = MessageDigest.getInstance("SHA-256").digest(rawDataBytes)
        return txidBytes.joinToString("") { "%02x".format(it) }
    }

    private fun safeClose(wrapper: ApiWrapper) {
        try {
            wrapper.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close ApiWrapper: $e")
        }
    }

    private companion object {
        const val TAG = "TransactionsImpl"
        const val USDT_CONTRACT_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        const val TRC20_FEE_LIMIT = 45_000_000L
        const val BANDWIDTH_BUFFER = 50L
        const val TRANSACTION_EXPIRE_OFFSET_MS = 30 * 60 * 1000L // 30 минут
    }
}
