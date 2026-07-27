package com.profpay.wallet.presentation.viewmodel.dto.transfer

sealed class TransferResult {
    object Success : TransferResult()

    data class Failure(
        val error: Throwable,
    ) : TransferResult()
}
