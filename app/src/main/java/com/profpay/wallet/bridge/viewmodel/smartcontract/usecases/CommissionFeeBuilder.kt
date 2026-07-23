package com.profpay.wallet.bridge.viewmodel.smartcontract.usecases

import com.google.protobuf.ByteString
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.domain.config.repository.ConfigRepository
import com.profpay.domain.contract.model.Deal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

class CommissionFeeBuilder
    @Inject
    constructor(
        private val addressLocalRepository: AddressLocalRepository,
        private val tron: Tron,
        @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
        private val configRepository: ConfigRepository
    ) {

        suspend fun build(
            commission: BigInteger,
            userId: Long,
            deal: Deal,
        ): CommissionFeeBuilderResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.walletAddress
                } else {
                    deal.seller.walletAddress
                }

            val addressData =
                withContext(dispatcher) {
                    addressLocalRepository.getByAddress(address)
                } ?: return CommissionFeeBuilderResult.Error("Address data is null")

            val result = configRepository.getFeeConfiguration()
            val trxFeeAddress = result.fold(
                onSuccess = {
                    it.trxFeeAddress
                },
                onFailure = {
                    Sentry.captureException(it)
                    throw RuntimeException(it)
                }
            )

            val signedTxnBytesCommission =
                withContext(dispatcher) {
                    tron.transactions.getSignedTrxTransaction(
                        fromAddress = addressData.address,
                        toAddress = trxFeeAddress,
                        privateKey = "addressData.privateKey".toByteArray(),
                        amount = commission,
                    )
                }

            val estimateCommissionBandwidth =
                withContext(dispatcher) {
                    tron.transactions.estimateBandwidthTrx(
                        fromAddress = addressData.address,
                        toAddress = trxFeeAddress,
                        privateKey = "addressData.privateKey".toByteArray(),
                        amount = commission,
                    )
                }
            return CommissionFeeBuilderResult.Success(
                executorAddress = address,
                requiredBandwidth = estimateCommissionBandwidth.bandwidth,
                transaction = signedTxnBytesCommission.signedTxn!!,
            )
        }
    }

sealed class CommissionFeeBuilderResult(
    open val executorAddress: String? = null,
    open val requiredBandwidth: Long? = null,
    open val transaction: ByteString? = null,
    open val errorMessage: String? = null,
) {
    data class Success(
        override val executorAddress: String,
        override val requiredBandwidth: Long,
        override val transaction: ByteString,
    ) : CommissionFeeBuilderResult(executorAddress, requiredBandwidth, transaction)

    data class Error(
        override val errorMessage: String,
    ) : CommissionFeeBuilderResult(errorMessage = errorMessage)
}
