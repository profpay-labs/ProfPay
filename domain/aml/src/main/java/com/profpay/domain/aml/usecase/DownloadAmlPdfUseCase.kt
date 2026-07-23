package com.profpay.domain.aml.usecase

import com.profpay.domain.aml.repository.AmlRepository
import javax.inject.Inject

class DownloadAmlPdfUseCase @Inject constructor(
    private val amlRepository: AmlRepository,
) {
    suspend operator fun invoke(userId: Long, txHash: String): Result<ByteArray> =
        amlRepository.downloadAmlPdf(userId, txHash)
}
