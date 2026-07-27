package com.profpay.wallet.presentation.viewmodel.smartcontract

import com.profpay.wallet.presentation.viewmodel.smartcontract.usecases.DealActionResult

data class CompleteReturnData(
    val status: CompleteStatusesEnum,
    val result: DealActionResult? = null,
)
