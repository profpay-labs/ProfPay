package com.profpay.data.transfer.api

import com.profpay.data.transfer.dto.CreateTransferRequest
import com.profpay.data.transfer.dto.CreateTransferResponseDto
import com.profpay.data.transfer.dto.EstimateCommissionRequest
import com.profpay.data.transfer.dto.EstimateCommissionResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TransferApi {

    /**
     * Создать перевод криптовалюты
     */
    @POST("transfers")
    suspend fun createTransfer(
        @Body request: CreateTransferRequest,
    ): Response<CreateTransferResponseDto>

    /**
     * Оценить комиссию для транзакции
     */
    @POST("transfers/estimate-commission")
    suspend fun estimateCommission(
        @Body request: EstimateCommissionRequest,
    ): Response<EstimateCommissionResponseDto>
}
