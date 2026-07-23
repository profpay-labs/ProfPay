package com.profpay.data.wallet.api

import com.profpay.data.wallet.dto.CentralAddressResponseDto
import com.profpay.data.wallet.dto.CreateWalletRequest
import com.profpay.data.wallet.dto.SetCentralAddressRequestDto
import com.profpay.data.wallet.dto.WalletResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Публичные API endpoints для кошелька.
 * Не требуют авторизации кошельком (вызываются до создания кошелька).
 */
interface PublicWalletApi {

    /**
     * Создать кошелёк
     */
    @POST("wallet")
    suspend fun createWallet(
        @Body request: CreateWalletRequest,
    ): Response<WalletResponseDto>
}
