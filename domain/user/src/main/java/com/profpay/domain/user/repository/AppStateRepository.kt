package com.profpay.domain.user.repository

import com.profpay.domain.user.model.AppState
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для получения состояния приложения.
 * Single Source of Truth — данные из Room.
 */
interface AppStateRepository {

    /**
     * Получить текущее состояние приложения.
     * Suspend функция для one-shot запроса.
     */
    suspend fun getAppState(): AppState

    /**
     * Наблюдать за состоянием приложения.
     * Flow для реактивных подписок.
     */
    fun observeAppState(): Flow<AppState>
}
