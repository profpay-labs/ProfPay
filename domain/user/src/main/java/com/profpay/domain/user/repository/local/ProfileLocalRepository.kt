package com.profpay.domain.user.repository.local

import com.profpay.domain.user.model.local.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий профиля пользователя.
 */
interface ProfileLocalRepository {

    // ══════════════════════════════════════════════════════════════════════
    // Основные идентификаторы (часто используются в других модулях)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Получить userId текущего профиля.
     * @throws NoSuchElementException если профиль не существует
     */
    suspend fun getUserId(): Long

    /**
     * Получить appId текущего профиля.
     * @throws NoSuchElementException если профиль не существует
     */
    suspend fun getAppId(): String

    // ══════════════════════════════════════════════════════════════════════
    // CRUD операции
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Создать новый профиль.
     */
    suspend fun create(profile: UserProfile)

    /**
     * Проверить, существует ли профиль.
     */
    suspend fun exists(): Boolean

    /**
     * Удалить профиль по Telegram ID.
     */
    suspend fun deleteByTelegramId(telegramId: Long)

    // ══════════════════════════════════════════════════════════════════════
    // Telegram интеграция
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Получить Telegram ID.
     */
    suspend fun getTelegramId(): Long?

    /**
     * Наблюдать за изменениями Telegram ID.
     */
    fun observeTelegramId(): Flow<Long?>

    /**
     * Наблюдать за изменениями Telegram username.
     */
    fun observeTelegramUsername(): Flow<String?>

    /**
     * Обновить данные Telegram.
     */
    suspend fun updateTelegram(telegramId: Long, username: String)

    /**
     * Обновить статус активации Telegram.
     */
    suspend fun updateTelegramActivation(
        isActive: Boolean,
        telegramId: Long,
        accessToken: String,
        expiresAt: Long,
    )

    // ══════════════════════════════════════════════════════════════════════
    // Device & Server sync
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Получить device token для push-уведомлений.
     */
    suspend fun getDeviceToken(): String?

    /**
     * Обновить device token.
     */
    suspend fun updateDeviceToken(deviceToken: String)

    /**
     * Обновить userId (после регистрации на сервере).
     */
    suspend fun updateUserId(userId: Long)
}
