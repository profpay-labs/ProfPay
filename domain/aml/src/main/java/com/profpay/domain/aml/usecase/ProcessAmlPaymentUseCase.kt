package com.profpay.domain.aml.usecase

import com.profpay.domain.aml.model.AmlPaymentResult
import com.profpay.domain.aml.repository.AmlRepository
import javax.inject.Inject

class ProcessAmlPaymentUseCase @Inject constructor(
    private val amlRepository: AmlRepository,
) {
    suspend operator fun invoke(params: Params): Result<AmlPaymentResult> =
        amlRepository.createAmlPayment(
            userId = params.userId,
            txHash = params.txHash,
            address = params.address,
            paymentAddress = params.paymentAddress,
            bandwidthRequired = params.bandwidthRequired,
            txnBytes = params.txnBytes,
        )

    data class Params(
        val userId: Long,
        val txHash: String,
        val address: String,
        val paymentAddress: String,
        val bandwidthRequired: Long,
        val txnBytes: String,
    )
}
