package com.profpay.domain.wallet.exception

sealed class WalletError : Exception() {

    /**
     * Некорректные данные для создания кошелька
     */
    data class InvalidWalletData(
        val reason: String,
    ) : WalletError() {
        override val message: String = "Invalid wallet data: $reason"
    }

    /**
     * Адрес уже существует
     */
    data class AddressAlreadyExists(
        val address: String,
    ) : WalletError() {
        override val message: String = "Address already exists: $address"
    }

    /**
     * Ошибка добавления кошелька
     */
    data class AddWalletFailed(
        override val cause: Throwable? = null,
    ) : WalletError() {
        override val message: String = "Failed to add wallet"
    }

    /**
     * Ошибка сервера
     */
    data class ServerError(
        override val cause: Throwable? = null,
    ) : WalletError() {
        override val message: String = "Server error while creating wallet"
    }

    /**
     * SOT адрес не найден
     */
    data class SotAddressNotFound(
        val address: String,
    ) : WalletError() {
        override val message: String = "SOT address not found: $address"
    }

    /**
     * Некорректный derivation index
     */
    data class InvalidDerivationIndex(
        val index: Int,
        val reason: String,
    ) : WalletError() {
        override val message: String = "Invalid derivation index $index: $reason"
    }

    /**
     * Адрес не найден
     */
    data class AddressNotFound(
        val address: String,
    ) : WalletError() {
        override val message: String = "Address not found: $address"
    }

    /**
     * Ошибка аутентификации — невалидная подпись
     */
    data class Unauthorized(
        override val cause: Throwable? = null,
    ) : WalletError() {
        override val message: String = "Unauthorized: invalid or missing signature"
    }

    /**
     * Невалидные данные центрального адреса
     */
    data class InvalidCentralAddressData(
        val details: String,
    ) : WalletError() {
        override val message: String = "Invalid central address data: $details"
    }

    data class CentralAddressServerError(
        override val message: String,
        override val cause: Throwable? = null,
    ) : WalletError()

    data class CentralAddressClientError(
        override val message: String,
        override val cause: Throwable? = null,
    ) : WalletError()
}
