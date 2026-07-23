package com.profpay.domain.aml.repository

import com.profpay.domain.aml.model.AmlPaymentResult
import com.profpay.domain.aml.model.AmlReport

/**
 * Репозиторий для работы с AML отчётами
 */
interface AmlRepository {

    /**
     * Получить AML отчёт по транзакции
     *
     * @param txHash хэш транзакции
     * @param address TRX адрес отправителя
     * @param userId ID пользователя в системе
     * @param tokenName название токена (по умолчанию USDT)
     * @return Result с AML отчётом или ошибкой
     */
    suspend fun getAmlReport(
        txHash: String,
        address: String,
        userId: Long,
        tokenName: String = "USDT",
    ): Result<AmlReport>

    /**
     * Обновить AML отчёт (запросить актуальные данные у провайдера)
     *
     * @throws AmlError.RenewCooldownNotExpired если с последнего обновления прошло менее 24 часов
     * @throws AmlError.ReportNotFound если отчёт не найден
     */
    suspend fun renewAmlReport(
        txHash: String,
        address: String,
        userId: Long,
        tokenName: String = "USDT",
    ): Result<AmlReport>

    /**
     * Создать платёж для AML проверки
     *
     * @param userId ID пользователя
     * @param txHash хэш транзакции
     * @param address адрес отправителя
     * @param paymentAddress адрес для оплаты bandwidth
     * @param bandwidthRequired требуемое количество bandwidth
     * @param txnBytes сериализованная транзакция в hex
     */
    suspend fun createAmlPayment(
        userId: Long,
        txHash: String,
        address: String,
        paymentAddress: String,
        bandwidthRequired: Long,
        txnBytes: String,
    ): Result<AmlPaymentResult>

    /**
     * Скачать PDF отчёт AML
     *
     * @param userId ID пользователя
     * @param txHash хэш транзакции
     * @return Result с байтами PDF файла или ошибкой
     */
    suspend fun downloadAmlPdf(
        userId: Long,
        txHash: String,
    ): Result<ByteArray>
}
