// core/tron/src/main/java/com/profpay/core/tron/api/TronSmartContractApi.kt
package com.profpay.core.tron.api

import com.google.protobuf.ByteString
import com.profpay.core.tron.model.CreateDealParams
import org.tron.trident.abi.datatypes.Type
import java.math.BigInteger

interface TronSmartContractApi {
    val multiSigRead: MultiSigReadApi
    val multiSigWrite: MultiSigWriteApi

    fun deploy(ownerAddress: String, privateKey: String): String?
    fun getSignedDeployTransaction(ownerAddress: String, privateKey: String): ByteString?
    suspend fun estimateDeployingContract(privateKey: String): Pair<Long, Long>
}

interface MultiSigReadApi {
    fun getUsdt(ownerAddress: String, privateKey: String, contractAddress: String): String
    fun getContractStats(ownerAddress: String, privateKey: String, contractAddress: String): Pair<String, String>
}

interface MultiSigWriteApi {
    fun createDeal(ownerAddress: String, contractAddress: String, privateKey: String, params: CreateDealParams): ByteString
    fun depositDeal(id: Long, ownerAddress: String, privateKey: String, contractAddress: String): ByteString
    fun approve(ownerAddress: String, privateKey: String, contractAddress: String): ByteString
    fun voteDeal(id: Long, ownerAddress: String, privateKey: String, contractAddress: String): ByteString
    fun cancelDeal(id: Long, ownerAddress: String, privateKey: String, contractAddress: String): ByteString
    fun executeDisputed(id: Long, ownerAddress: String, privateKey: String, contractAddress: String): ByteString
    fun paySellerExpertFee(id: Long, ownerAddress: String, privateKey: String, contractAddress: String): ByteString
    fun assignDecisionAdminAndSetAmounts(id: Long, ownerAddress: String, privateKey: String, contractAddress: String, sellerValue: BigInteger, buyerValue: BigInteger): ByteString
    fun voteOnDisputeResolution(id: Long, ownerAddress: String, privateKey: String, contractAddress: String): ByteString
    fun declineDisputeResolution(id: Long, ownerAddress: String, privateKey: String, contractAddress: String): ByteString
}
