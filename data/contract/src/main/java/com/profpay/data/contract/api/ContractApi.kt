package com.profpay.data.contract.api

import com.profpay.data.contract.dto.request.CallContractRequest
import com.profpay.data.contract.dto.response.CallContractResponseDto
import com.profpay.data.contract.dto.request.CreateDealRequest
import com.profpay.data.contract.dto.response.CreateDealResponseDto
import com.profpay.data.contract.dto.request.DealStatusChangeRequestDto
import com.profpay.data.contract.dto.response.DealStatusChangeResponseDto
import com.profpay.data.contract.dto.request.DeployContractRequest
import com.profpay.data.contract.dto.response.DeployContractResponseDto
import com.profpay.data.contract.dto.request.DisputeActionRequestDto
import com.profpay.data.contract.dto.response.DisputeActionResponseDto
import com.profpay.data.contract.dto.response.UserDealsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContractApi {

    @GET("contracts/deals/user/{userId}")
    suspend fun getUserDeals(
        @Path("userId") userId: Long,
    ): Response<UserDealsResponseDto>

    /**
     * Деплой смарт-контракта
     */
    @POST("contracts/deploy")
    suspend fun deployContract(
        @Body request: DeployContractRequest,
    ): Response<DeployContractResponseDto>

    /**
     * Создать сделку
     */
    @POST("contracts/deals")
    suspend fun createDeal(
        @Body request: CreateDealRequest,
    ): Response<CreateDealResponseDto>

    /**
     * Вызов метода контракта (подтверждение, отмена, диспут и т.д.)
     */
    @POST("contracts/call")
    suspend fun callContract(
        @Body request: CallContractRequest,
    ): Response<CallContractResponseDto>

    @POST("contracts/disputes/action")
    suspend fun processDisputeAction(
        @Body request: DisputeActionRequestDto,
    ): Response<DisputeActionResponseDto>

    @POST("contracts/deals/status-change")
    suspend fun processDealStatusChange(
        @Body request: DealStatusChangeRequestDto,
    ): Response<DealStatusChangeResponseDto>
}
