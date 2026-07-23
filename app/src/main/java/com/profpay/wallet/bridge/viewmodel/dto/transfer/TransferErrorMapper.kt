package com.profpay.wallet.bridge.viewmodel.dto.transfer

import com.profpay.domain.transfer.exception.TransferError

/**
 * Маппинг domain-ошибок трансфера в UI-текст.
 * UI-тексты живут в presentation-слое, НЕ в domain.
 */
object TransferErrorMapper {

    fun toUserMessage(error: Throwable): String = when (error) {
        is TransferError.TokenNotFound -> "Токен не найден"
        is TransferError.SenderAddressNotFound -> "Адрес отправителя не найден"
        is TransferError.AddressNotActivated ->
            "Для активации необходимо перейти в «Системный TRX»"
        is TransferError.InsufficientFeeBalance ->
            "Недостаточно средств для комиссии.\nАдрес: ${error.feeAddress}"
        is TransferError.AmountExceedsBalance ->
            "Сумма транзакции превышает доступную"
        is TransferError.InsufficientBalanceWithCommission ->
            "Недостаточно средств с учётом комиссии"
        is TransferError.InvalidCommission ->
            "Комиссия должна быть больше 0"
        is TransferError.FeeAddressUnavailable ->
            "Не удалось получить адрес для комиссии"
        is TransferError.ServerError ->
            "Ошибка сервера при отправке"
        is TransferError.NetworkError ->
            "Ошибка сети. Проверьте подключение"
        is TransferError.Unknown ->
            error.cause?.message ?: "Неизвестная ошибка"
        else -> error.message ?: "Неизвестная ошибка"
    }
}
