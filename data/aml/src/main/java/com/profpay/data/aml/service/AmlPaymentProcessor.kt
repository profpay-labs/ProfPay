package com.profpay.data.aml.service

import com.profpay.core.common.converter.toSunAmount
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.crypto.util.ByteUtils
import com.profpay.core.crypto.util.toBase64
import com.profpay.core.tron.Tron
import com.profpay.domain.aml.model.local.PendingAmlTransactionLocal
import com.profpay.domain.aml.repository.PendingAmlTransactionLocalRepository
import com.profpay.domain.aml.usecase.ProcessAmlPaymentUseCase
import com.profpay.domain.config.repository.ConfigRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Процессор AML-платежей.
 *
 * Отвечает за автоматическую обработку AML проверок входящих транзакций:
 * - Проверяет достаточность баланса для оплаты AML
 * - Подписывает транзакцию оплаты
 * - Отправляет запрос на сервер через UseCase
 * - Сохраняет pending транзакцию локально
 */
@Singleton
class AmlPaymentProcessor @Inject constructor(
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val pendingAmlTransactionLocalRepository: PendingAmlTransactionLocalRepository,
    private val tron: Tron,
    private val profileLocalRepository: ProfileLocalRepository,
    private val processAmlPaymentUseCase: ProcessAmlPaymentUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val configRepository: ConfigRepository,
) {

    /**
     * Результат обработки AML отчёта.
     */
    sealed class AmlProcessResult {
        data object Success : AmlProcessResult()
        data class Error(val message: String) : AmlProcessResult()
    }

    /**
     * Автоматически обрабатывает AML отчёт для входящей транзакции.
     *
     * @param address адрес получателя
     * @param txid хэш транзакции
     * @return результат операции
     */
    suspend fun processAmlReport(
        address: String,
        txid: String,
    ): AmlProcessResult = withContext(ioDispatcher) {
        val centralAddress = centralAddressLocalRepository.get()
            ?: return@withContext AmlProcessResult.Error("Не удалось получить central address")

        val balance = tron.addressUtilities.getTrxBalance(centralAddress.address)
        val userId = profileLocalRepository.getUserId()

        val serverParameters = configRepository.getFeeConfiguration().fold(
            onSuccess = { it },
            onFailure = {
                return@withContext AmlProcessResult.Error("Сервер недоступен")
            },
        )

        val amlFeeValue = serverParameters.amlFee
        val trxFeeAddress = serverParameters.trxFeeAddress

        val requiredAmount = amlFeeValue.toBigInteger().toTokenAmount()
        if (balance.toTokenAmount() < requiredAmount) {
            return@withContext AmlProcessResult.Error(
                "Недостаточно средств на балансе.\nНеобходимо: $requiredAmount TRX",
            )
        }

        val privateKey = ByteUtils.parseHex(centralAddress.privateKey)

        try {
            val paymentAmount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount()

            val signedTxnBytes = tron.transactions.getSignedTrxTransaction(
                fromAddress = centralAddress.address,
                toAddress = trxFeeAddress,
                privateKey = privateKey,
                amount = paymentAmount,
            )

            val estimateBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = centralAddress.address,
                toAddress = trxFeeAddress,
                privateKey = privateKey,
                amount = paymentAmount,
            )

            val txnBytes = signedTxnBytes.signedTxn?.toBase64()
                ?: return@withContext AmlProcessResult.Error("Не удалось подписать транзакцию")

            processAmlPaymentUseCase(
                ProcessAmlPaymentUseCase.Params(
                    userId = userId,
                    txHash = txid,
                    address = address,
                    paymentAddress = trxFeeAddress,
                    bandwidthRequired = estimateBandwidth.bandwidth,
                    txnBytes = txnBytes,
                ),
            ).fold(
                onSuccess = {
                    pendingAmlTransactionLocalRepository.insert(
                        PendingAmlTransactionLocal(txId = txid),
                    )
                    AmlProcessResult.Success
                },
                onFailure = { error ->
                    AmlProcessResult.Error(error.message ?: "Неизвестная ошибка")
                },
            )
        } finally {
            privateKey.fill(0) // Очищаем приватный ключ
        }
    }

    /**
     * Обрабатывает AML отчёт и возвращает Pair для обратной совместимости.
     * @deprecated Используйте [processAmlReport] с [AmlProcessResult]
     */
    @Deprecated(
        message = "Use processAmlReport with AmlProcessResult instead",
        replaceWith = ReplaceWith("processAmlReport(address, txid)"),
    )
    suspend fun processAmlReportLegacy(
        address: String,
        txid: String,
    ): Pair<Boolean, String> {
        return when (val result = processAmlReport(address, txid)) {
            is AmlProcessResult.Success -> true to "Успешное действие, ожидайте уведомление."
            is AmlProcessResult.Error -> false to result.message
        }
    }
}
