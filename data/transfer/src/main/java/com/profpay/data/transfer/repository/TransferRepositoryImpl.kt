package com.profpay.data.transfer.repository

import com.profpay.core.network.error.NetworkError
import com.profpay.core.network.error.safeApiCall
import com.profpay.data.transfer.api.TransferApi
import com.profpay.data.transfer.dto.CreateTransferResponseDto
import com.profpay.data.transfer.dto.EstimateCommissionResponseDto
import com.profpay.data.transfer.mapper.toDomain
import com.profpay.data.transfer.mapper.toDto
import com.profpay.domain.transfer.exception.TransferError
import com.profpay.domain.transfer.model.CreateTransferParams
import com.profpay.domain.transfer.model.EstimateCommissionParams
import com.profpay.domain.transfer.model.EstimateCommissionResult
import com.profpay.domain.transfer.model.TransferResult
import com.profpay.domain.transfer.repository.TransferRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransferRepositoryImpl @Inject constructor(
    private val transferApi: TransferApi,
    private val json: Json,
) : TransferRepository {

    override suspend fun createTransfer(params: CreateTransferParams): Result<TransferResult> {
        val apiResult: Result<CreateTransferResponseDto> = safeApiCall(json) {
            transferApi.createTransfer(params.toDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toTransferError() }
    }

    override suspend fun estimateCommission(
        params: EstimateCommissionParams,
    ): Result<EstimateCommissionResult> {
        val apiResult: Result<EstimateCommissionResponseDto> = safeApiCall(json) {
            transferApi.estimateCommission(params.toDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toEstimateError(params.userId) }
    }

    private fun Throwable.toTransferError(): Throwable = when (this) {
        is NetworkError.HttpError -> when (code) {
            400 -> TransferError.ValidationError("request", errorBody ?: "Invalid request")
            402 -> TransferError.InsufficientFunds("unknown", "unknown")
            503 -> TransferError.ResourceServiceUnavailable(this)
            in 500..599 -> TransferError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toEstimateError(userId: Long): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            code == 400 -> TransferError.ValidationError("request", errorBody ?: "Invalid request")
            isNotFound -> TransferError.UserNotFound(userId)
            isServerError -> TransferError.ServerError(this)
            else -> this
        }
        else -> this
    }
}
