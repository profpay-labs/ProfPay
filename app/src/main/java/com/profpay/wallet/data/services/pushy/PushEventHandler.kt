package com.profpay.wallet.data.services.pushy

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.tron.Tron
import com.profpay.data.aml.service.AmlPaymentProcessor
import com.profpay.domain.aml.repository.PendingAmlTransactionLocalRepository
import com.profpay.domain.contract.model.local.SmartContractLocal
import com.profpay.domain.contract.repository.SmartContractLocalRepository
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import com.profpay.domain.wallet.model.Transaction
import com.profpay.domain.wallet.model.TransactionStatusCode
import com.profpay.domain.wallet.model.TransactionType
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.repository.local.TokenLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.utils.NotificationUtils.showNotification
import io.sentry.Sentry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class PushEventHandler @Inject constructor(
    private val pendingAmlTransactionLocalRepository: PendingAmlTransactionLocalRepository,
    private val pendingTransactionLocalRepository: PendingTransactionLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val addressLocalRepository: AddressLocalRepository,
    private val smartContractLocalRepository: SmartContractLocalRepository,
    private val tron: Tron,
    private val tokenLocalRepository: TokenLocalRepository,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
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

    private suspend fun handleCentralAddressTransaction(context: Context, event: PushEvent.NewTransaction) = coroutineScope {
        val (senderAddressEntity, receiverAddressEntity) =
            listOf(
                async { addressLocalRepository.getByAddress(event.from) },
                async { addressLocalRepository.getByAddress(event.to) },
            ).awaitAll()

        val senderAddressId = senderAddressEntity?.id
        val receiverAddressId = receiverAddressEntity?.id

        val amount = BigInteger(event.amount)
        try {
            transactionLocalRepository.insert(
                Transaction(
                    id = 0L,
                    txId = event.txid,
                    senderAddressId = senderAddressId,
                    receiverAddressId = receiverAddressId,
                    senderAddress = event.from,
                    receiverAddress = event.to,
                    walletId = 0,
                    tokenName = event.token.symbol,
                    amount = amount,
                    timestamp = event.blockTimestamp,
                    status = "Success",
                    isProcessed = false,
                    type = TransactionType.CENTRAL_ADDRESS,
                    statusCode = TransactionStatusCode.SUCCESS,
                    commission = BigInteger.ZERO,
                ),
            )
        } catch (_: SQLiteConstraintException) {
            return@coroutineScope
        }

        val centralAddress = centralAddressLocalRepository.get()
        val balance = tron.addressUtilities.getTrxBalance(centralAddress!!.address)
        centralAddressLocalRepository.updateTrxBalance(balance)

        if (centralAddress.address == event.to) {
            pendingTransactionLocalRepository.deleteByTxId(event.txid)
            showNotification(
                context,
                "\uD83D\uDCB0 Пополнение центрального адреса",
                "Получено: ${amount.toTokenAmount()} ${event.token.symbol}\n" +
                    "От ${event.from.take(6)}...${event.from.takeLast(4)}",
            )
        }

        val addresses = addressLocalRepository.getAllSotsWithTokensByBlockchain("Tron")
        if (balance >= BigInteger.valueOf(1_500_000)) {
            for (addressData in addresses) {
                val newBalance = tron.addressUtilities.getTrxBalance(centralAddress.address)
                if (newBalance < BigInteger.valueOf(1_000_000)) break
                if (!tron.addressUtilities.isAddressActivated(addressData.address.address)) {
                    tron.transactions.trxTransfer(
                        fromAddress = centralAddress.address,
                        toAddress = addressData.address.address,
                        privateKey = centralAddress.privateKey,
                        amount = 1_000,
                    )
                }
            }
        }
    }

    private suspend fun handleNewTransaction(context: Context, event: PushEvent.NewTransaction) {
        val sharedPrefs = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            MODE_PRIVATE
        )

        val autoCheckAml = sharedPrefs.getBoolean(PrefKeys.AUTO_CHECK_AML, true)

        val senderAddressEntity = addressLocalRepository.getByAddress(event.from)
        val receiverAddressEntity = addressLocalRepository.getByAddress(event.to)

        val (addressEntity, isSender) =
            when (event.targetAddress) {
                senderAddressEntity?.address -> Pair(senderAddressEntity, true)
                receiverAddressEntity?.address -> Pair(receiverAddressEntity, false)
                else -> return
            }

        val amount = BigInteger(event.amount)

        val isTransactionPending = transactionLocalRepository.isPending(event.txid)

        if (isTransactionPending && isSender) {
            transactionLocalRepository.updateStatusAndTimestamp(
                statusCode = TransactionStatusCode.SUCCESS,
                timestamp = event.blockTimestamp,
                txId = event.txid,
            )
        } else {
            try {
                transactionLocalRepository.insert(
                    Transaction(
                        id = 0L,
                        txId = event.txid,
                        senderAddressId = senderAddressEntity?.id,
                        receiverAddressId = receiverAddressEntity?.id,
                        senderAddress = event.from,
                        receiverAddress = event.to,
                        walletId = addressEntity.walletId,
                        tokenName = event.token.symbol,
                        amount = amount,
                        timestamp = event.blockTimestamp,
                        status = "Success",
                        isProcessed = false,
                        type = when {
                            senderAddressEntity?.id != null && receiverAddressEntity?.id != null -> TransactionType.BETWEEN_YOURSELF
                            senderAddressEntity?.id != null -> TransactionType.SEND
                            else -> TransactionType.RECEIVE
                        },
                        statusCode = TransactionStatusCode.SUCCESS,
                        commission = BigInteger.ZERO,
                    ),
                )
            } catch (_: SQLiteConstraintException) {
                return
            }
        }

        if (senderAddressEntity != null) {
            if (event.token == TransactionToken.TRX) {
                val balance = tron.addressUtilities.getTrxBalance(senderAddressEntity.address)
                tokenLocalRepository.updateBalance(senderAddressEntity.id, event.token.symbol, balance)
            } else {
                val balance = tron.addressUtilities.getUsdtBalance(senderAddressEntity.address)
                tokenLocalRepository.updateBalance(senderAddressEntity.id, event.token.symbol, balance)
            }
        }

        if (receiverAddressEntity != null) {
            if (autoCheckAml && senderAddressEntity == null) {
                if (event.token == TransactionToken.TRX && amount.toTokenAmount() > BigDecimal(5)) {
                    runCatching {
                        when (val result = amlPaymentProcessor.processAmlReport(
                            address = event.to,
                            txid = event.txid,
                        )) {
                            is AmlPaymentProcessor.AmlProcessResult.Success -> {}
                            is AmlPaymentProcessor.AmlProcessResult.Error -> {
                                showNotification(context, "Ошибка авто. проверки AML", result.message)
                            }
                        }
                    }.onFailure { ex ->
                        Sentry.captureException(ex)
                    }
                }

                if (event.token == TransactionToken.USDT && amount.toTokenAmount() > BigDecimal(1)) {
                    runCatching {
                        when (val result = amlPaymentProcessor.processAmlReport(
                            address = event.to,
                            txid = event.txid,
                        )) {
                            is AmlPaymentProcessor.AmlProcessResult.Success -> {}
                            is AmlPaymentProcessor.AmlProcessResult.Error -> {
                                showNotification(context, "Ошибка авто. проверки AML", result.message)
                            }
                        }
                    }.onFailure { ex ->
                        Sentry.captureException(ex)
                    }
                }
            }

            if (event.token == TransactionToken.TRX) {
                val balance = tron.addressUtilities.getTrxBalance(receiverAddressEntity.address)
                tokenLocalRepository.updateBalance(receiverAddressEntity.id, event.token.symbol, balance)
            } else {
                val balance = tron.addressUtilities.getUsdtBalance(receiverAddressEntity.address)
                tokenLocalRepository.updateBalance(receiverAddressEntity.id, event.token.symbol, balance)
            }
        }

        if (isSender) {
            pendingTransactionLocalRepository.deleteByTxId(event.txid)
            showNotification(
                context,
                "\uD83D\uDCB8 Отправлено: ${amount.toTokenAmount()} ${event.token.symbol}",
                "На ${event.to.take(6)}...${event.to.takeLast(4)}",
            )
        } else {
            showNotification(
                context,
                "\uD83D\uDCB0 Получено: ${amount.toTokenAmount()} ${event.token.symbol}",
                "От ${event.from.take(6)}...${event.from.takeLast(4)}",
            )
        }
    }
}

