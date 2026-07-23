package com.profpay.data.aml.api

import com.profpay.data.aml.dto.AmlPaymentResponseDto
import com.profpay.data.aml.dto.AmlReportDto
import com.profpay.data.aml.dto.CreateAmlPaymentRequest
import com.profpay.data.aml.dto.RenewAmlReportRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * API для работы с AML сервисом
 */
interface AmlApi {

    /**
     * Получить AML отчёт по транзакции
     */
    @GET("aml/reports/{tx}")
    suspend fun getAmlReport(
        @Path("tx") txHash: String,
        @Query("address") address: String,
        @Query("userId") userId: Long,
        @Query("tokenName") tokenName: String = "USDT",
    ): Response<AmlReportDto>

    /**
     * Обновить AML отчёт (запросить актуальные данные у провайдера)
     *
     * Ограничение: между запросами на обновление должно пройти не менее 24 часов.
     */
    @POST("aml/reports/{tx}/renew")
    suspend fun renewAmlReport(
        @Path("tx") txHash: String,
        @Body request: RenewAmlReportRequest,
    ): Response<AmlReportDto>

    /**
     * Создать платёж для AML проверки
     */
    @POST("aml/payments")
    suspend fun createAmlPayment(
        @Body request: CreateAmlPaymentRequest,
    ): Response<AmlPaymentResponseDto>

    /**
     * Скачать PDF отчёт AML
     */
    @Streaming
    @GET("aml/download")
    suspend fun downloadAmlPdf(
        @Query("user_id") userId: Long,
        @Query("tx_id") txHash: String,
    ): Response<ResponseBody>
}
