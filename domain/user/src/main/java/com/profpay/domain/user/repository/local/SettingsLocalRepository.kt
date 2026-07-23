package com.profpay.domain.user.repository.local

import com.profpay.domain.user.model.local.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий настроек пользователя.
 */
interface SettingsLocalRepository {

    /**
     * Создать настройки по умолчанию (если не существуют).
     */
    suspend fun createDefaultIfNotExists(languageCode: String = "ru")

    /**
     * Получить текущие настройки.
     * @return настройки или null, если не инициализированы
     */
    suspend fun get(): UserSettings?

    /**
     * Наблюдать за изменениями настроек.
     */
    fun observe(): Flow<UserSettings>

    /**
     * Получить код языка.
     */
    suspend fun getLanguageCode(): String

    /**
     * Обновить статус бота.
     */
    suspend fun updateBotActive(isActive: Boolean)

    /**
     * Обновить токен бота.
     */
    suspend fun updateBotToken(token: String)

    /**
     * Обновить настройку авто-AML.
     */
    suspend fun updateAutoAml(isEnabled: Boolean)

    /**
     * Проверить, существуют ли настройки.
     */
    suspend fun exists(): Boolean
}
