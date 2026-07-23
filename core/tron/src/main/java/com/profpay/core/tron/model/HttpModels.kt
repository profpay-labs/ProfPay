// core/tron/src/main/java/com/profpay/core/tron/model/HttpModels.kt
package com.profpay.core.tron.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════════════════
// TRX Transactions
// ══════════════════════════════════════════════════════════════════════

@Serializable
data class TrxTransactionResponse(
    val data: List<TrxTransactionData> = emptyList(),
    val success: Boolean = false,
    val meta: TrxTransactionMeta? = null,
)

@Serializable
data class TrxTransactionData(
    val ret: List<TrxTransactionRet> = emptyList(),
    @SerialName("txID") val txId: String = "",
    @SerialName("block_timestamp") val blockTimestamp: Long = 0,
    @SerialName("raw_data") val rawData: TrxRawData? = null,
    @SerialName("raw_data_hex") val rawDataHex: String = "",
)

@Serializable
data class TrxTransactionRet(
    val contractRet: String = "",
    val fee: Long = 0,
)

@Serializable
data class TrxRawData(
    val contract: List<TrxContract> = emptyList(),
    @SerialName("ref_block_bytes") val refBlockBytes: String = "",
    @SerialName("ref_block_hash") val refBlockHash: String = "",
    val expiration: Long = 0,
    val timestamp: Long = 0,
)

@Serializable
data class TrxContract(
    val parameter: TrxContractParameter? = null,
    val type: String = "",
)

@Serializable
data class TrxContractParameter(
    val value: TrxContractValue? = null,
    @SerialName("type_url") val typeUrl: String = "",
)

@Serializable
data class TrxContractValue(
    val amount: Long = 0,
    @SerialName("owner_address") val ownerAddress: String = "",
    @SerialName("to_address") val toAddress: String = "",
    @SerialName("contract_address") val contractAddress: String? = null,
    val data: String? = null,
)

@Serializable
data class TrxTransactionMeta(
    @SerialName("page_size") val pageSize: Int = 0,
    val fingerprint: String = "",
)

// ══════════════════════════════════════════════════════════════════════
// TRC20 Transactions
// ══════════════════════════════════════════════════════════════════════

@Serializable
data class Trc20TransactionResponse(
    val data: List<Trc20TransactionData> = emptyList(),
    val success: Boolean = false,
    val meta: Trc20TransactionMeta? = null,
)

@Serializable
data class Trc20TransactionData(
    @SerialName("transaction_id") val transactionId: String = "",
    @SerialName("token_info") val tokenInfo: Trc20TokenInfo? = null,
    @SerialName("block_timestamp") val blockTimestamp: Long = 0,
    val from: String = "",
    val to: String = "",
    val type: String = "",
    val value: String = "0",
)

@Serializable
data class Trc20TokenInfo(
    val symbol: String = "",
    val address: String = "",
    val decimals: Int = 6,
    val name: String = "",
)

@Serializable
data class Trc20TransactionMeta(
    @SerialName("page_size") val pageSize: Int = 0,
    val fingerprint: String? = null,
)

// ══════════════════════════════════════════════════════════════════════
// Domain Models (для возврата из API)
// ══════════════════════════════════════════════════════════════════════

/**
 * Унифицированная модель транзакции для использования в domain/presentation слоях.
 */
data class TronTransaction(
    val txId: String,
    val fromAddress: String,
    val toAddress: String,
    val amount: Long,
    val tokenSymbol: String, // "TRX" или "USDT" и т.д.
    val tokenDecimals: Int,
    val blockTimestamp: Long,
    val status: TransactionStatus,
    val fee: Long,
    val type: TransactionType,
)

enum class TransactionStatus {
    SUCCESS,
    FAILED,
    PENDING,
}

enum class TransactionType {
    TRANSFER,
    TRC20_TRANSFER,
    CONTRACT_CALL,
    STAKE,
    UNSTAKE,
    OTHER,
}
