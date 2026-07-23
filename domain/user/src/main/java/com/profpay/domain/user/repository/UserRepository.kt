package com.profpay.domain.user.repository

import com.profpay.domain.user.model.OnboardUserResult
import com.profpay.domain.user.model.RegisterDeviceResult
import com.profpay.domain.user.model.TelegramDataResult
import com.profpay.domain.user.model.UpdateTelegramResult
import com.profpay.domain.user.model.UserExistsResult
import com.profpay.domain.user.model.UserPermissionsResult
import com.profpay.domain.user.model.WalletOnboardingData

interface UserRepository {

    /**
     * Онбординг пользователя с созданием кошелька.
     *
     * @param deviceToken Токен устройства для push-уведомлений
     * @param appId Уникальный идентификатор установки приложения
     * @param consentAccepted Флаг принятия пользовательского соглашения
     * @param wallet Данные кошелька (адреса, публичные ключи)
     * @return Результат онбординга с userId, walletId и timestamp
     */
    suspend fun onboardUser(
        deviceToken: String,
        appId: String,
        consentAccepted: Boolean,
        wallet: WalletOnboardingData,
    ): Result<OnboardUserResult>

    /**
     * Проверить существование пользователя
     */
    suspend fun checkUserExists(userId: Long): Result<UserExistsResult>

    /**
     * Получить Telegram данные по appId
     */
    suspend fun getTelegramData(appId: String): Result<TelegramDataResult>

    /**
     * Проверка разрешений пользователя
     */
    suspend fun checkPermissions(
        appId: String,
        deviceToken: String,
    ): Result<UserPermissionsResult>

    /**
     * Регистрация нового устройства
     */
    suspend fun registerDevice(
        userId: Long,
        deviceToken: String,
        appId: String,
    ): Result<RegisterDeviceResult>

    /**
     * Обновить Telegram данные пользователя
     */
    suspend fun updateTelegram(
        userId: Long,
        username: String,
        telegramId: Long,
    ): Result<UpdateTelegramResult>
}
