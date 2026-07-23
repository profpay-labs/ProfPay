package com.profpay.data.transfer.service

import android.database.sqlite.SQLiteConstraintException
import com.profpay.core.tron.Tron
import com.profpay.core.tron.model.Trc20TransactionData
import com.profpay.core.tron.model.TrxTransactionData
import com.profpay.domain.aml.repository.AmlRepository
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.model.Transaction
import com.profpay.domain.wallet.model.TransactionStatusCode
import com.profpay.domain.wallet.model.TransactionType
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.repository.local.TokenLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * Сервис синхронизации транзакций с блокчейном TRON.
 *
 * Выполняет:
 * - Получение TRC20 и TRX транзакций с блокчейна
 * - Сохранение/обновление транзакций в локальной БД
 * - Обновление балансов токенов
 * - Запрос AML отчётов для новых входящих транзакций
 * - Автоматическую активацию неактивированных адресов
 */
@Singleton
class TransactionSyncService @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val profileLocalRepository: ProfileLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val tokenLocalRepository: TokenLocalRepository,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val tron: Tron,
    private val pendingTransactionLocalRepository: PendingTransactionLocalRepository,
    private val amlRepository: AmlRepository,
) {

    /**
     * Запустить полную синхронизацию транзакций для кошелька.
     */
    suspend fun startSync(walletId: Long) {
        val addressList = addressLocalRepository.getSotsWithTokensByBlockchain(BLOCKCHAIN_TRON, walletId)
        val centralAddress = centralAddressLocalRepository.get()

        for (address in addressList) {
            syncTrc20Transactions(address.address.address)
            delay(SYNC_DELAY_MS.milliseconds)

            syncTrxTransactions(address.address.address)
            delay(SYNC_DELAY_MS.milliseconds)

            centralAddress?.let {
                syncCentralAddressTransactions(it.address)
            }
            delay(SYNC_DELAY_MS.milliseconds)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TRC20 (USDT) Transactions
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun syncTrc20Transactions(address: String) {
        try {
            tron.http.getTrc20Transactions(address, contractAddress = null, limit = TRANSACTION_LIMIT)
                .onSuccess { transactions ->
                    transactions
                        .filter { it.type == TRANSFER_TYPE }
                        .forEach { processUsdtTransfer(it, TOKEN_USDT, address) }
                }
        } catch (e: Exception) {
//            Sentry.captureException(e) TODO: Реализовать
        }
    }

    private suspend fun processUsdtTransfer(
        transaction: Trc20TransactionData,
        tokenName: String,
        address: String,
    ) = coroutineScope {
        if (transaction.type != TRANSFER_TYPE) return@coroutineScope

        val senderAddressEntity = addressLocalRepository.getByAddress(transaction.from)
        val receiverAddressEntity = addressLocalRepository.getByAddress(transaction.to)

        val (addressEntity, isSender) = when (address) {
            senderAddressEntity?.address -> senderAddressEntity to true
            receiverAddressEntity?.address -> receiverAddressEntity to false
            else -> return@coroutineScope
        }

        val amount = BigInteger(transaction.value)
        val isTransactionPending = transactionLocalRepository.isPending(transaction.transactionId)

        if (isTransactionPending && isSender) {
            transactionLocalRepository.updateStatusAndTimestamp(
                statusCode = TransactionStatusCode.SUCCESS,
                timestamp = transaction.blockTimestamp,
                txId = transaction.transactionId,
            )
        } else {
            insertTransactionSafely(
                Transaction(
                    id = 0L,
                    txId = transaction.transactionId,
                    senderAddressId = senderAddressEntity?.id,
                    receiverAddressId = receiverAddressEntity?.id,
                    senderAddress = transaction.from,
                    receiverAddress = transaction.to,
                    walletId = addressEntity.walletId,
                    tokenName = tokenName,
                    amount = amount,
                    timestamp = transaction.blockTimestamp,
                    status = STATUS_SUCCESS,
                    isProcessed = false,
                    type = resolveTransactionType(senderAddressEntity?.id, receiverAddressEntity?.id),
                    statusCode = TransactionStatusCode.SUCCESS,
                    commission = BigInteger.ZERO,
                ),
            ) ?: return@coroutineScope
        }

        // Обновляем балансы
        senderAddressEntity?.let { entity ->
            val balance = tron.addressUtilities.getUsdtBalance(entity.address)
            tokenLocalRepository.updateBalance(entity.id, tokenName, balance)
        }

        receiverAddressEntity?.let { entity ->
            requestAmlReportIfRecent(
                transaction.blockTimestamp,
                transaction.from,
                transaction.transactionId,
                tokenName,
            )

            val balance = tron.addressUtilities.getUsdtBalance(entity.address)
            tokenLocalRepository.updateBalance(entity.id, tokenName, balance)
        }

        // Очищаем pending
        if (pendingTransactionLocalRepository.exists(transaction.transactionId)) {
            pendingTransactionLocalRepository.deleteByTxId(transaction.transactionId)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TRX Transactions
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun syncTrxTransactions(address: String) {
        try {
            tron.http.getTrxTransactions(address, limit = TRANSACTION_LIMIT)
                .onSuccess { transactions ->
                    transactions.forEach { transaction ->
                        val contract = transaction.rawData?.contract?.getOrNull(0)
                        if (contract?.type == CONTRACT_TYPE_TRANSFER) {
                            processTrxTransfer(transaction, TOKEN_TRX, address)
                        }
                    }
                }
        } catch (e: Exception) {
//            Sentry.captureException(e) TODO: Реализовать
        }
    }

    private suspend fun processTrxTransfer(
        transaction: TrxTransactionData,
        tokenName: String,
        address: String,
    ) = coroutineScope {
        if (transactionLocalRepository.countByTxId(transaction.txId) > 2) return@coroutineScope

        val contract = transaction.rawData?.contract?.getOrNull(0) ?: return@coroutineScope
        val parameterValue = contract.parameter?.value ?: return@coroutineScope

        if (parameterValue.toAddress == null) return@coroutineScope

        val ownerAddress = tron.addressUtilities.hexToBase58CheckAddress(parameterValue.ownerAddress)
        val toAddress = tron.addressUtilities.hexToBase58CheckAddress(parameterValue.toAddress)

        val senderAddressEntity = addressLocalRepository.getByAddress(ownerAddress)
        val receiverAddressEntity = addressLocalRepository.getByAddress(toAddress)

        val (addressEntity, isSender) = when (address) {
            senderAddressEntity?.address -> senderAddressEntity to true
            receiverAddressEntity?.address -> receiverAddressEntity to false
            else -> return@coroutineScope
        }

        val transactionType = when (contract.type) {
            CONTRACT_TYPE_TRANSFER -> resolveTransactionType(senderAddressEntity?.id, receiverAddressEntity?.id)
            CONTRACT_TYPE_TRIGGER_SMART -> TransactionType.TRIGGER_SMART_CONTRACT
            else -> return@coroutineScope
        }

        val isTransactionPending = transactionLocalRepository.isPending(transaction.txId)

        if (isTransactionPending && isSender) {
            transactionLocalRepository.updateStatusAndTimestamp(
                statusCode = TransactionStatusCode.SUCCESS,
                timestamp = transaction.blockTimestamp,
                txId = transaction.txId,
            )
        } else {
            insertTransactionSafely(
                Transaction(
                    id = 0L,
                    txId = transaction.txId,
                    senderAddressId = senderAddressEntity?.id,
                    receiverAddressId = receiverAddressEntity?.id,
                    senderAddress = ownerAddress,
                    receiverAddress = toAddress,
                    walletId = addressEntity.walletId,
                    tokenName = tokenName,
                    amount = BigInteger.valueOf(parameterValue.amount),
                    timestamp = transaction.blockTimestamp,
                    status = STATUS_SUCCESS,
                    isProcessed = false,
                    type = transactionType,
                    statusCode = TransactionStatusCode.SUCCESS,
                    commission = BigInteger.ZERO,
                ),
            ) ?: return@coroutineScope
        }

        // Обновляем балансы
        senderAddressEntity?.let { entity ->
            val balance = tron.addressUtilities.getTrxBalance(entity.address)
            tokenLocalRepository.updateBalance(entity.id, tokenName, balance)
        }

        receiverAddressEntity?.let { entity ->
            // AML только для значимых сумм (> 1 TRX)
            if (parameterValue.amount > MIN_AML_AMOUNT_SUN) {
                requestAmlReportIfRecent(
                    transaction.blockTimestamp,
                    ownerAddress,
                    transaction.txId,
                    tokenName,
                )
            }

            val balance = tron.addressUtilities.getTrxBalance(entity.address)
            tokenLocalRepository.updateBalance(entity.id, tokenName, balance)
        }

        // Очищаем pending
        if (pendingTransactionLocalRepository.exists(transaction.txId)) {
            pendingTransactionLocalRepository.deleteByTxId(transaction.txId)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Central Address Transactions
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun syncCentralAddressTransactions(centralAddress: String) {
        try {
            tron.http.getTrxTransactions(centralAddress, limit = TRANSACTION_LIMIT)
                .onSuccess { transactions ->
                    transactions.forEach { transaction ->
                        val contract = transaction.rawData?.contract?.getOrNull(0)
                        if (contract?.type == CONTRACT_TYPE_TRANSFER) {
                            processCentralTrxTransfer(centralAddress, transaction, TOKEN_TRX)
                        }
                    }
                }
        } catch (e: Exception) {
//            Sentry.captureException(e) TODO: Реализовать
        }
    }

    private suspend fun processCentralTrxTransfer(
        centralAddress: String,
        transaction: TrxTransactionData,
        tokenName: String,
    ) = coroutineScope {
        if (transactionLocalRepository.countByTxId(transaction.txId) == 1) return@coroutineScope

        val contract = transaction.rawData?.contract?.getOrNull(0) ?: return@coroutineScope
        val parameterValue = contract.parameter?.value ?: return@coroutineScope

        if (parameterValue.toAddress == null) return@coroutineScope

        val ownerAddress = tron.addressUtilities.hexToBase58CheckAddress(parameterValue.ownerAddress)
        val toAddress = tron.addressUtilities.hexToBase58CheckAddress(parameterValue.toAddress)

        val (senderAddressEntity, receiverAddressEntity) = listOf(
            async { addressLocalRepository.getByAddress(ownerAddress) },
            async { addressLocalRepository.getByAddress(toAddress) },
        ).awaitAll()

        val typeCode = resolveTransactionType(
            senderAddressEntity?.id,
            receiverAddressEntity?.id,
            isCentralAddress = true,
        )

        insertTransactionSafely(
            Transaction(
                id = 0L,
                txId = transaction.txId,
                senderAddressId = senderAddressEntity?.id,
                receiverAddressId = receiverAddressEntity?.id,
                senderAddress = ownerAddress,
                receiverAddress = toAddress,
                walletId = 0,
                tokenName = tokenName,
                amount = BigInteger.valueOf(parameterValue.amount),
                timestamp = transaction.blockTimestamp,
                status = STATUS_SUCCESS,
                isProcessed = false,
                type = typeCode,
                statusCode = TransactionStatusCode.SUCCESS,
                commission = BigInteger.ZERO,
            ),
        ) ?: return@coroutineScope

        // Автоактивация неактивированных адресов
        activateInactiveAddressesIfNeeded(centralAddress)
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helper methods
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun insertTransactionSafely(transaction: Transaction): Unit? {
        return try {
            transactionLocalRepository.insert(transaction)
        } catch (_: SQLiteConstraintException) {
            null // Транзакция уже существует
        }
    }

    private suspend fun requestAmlReportIfRecent(
        blockTimestamp: Long,
        fromAddress: String,
        txHash: String,
        tokenName: String,
    ) {
        val blockTime = Instant.ofEpochMilli(blockTimestamp)
        val now = Instant.now()
        val withinWindow = Duration.between(blockTime, now).abs().toMinutes() <= AML_WINDOW_MINUTES

        if (withinWindow) {
            runCatching {
                val userId = profileLocalRepository.getUserId()
                amlRepository.getAmlReport(
                    userId = userId,
                    address = fromAddress,
                    txHash = txHash,
                    tokenName = tokenName,
                )
            }.onFailure {
//                Sentry.captureException(it) TODO: Реализовать
            }
        }
    }

    private suspend fun activateInactiveAddressesIfNeeded(centralAddress: String) {
        val centralAddressEntity = centralAddressLocalRepository.get() ?: return
        val balance = tron.addressUtilities.getTrxBalance(centralAddressEntity.address)

        if (balance < MIN_BALANCE_FOR_ACTIVATION) return

        val addresses = addressLocalRepository.getAllSotsWithTokensByBlockchain(BLOCKCHAIN_TRON)
        for (addressData in addresses) {
            val currentBalance = tron.addressUtilities.getTrxBalance(centralAddressEntity.address)
            if (currentBalance < ACTIVATION_THRESHOLD) break

            if (!tron.addressUtilities.isAddressActivated(addressData.address.address)) {
                tron.transactions.trxTransfer(
                    fromAddress = centralAddressEntity.address,
                    toAddress = addressData.address.address,
                    privateKey = centralAddressEntity.privateKey,
                    amount = ACTIVATION_AMOUNT,
                )
            }
        }
    }

    private fun resolveTransactionType(
        senderId: Long?,
        receiverId: Long?,
        isCentralAddress: Boolean = false,
    ): TransactionType {
        return when {
            senderId != null && receiverId != null -> TransactionType.BETWEEN_YOURSELF
            senderId != null -> TransactionType.SEND
            receiverId != null -> TransactionType.RECEIVE
            isCentralAddress -> TransactionType.CENTRAL_ADDRESS
            else -> TransactionType.UNKNOWN
        }
    }

    companion object {
        private const val BLOCKCHAIN_TRON = "Tron"
        private const val TOKEN_USDT = "USDT"
        private const val TOKEN_TRX = "TRX"
        private const val TRANSFER_TYPE = "Transfer"
        private const val CONTRACT_TYPE_TRANSFER = "TransferContract"
        private const val CONTRACT_TYPE_TRIGGER_SMART = "TriggerSmartContract"
        private const val STATUS_SUCCESS = "Success"
        private const val TRANSACTION_LIMIT = 200
        private const val SYNC_DELAY_MS = 3000L
        private const val AML_WINDOW_MINUTES = 10L
        private const val MIN_AML_AMOUNT_SUN = 1_000_000L // 1 TRX
        private val MIN_BALANCE_FOR_ACTIVATION = BigInteger.valueOf(1_500_000)
        private val ACTIVATION_THRESHOLD = BigInteger.valueOf(1_000_000)
        private const val ACTIVATION_AMOUNT = 1_000L
    }
}
