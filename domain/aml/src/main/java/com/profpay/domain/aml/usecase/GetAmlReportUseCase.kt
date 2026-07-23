package com.profpay.domain.aml.usecase

import com.profpay.domain.aml.model.AmlReport
import com.profpay.domain.aml.repository.AmlRepository
import javax.inject.Inject

class GetAmlReportUseCase @Inject constructor(
    private val amlRepository: AmlRepository,
) {
    suspend operator fun invoke(
        userId: Long,
        address: String,
        txHash: String,
        tokenName: String = "USDT",
    ): Result<AmlReport> = amlRepository.getAmlReport(
        txHash = txHash,
        address = address,
        userId = userId,
        tokenName = tokenName,
    )
}
