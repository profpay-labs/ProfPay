package com.profpay.data.aml.repository

import com.profpay.core.network.exception.ApiException
import com.profpay.core.network.exception.NetworkError
import com.profpay.core.network.exception.safeApiCall
import com.profpay.data.aml.api.AmlApi
import com.profpay.data.aml.dto.AmlPaymentResponseDto
import com.profpay.data.aml.dto.AmlReportDto
import com.profpay.data.aml.dto.CreateAmlPaymentRequest
import com.profpay.data.aml.dto.RenewAmlReportRequest
import com.profpay.data.aml.mapper.toDomain
import com.profpay.domain.aml.exception.AmlError
import com.profpay.domain.aml.model.AmlPaymentResult
import com.profpay.domain.aml.model.AmlReport
import com.profpay.domain.aml.repository.AmlRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AmlRepositoryImpl @Inject constructor(
    private val amlApi: AmlApi,
    private val json: Json,
) : AmlRepository {

    override suspend fun getAmlReport(
        txHash: String,
        address: String,
        userId: Long,
        tokenName: String,
    ): Result<AmlReport> {
        val apiResult: Result<AmlReportDto> = safeApiCall(json) {
            amlApi.getAmlReport(
                txHash = txHash,
                address = address,
                userId = userId,
                tokenName = tokenName,
            )
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error ->
                throw error.toAmlError(txHash)
            }
    }

    override suspend fun renewAmlReport(
        txHash: String,
        address: String,
        userId: Long,
        tokenName: String,
    ): Result<AmlReport> {
        val apiResult: Result<AmlReportDto> = safeApiCall(json) {
            amlApi.renewAmlReport(
                txHash = txHash,
                request = RenewAmlReportRequest(
                    address = address,
                    userId = userId,
                    tokenName = tokenName,
                ),
            )
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toAmlRenewError(txHash) }
    }

    override suspend fun createAmlPayment(
        userId: Long,
        txHash: String,
        address: String,
        paymentAddress: String,
        bandwidthRequired: Long,
        txnBytes: String,
    ): Result<AmlPaymentResult> {
        val apiResult: Result<AmlPaymentResponseDto> = safeApiCall(json) {
            amlApi.createAmlPayment(
                request = CreateAmlPaymentRequest(
                    userId = userId,
                    txHash = txHash,
                    address = address,
                    paymentAddress = paymentAddress,
                    bandwidthRequired = bandwidthRequired,
                    txnBytes = txnBytes,
                ),
            )
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toAmlPaymentError() }
    }

    override suspend fun downloadAmlPdf(
        userId: Long,
        txHash: String,
    ): Result<ByteArray> = runCatching {
        val response = amlApi.downloadAmlPdf(userId, txHash)

        if (!response.isSuccessful) {
            throw AmlError.ReportNotFound(txHash)
        }

        response.body()?.bytes()
            ?: throw AmlError.ReportNotFound(txHash)
    }.recoverCatching { e ->
        throw e.toAmlError(txHash)
    }

    private fun Throwable.toAmlError(txHash: String): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> AmlError.ReportNotFound(txHash)
            code == 503 -> AmlError.ProviderUnavailable(this)
            else -> this
        }
        is ApiException -> when {
            isNotFound -> AmlError.ReportNotFound(txHash)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toAmlRenewError(txHash: String): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> AmlError.ReportNotFound(txHash)
            isTooManyRequests -> AmlError.RenewCooldownNotExpired(txHash)
            code == 503 -> AmlError.ProviderUnavailable(this)
            else -> this
        }
        is ApiException -> when {
            isNotFound -> AmlError.ReportNotFound(txHash)
            isTooManyRequests -> AmlError.RenewCooldownNotExpired(txHash)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toAmlPaymentError(): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            code == 400 -> AmlError.InvalidPaymentRequest(errorBody ?: "Bad request")
            isServerError -> AmlError.ResourcePurchaseFailed(this)
            else -> this
        }
        else -> this
    }
}
