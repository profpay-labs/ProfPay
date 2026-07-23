package com.profpay.core.tron.api

import com.profpay.core.tron.model.BandwidthEstimate
import com.profpay.core.tron.model.EnergyEstimate
import com.profpay.core.tron.model.SignedTransactionData
import org.tron.trident.abi.datatypes.Function
import java.math.BigInteger

interface TronTransactionsApi {
    // Transfers
    fun trc20Transfer(fromAddress: String, toAddress: String, privateKey: ByteArray, amount: Long): String
    fun trxTransfer(fromAddress: String, toAddress: String, privateKey: String, amount: Long): String

    // Signed transactions (for server broadcast)
    fun getSignedTrxTransaction(fromAddress: String, toAddress: String, privateKey: ByteArray, amount: BigInteger): SignedTransactionData
    fun getSignedUsdtTransaction(fromAddress: String, toAddress: String, privateKey: ByteArray, amount: BigInteger): SignedTransactionData

    // Energy estimation
    fun estimateEnergy(fromAddress: String, toAddress: String, privateKey: ByteArray, amount: BigInteger): EnergyEstimate
    fun estimateEnergy(function: Function, contractAddress: String, address: String, privateKey: ByteArray): EnergyEstimate

    // Bandwidth estimation
    fun estimateBandwidth(fromAddress: String, toAddress: String, privateKey: ByteArray, amount: BigInteger): BandwidthEstimate
    fun estimateBandwidthTrx(fromAddress: String, toAddress: String, privateKey: ByteArray, amount: BigInteger): BandwidthEstimate
    fun estimateBandwidth(function: Function, contractAddress: String, address: String, privateKey: ByteArray): BandwidthEstimate
}
