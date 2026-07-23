package com.profpay.domain.aml.usecase

import javax.inject.Inject

/**
 * UseCase для автоматической проверки AML при входящих транзакциях.
 * Используется в фоновых сервисах (push-уведомления).
 *
 * Возвращает параметры, необходимые для выполнения AML платежа.
 * Сама логика подписания транзакции остаётся в app модуле,
 * так как требует доступ к Tron SDK.
 */
class AutoAmlCheckUseCase @Inject constructor(
    private val processAmlPaymentUseCase: ProcessAmlPaymentUseCase,
) {
    suspend operator fun invoke(params: Params): Result<Unit> =
        processAmlPaymentUseCase(
            ProcessAmlPaymentUseCase.Params(
                userId = params.userId,
                txHash = params.txHash,
                address = params.address,
                paymentAddress = params.paymentAddress,
                bandwidthRequired = params.bandwidthRequired,
                txnBytes = params.txnBytes,
            ),
        ).map { }

    data class Params(
        val userId: Long,
        val txHash: String,
        val address: String,
        val paymentAddress: String,
        val bandwidthRequired: Long,
        val txnBytes: String,
    )
}
