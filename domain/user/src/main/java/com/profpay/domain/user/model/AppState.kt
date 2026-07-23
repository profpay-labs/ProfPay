package com.profpay.domain.user.model

/**
 * Состояние приложения относительно регистрации пользователя.
 * Single Source of Truth — определяется по данным в Room.
 */
sealed class AppState {

    /**
     * Первый запуск — пользователь не зарегистрирован.
     * Нет профиля в базе данных.
     */
    data object NotRegistered : AppState()

    /**
     * Пользователь зарегистрирован.
     * Есть профиль и минимум один кошелёк.
     */
    data class Registered(
        val userId: Long,
        val appId: String,
        val walletsCount: Int,
    ) : AppState() {

        /**
         * Есть ли у пользователя кошельки.
         */
        val hasWallets: Boolean
            get() = walletsCount > 0
    }

    /**
     * Проверка: зарегистрирован ли пользователь.
     */
    val isRegistered: Boolean
        get() = this is Registered
}
