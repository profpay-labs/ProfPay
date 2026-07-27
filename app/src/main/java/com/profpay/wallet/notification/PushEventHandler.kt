package com.profpay.wallet.notification

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.data.aml.service.AmlPaymentProcessor
import com.profpay.data.transfer.service.IncomingTransactionProcessor
import com.profpay.data.transfer.service.TransactionToken
import com.profpay.domain.aml.repository.PendingAmlTransactionLocalRepository
import com.profpay.domain.contract.model.local.SmartContractLocal
import com.profpay.domain.contract.repository.SmartContractLocalRepository
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.notification.NotificationUtils.showNotification
import io.sentry.Sentry
import javax.inject.Inject

/**
 * Координатор обработки push-событий.
 * Делегирует обработку соответствующим сервисам и репозиториям.
 */
class PushEventHandler @Inject constructor(
    private val pendingAmlTransactionLocalRepository: PendingAmlTransactionLocalRepository,
    private val pendingTransactionLocalRepository: PendingTransactionLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val addressLocalRepository: AddressLocalRepository,
    private val smartContractLocalRepository: SmartContractLocalRepository,
    private val incomingTransactionProcessor: IncomingTransactionProcessor,
    private val amlPaymentProcessor: AmlPaymentProcessor,
) {

    suspend fun handle(context: Context, event: PushEvent, intent: Intent) {
        val notificationTitle = intent.getStringExtra("title") ?: "Уведомление"
        val notificationText = intent.getStringExtra("message") ?: "Сообщение"

        when (event) {
            is PushEvent.AmlPaymentSuccess -> {
                pendingAmlTransactionLocalRepository.markAsSuccessful(event.transactionId)
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.AmlPaymentError -> {
                pendingAmlTransactionLocalRepository.markAsError(event.transactionId)
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.TransferError -> {
                handleTransferError(event)
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.TransferSuccess -> {
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.DeployContractSuccess -> {
                handleDeploy(event)
                showNotification(context, notificationTitle, notificationText)
            }

            is PushEvent.NewTransaction -> {
                if (event.type == RestTransactionType.CENTRAL) {
                    handleCentralAddressTransaction(context, event)
                } else {
                    handleNewTransaction(context, event)
                }
            }
        }
    }

    private suspend fun handleTransferError(event: PushEvent.TransferError) {
        val address = addressLocalRepository.getByAddress(event.senderAddress)
        pendingTransactionLocalRepository.deleteByTxId(event.transactionId)
        transactionLocalRepository.deleteByTxId(event.transactionId)

        address?.id?.let {
            transactionLocalRepository.markAsUnprocessedByTxId(event.transactionId)
        }
    }

    private suspend fun handleDeploy(event: PushEvent.DeployContractSuccess) {
        if (smartContractLocalRepository.get() == null) {
            smartContractLocalRepository.insert(
                SmartContractLocal(
                    contractAddress = event.contractAddress,
                    ownerAddress = event.address
                )
            )
        } else {
            smartContractLocalRepository.restore(event.contractAddress)
        }
    }

    private suspend fun handleCentralAddressTransaction(
        context: Context,
        event: PushEvent.NewTransaction,
    ) {
        when (val result = incomingTransactionProcessor.processCentralAddressTransaction(
            txid = event.txid,
            from = event.from,
            to = event.to,
            amountStr = event.amount,
            tokenSymbol = event.token.symbol,
            blockTimestamp = event.blockTimestamp,
        )) {
            is IncomingTransactionProcessor.CentralAddressResult.Deposited -> {
                showNotification(
                    context,
                    "\uD83D\uDCB0 Пополнение центрального адреса",
                    "Получено: ${result.amount.toTokenAmount()} ${result.tokenSymbol}\n" +
                        "От ${result.fromAddress.take(6)}...${result.fromAddress.takeLast(4)}",
                )
            }
            is IncomingTransactionProcessor.CentralAddressResult.Processed -> {
                // Обработано, но не депозит — ничего не показываем
            }
            is IncomingTransactionProcessor.CentralAddressResult.Skipped -> {
                // Дубликат — пропускаем
            }
        }
    }

    private suspend fun handleNewTransaction(context: Context, event: PushEvent.NewTransaction) {
        val sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            MODE_PRIVATE
        )
        val autoCheckAml = sharedPrefs.getBoolean(PrefKeys.AUTO_CHECK_AML, true)

        val tokenType = when (event.token) {
            com.profpay.wallet.notification.TransactionToken.TRX -> TransactionToken.TRX
            com.profpay.wallet.notification.TransactionToken.USDT -> TransactionToken.USDT
        }

        when (val result = incomingTransactionProcessor.processNewTransaction(
            txid = event.txid,
            targetAddress = event.targetAddress,
            from = event.from,
            to = event.to,
            amountStr = event.amount,
            tokenSymbol = event.token.symbol,
            tokenType = tokenType,
            blockTimestamp = event.blockTimestamp,
            autoCheckAml = autoCheckAml,
        )) {
            is IncomingTransactionProcessor.ProcessResult.Sent -> {
                showNotification(
                    context,
                    "\uD83D\uDCB8 Отправлено: ${result.amount.toTokenAmount()} ${result.tokenSymbol}",
                    "На ${result.toAddress.take(6)}...${result.toAddress.takeLast(4)}",
                )
            }

            is IncomingTransactionProcessor.ProcessResult.Received -> {
                // Если нужна AML проверка — выполняем
                if (result.requiresAmlCheck) {
                    runCatching {
                        when (val amlResult = amlPaymentProcessor.processAmlReport(
                            address = result.toAddress,
                            txid = result.txid,
                        )) {
                            is AmlPaymentProcessor.AmlProcessResult.Success -> { /* OK */ }
                            is AmlPaymentProcessor.AmlProcessResult.Error -> {
                                showNotification(
                                    context,
                                    "Ошибка авто. проверки AML",
                                    amlResult.message
                                )
                            }
                        }
                    }.onFailure { ex ->
                        Sentry.captureException(ex)
                    }
                }

                showNotification(
                    context,
                    "\uD83D\uDCB0 Получено: ${result.amount.toTokenAmount()} ${result.tokenSymbol}",
                    "От ${result.fromAddress.take(6)}...${result.fromAddress.takeLast(4)}",
                )
            }

            is IncomingTransactionProcessor.ProcessResult.Skipped -> {
                // Дубликат или не наш адрес — пропускаем
            }
        }
    }
}
