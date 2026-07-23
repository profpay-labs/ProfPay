package com.profpay.data.user.api

import com.profpay.data.user.dto.OnboardUserRequestDto
import com.profpay.data.user.dto.OnboardUserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Публичные API endpoints для пользователей.
 * Не требуют авторизации кошельком.
 */
interface PublicUserApi {

    /**
     * Онбординг пользователя с созданием кошелька.
     * Полный процесс регистрации при первом запуске приложения:
     * 1. Создание пользователя
     * 2. Регистрация устройства
     * 3. Принятие соглашения
     * 4. Создание кошелька с адресами
     */
    @POST("users/onboard")
    suspend fun onboardUser(
        @Body request: OnboardUserRequestDto,
    ): Response<OnboardUserResponseDto>
}
