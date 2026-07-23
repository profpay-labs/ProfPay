package com.profpay.domain.user.exception

sealed class UserError : Exception() {

    /**
     * Ошибка сервера
     */
    data class ServerError(
        override val cause: Throwable? = null,
    ) : UserError() {
        override val message: String = "Server error"
    }

    /**
     * Пользователь не найден по appId
     */
    data class UserNotFoundByAppId(
        val appId: String,
    ) : UserError() {
        override val message: String = "User not found for appId: $appId"
    }

    /**
     * Некорректные данные регистрации
     */
    data class InvalidRegistrationData(
        override val cause: Throwable? = null,
    ) : UserError() {
        override val message: String = "Invalid registration data"
    }

    /**
     * Устройство уже зарегистрировано
     */
    data class DeviceAlreadyRegistered(
        val deviceToken: String,
    ) : UserError() {
        override val message: String = "Device already registered: $deviceToken"
    }

    /**
     * Пользователь не найден по userId
     */
    data class UserNotFound(
        val userId: Long,
    ) : UserError() {
        override val message: String = "User not found: $userId"
    }

    /**
     * Не принято пользовательское соглашение при онбординге
     */
    data object ConsentNotAccepted : UserError() {
        private fun readResolve(): Any = ConsentNotAccepted
        override val message: String = "User consent not accepted"
    }

    /**
     * Ошибка онбординга
     */
    data class OnboardingFailed(
        override val cause: Throwable? = null,
    ) : UserError() {
        override val message: String = "Onboarding failed"
    }
}
