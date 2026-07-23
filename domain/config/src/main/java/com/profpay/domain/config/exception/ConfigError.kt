package com.profpay.domain.config.exception

sealed class ConfigError : Exception() {

    data class FetchFailed(override val cause: Throwable?) : ConfigError() {
        override val message: String = "Failed to fetch fee configuration"
    }

    data class ServerError(override val cause: Throwable?) : ConfigError() {
        override val message: String = "Server error while fetching configuration"
    }
}
