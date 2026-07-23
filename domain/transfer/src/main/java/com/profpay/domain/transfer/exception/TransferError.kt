package com.profpay.domain.transfer.exception

sealed class TransferError : Exception() {

    data object TokenNotFound : TransferError() {
        override val message: String = "Token not found"
    }

    data object SenderAddressNotFound : TransferError() {
        override val message: String = "Sender address not found"
    }

    data object AddressNotActivated : TransferError() {
        override val message: String = "Address is not activated"
    }

    data class InsufficientFeeBalance(
        val feeAddress: String,
        val required: String,
        val available: String,
    ) : TransferError() {
        override val message: String =
            "Insufficient fee balance on $feeAddress. Required: $required, available: $available"
    }

    data class AmountExceedsBalance(
        val requested: String,
        val available: String,
    ) : TransferError() {
        override val message: String =
            "Amount exceeds available balance. Requested: $requested, available: $available"
    }

    data class InsufficientBalanceWithCommission(
        val remaining: String,
    ) : TransferError() {
        override val message: String =
            "Insufficient balance after commission. Remaining would be: $remaining"
    }

    data object InvalidCommission : TransferError() {
        override val message: String = "Commission must be greater than zero"
    }

    data object FeeAddressUnavailable : TransferError() {
        override val message: String = "Failed to retrieve fee address from configuration"
    }

    /**
     * Некорректные данные запроса
     */
    data class ValidationError(
        val field: String,
        val reason: String,
    ) : TransferError() {
        override val message: String = "Validation error for field '$field': $reason"
    }

    /**
     * Недостаточно средств для покупки ресурсов
     */
    data class InsufficientFunds(
        val required: String,
        val available: String,
    ) : TransferError() {
        override val message: String = "Insufficient funds: required $required, available $available"
    }

    /**
     * Сервис покупки ресурсов недоступен
     */
    data class ResourceServiceUnavailable(
        override val cause: Throwable? = null,
    ) : TransferError() {
        override val message: String = "Resource purchase service is temporarily unavailable"
    }

    /**
     * Внутренняя ошибка сервера
     */
    data class ServerError(
        override val cause: Throwable? = null,
    ) : TransferError() {
        override val message: String = "Internal server error"
    }

    data class NetworkError(
        override val cause: Throwable? = null,
    ) : TransferError() {
        override val message: String = "Network error during transfer"
    }

    /**
     * Пользователь не найден
     */
    data class UserNotFound(
        val userId: Long,
    ) : TransferError() {
        override val message: String = "User not found: $userId"
    }

    data class Unknown(
        override val cause: Throwable? = null,
    ) : TransferError() {
        override val message: String = cause?.message ?: "Unknown transfer error"
    }

}
