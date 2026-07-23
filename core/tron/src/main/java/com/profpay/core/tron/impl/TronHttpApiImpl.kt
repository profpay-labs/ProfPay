// core/tron/src/main/java/com/profpay/core/tron/impl/TronHttpApiImpl.kt
package com.profpay.core.tron.impl

import com.profpay.core.tron.api.TronAddressApi
import com.profpay.core.tron.api.TronHttpApi
import com.profpay.core.tron.model.Trc20TransactionData
import com.profpay.core.tron.model.Trc20TransactionResponse
import com.profpay.core.tron.model.TransactionStatus
import com.profpay.core.tron.model.TransactionType
import com.profpay.core.tron.model.TronTransaction
import com.profpay.core.tron.model.TrxTransactionData
import com.profpay.core.tron.model.TrxTransactionResponse
import com.profpay.core.tron.network.TronGridClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TronHttpApiImpl @Inject constructor(
    private val client: TronGridClient,
    private val addressApi: TronAddressApi,
) : TronHttpApi {

    override suspend fun getTrxTransactions(
        address: String,
        limit: Int,
    ): Result<List<TrxTransactionData>> = runCatching {
        val response = client.get<TrxTransactionResponse> {
            addPathSegments("v1/accounts/$address/transactions")
            addQueryParameter("limit", limit.coerceIn(1, 200).toString())
        }

        if (!response.success) {
            throw IllegalStateException("TronGrid returned success=false")
        }

        response.data
    }

    override suspend fun getTrc20Transactions(
        address: String,
        contractAddress: String?,
        limit: Int,
    ): Result<List<Trc20TransactionData>> = runCatching {
        val response = client.get<Trc20TransactionResponse> {
            addPathSegments("v1/accounts/$address/transactions/trc20")
            addQueryParameter("limit", limit.coerceIn(1, 200).toString())
            contractAddress?.let { addQueryParameter("contract_address", it) }
        }

        if (!response.success) {
            throw IllegalStateException("TronGrid returned success=false for TRC20")
        }

        response.data
    }

    override suspend fun getAllTransactions(
        address: String,
        limit: Int,
    ): Result<List<TronTransaction>> = runCatching {
        val trxResult = getTrxTransactions(address, limit)
        val trc20Result = getTrc20Transactions(address, null, limit)

        val trxTransactions = trxResult.getOrDefault(emptyList()).mapNotNull { tx ->
            mapTrxToTronTransaction(tx)
        }

        val trc20Transactions = trc20Result.getOrDefault(emptyList()).map { tx ->
            mapTrc20ToTronTransaction(tx)
        }

        (trxTransactions + trc20Transactions)
            .sortedByDescending { it.blockTimestamp }
    }

    private fun mapTrxToTronTransaction(tx: TrxTransactionData): TronTransaction? {
        val contract = tx.rawData?.contract?.firstOrNull() ?: return null
        val value = contract.parameter?.value ?: return null

        val fromAddress = value.ownerAddress.takeIf { it.isNotEmpty() }
            ?.let { runCatching { addressApi.hexToBase58CheckAddress(it) }.getOrNull() }
            ?: return null

        val toAddress = value.toAddress.takeIf { it.isNotEmpty() }
            ?.let { runCatching { addressApi.hexToBase58CheckAddress(it) }.getOrNull() }
            ?: return null

        val status = when (tx.ret.firstOrNull()?.contractRet) {
            "SUCCESS" -> TransactionStatus.SUCCESS
            "REVERT", "OUT_OF_ENERGY", "OUT_OF_TIME" -> TransactionStatus.FAILED
            else -> TransactionStatus.PENDING
        }

        val type = when (contract.type) {
            "TransferContract" -> TransactionType.TRANSFER
            "TriggerSmartContract" -> TransactionType.CONTRACT_CALL
            "FreezeBalanceV2Contract" -> TransactionType.STAKE
            "UnfreezeBalanceV2Contract" -> TransactionType.UNSTAKE
            else -> TransactionType.OTHER
        }

        return TronTransaction(
            txId = tx.txId,
            fromAddress = fromAddress,
            toAddress = toAddress,
            amount = value.amount,
            tokenSymbol = "TRX",
            tokenDecimals = 6,
            blockTimestamp = tx.blockTimestamp,
            status = status,
            fee = tx.ret.firstOrNull()?.fee ?: 0,
            type = type,
        )
    }

    private fun mapTrc20ToTronTransaction(tx: Trc20TransactionData): TronTransaction {
        return TronTransaction(
            txId = tx.transactionId,
            fromAddress = tx.from,
            toAddress = tx.to,
            amount = tx.value.toLongOrNull() ?: 0L,
            tokenSymbol = tx.tokenInfo?.symbol ?: "TRC20",
            tokenDecimals = tx.tokenInfo?.decimals ?: 6,
            blockTimestamp = tx.blockTimestamp,
            status = TransactionStatus.SUCCESS,
            fee = 0,
            type = TransactionType.TRC20_TRANSFER,
        )
    }
}
