package com.profpay.data.wallet.api

import com.profpay.data.wallet.dto.AddWalletRequestDto
import com.profpay.data.wallet.dto.CentralAddressResponseDto
import com.profpay.data.wallet.dto.SetCentralAddressRequestDto
import com.profpay.data.wallet.dto.UpdateDerivedIndexRequest
import com.profpay.data.wallet.dto.UpdateDerivedIndexResponseDto
import com.profpay.data.wallet.dto.WalletDataResponseDto
import com.profpay.data.wallet.dto.WalletResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface WalletApi {

    /**
     * Добавить дополнительный кошелёк к существующему пользователю.
     * Требует авторизации кошельком.
     */
    @POST("wallet/add")
    suspend fun addWallet(
        @Body request: AddWalletRequestDto,
    ): Response<WalletResponseDto>

    /**
     * Обновить derived index SOT-адреса
     */
    @POST("wallet/update-derived-index")
    suspend fun updateDerivedIndex(
        @Body request: UpdateDerivedIndexRequest,
    ): Response<UpdateDerivedIndexResponseDto>

    /**
     * Получить данные кошелька по адресу
     */
    @GET("wallet/wallet-data")
    suspend fun getWalletData(
        @Query("address") address: String,
    ): Response<WalletDataResponseDto>

    /**
     * Установить центральный адрес
     */
    @PUT("wallet/central-address")
    suspend fun setCentralAddress(
        @Body request: SetCentralAddressRequestDto,
    ): Response<CentralAddressResponseDto>
}
