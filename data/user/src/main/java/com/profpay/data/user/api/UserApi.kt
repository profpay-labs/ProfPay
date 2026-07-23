package com.profpay.data.user.api

import com.profpay.data.user.dto.CheckPermissionsRequestDto
import com.profpay.data.user.dto.CheckPermissionsResponseDto
import com.profpay.data.user.dto.RegisterDeviceRequestDto
import com.profpay.data.user.dto.RegisterDeviceResponseDto
import com.profpay.data.user.dto.TelegramDataResponseDto
import com.profpay.data.user.dto.UpdateTelegramRequestDto
import com.profpay.data.user.dto.UpdateTelegramResponseDto
import com.profpay.data.user.dto.UserExistsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    /**
     * Проверить разрешения пользователя
     */
    @POST("users/permissions/check")
    suspend fun checkPermissions(
        @Body request: CheckPermissionsRequestDto,
    ): Response<CheckPermissionsResponseDto>

    /**
     * Регистрация устройства
     */
    @POST("users/devices")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequestDto,
    ): Response<RegisterDeviceResponseDto>

    /**
     * Проверить существование пользователя
     */
    @GET("users/{userId}/exists")
    suspend fun checkUserExists(
        @Path("userId") userId: Long,
    ): Response<UserExistsResponseDto>

    /**
     * Получить Telegram данные по appId
     */
    @GET("users/telegram")
    suspend fun getTelegramData(
        @Query("appId") appId: String,
    ): Response<TelegramDataResponseDto>

    /**
     * Обновить Telegram данные
     */
    @PUT("users/{userId}/telegram")
    suspend fun updateTelegram(
        @Path("userId") userId: Long,
        @Body request: UpdateTelegramRequestDto,
    ): Response<UpdateTelegramResponseDto>
}
