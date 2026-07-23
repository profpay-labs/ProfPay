package com.profpay.core.tron.impl

import android.util.Log
import com.profpay.core.tron.api.TronAccountsApi
import com.profpay.core.tron.network.TronNodeManager
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.contract.Trc20Contract
import org.tron.trident.core.key.KeyPair
import org.tron.trident.proto.Response
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountsImpl @Inject constructor() : TronAccountsApi {

    override fun getAccountResource(
        ownerAddress: String,
        privateKey: String,
    ): Response.AccountResourceMessage {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)
            try {
                wrapper.getAccountResource(ownerAddress)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun getAccount(
        ownerAddress: String,
        privateKey: String,
    ): Response.Account {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)
            try {
                wrapper.getAccount(ownerAddress)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun allowance(
        spender: String,
        ownerAddress: String,
        privateKey: String,
    ): BigInteger? {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)
            try {
                val contract: Contract = wrapper.getContract(USDT_CONTRACT_ADDRESS)
                val token = Trc20Contract(contract, ownerAddress, wrapper)
                token.allowance(ownerAddress, spender)
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun isAllowanceUnlimited(
        spender: String,
        ownerAddress: String,
        privateKey: String,
    ): Boolean {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, privateKey)
            try {
                val contract: Contract = wrapper.getContract(USDT_CONTRACT_ADDRESS)
                val token = Trc20Contract(contract, ownerAddress, wrapper)
                val result = token.allowance(ownerAddress, spender)

                val max = BigInteger.valueOf(Long.MAX_VALUE)
                    .multiply(BigInteger.valueOf(10L).pow(USDT_DECIMALS))
                result >= max
            } finally {
                safeClose(wrapper)
            }
        }
    }

    override fun hasEnoughBandwidth(
        address: String,
        requiredBandwidth: Long,
    ): Boolean {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(
                node.grpc,
                node.solidityGrpc,
                KeyPair.generate().toPrivateKey()
            )

            try {
                val resources = wrapper.getAccountResource(address)

                val freeNetRemaining = resources.freeNetLimit - resources.freeNetUsed
                val paidNetRemaining = resources.netLimit - resources.netUsed
                val totalAvailableBandwidth = freeNetRemaining + paidNetRemaining

                totalAvailableBandwidth >= requiredBandwidth
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
        const val TAG = "AccountsImpl"
        const val USDT_CONTRACT_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        const val USDT_DECIMALS = 6
    }
}
