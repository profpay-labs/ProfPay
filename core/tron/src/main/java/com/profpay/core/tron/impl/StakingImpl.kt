// core/tron/src/main/java/com/profpay/core/tron/impl/StakingImpl.kt
package com.profpay.core.tron.impl

import android.util.Log
import com.profpay.core.tron.api.TronStakingApi
import com.profpay.core.tron.network.TronNodeManager
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.exceptions.IllegalException
import org.tron.trident.proto.Chain
import org.tron.trident.proto.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StakingImpl @Inject constructor() : TronStakingApi {

    override fun freezeTrxV2(
        value: Long,
        ownerAddress: String,
        privateKey: String,
    ): String? {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

            try {
                val txnExt: Response.TransactionExtention = wrapper.freezeBalanceV2(
                    ownerAddress,
                    value,
                    RESOURCE_BANDWIDTH, // 1 = BANDWIDTH
                )

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

    override fun unfreezeBalanceV2(
        value: Long,
        ownerAddress: String,
        privateKey: String,
    ): String? {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

            try {
                val txnExt: Response.TransactionExtention = wrapper.unfreezeBalanceV2(
                    ownerAddress,
                    value,
                    RESOURCE_BANDWIDTH,
                )

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

    override fun withdrawExpireUnfreeze(
        ownerAddress: String,
        privateKey: String,
    ): String? {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)

            try {
                val txnExt: Response.TransactionExtention = wrapper.withdrawExpireUnfreeze(ownerAddress)

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

    private fun safeClose(wrapper: ApiWrapper) {
        try {
            wrapper.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to close ApiWrapper: $e")
        }
    }

    private companion object {
        const val TAG = "StakingImpl"
        const val RESOURCE_BANDWIDTH = 1
    }
}
