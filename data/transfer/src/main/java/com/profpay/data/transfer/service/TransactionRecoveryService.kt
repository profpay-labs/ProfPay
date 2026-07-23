package com.profpay.data.transfer.service

import android.database.sqlite.SQLiteConstraintException
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.core.tron.model.Trc20TransactionData
import com.profpay.core.tron.model.TrxTransactionData
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import com.profpay.domain.wallet.model.Transaction
import com.profpay.domain.wallet.model.TransactionStatusCode
import com.profpay.domain.wallet.model.TransactionType
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.TokenLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * Сервис восстановления транзакций с блокчейна.
 *
 * Используется для:
 * - Восстановления истории транзакций при первом запуске
 * - Синхронизации пропущенных транзакций
 * - Обновления балансов токенов
 */
@Singleton
class TransactionRecoveryService @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val tokenLocalRepository: TokenLocalRepository,
    private val tron: Tron,
    private val pendingTransactionLocalRepository: PendingTransactionLocalRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /**
     * Запустить восстановление транзакций для кошелька.
     */
    suspend fun recoverTransactions(walletId: Long) = withContext(ioDispatcher) {
        val addressList = addressLocalRepository.getSotsWithTokensByBlockchain(BLOCKCHAIN_TRON, walletId)

        for (addressWithTokens in addressList) {
            val address = addressWithTokens.address.address

            recoverUsdtTransactions(address)
            delay(API_DELAY_MS.milliseconds)

            recoverTrxTransactions(address)
            delay(API_DELAY_SHORT_MS.milliseconds)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // USDT (TRC20) Recovery
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun recoverUsdtTransactions(address: String) {
        try {
            tron.http.getTrc20Transactions(
                address = address,
                contractAddress = USDT_CONTRACT_ADDRESS,
                limit = TRANSACTION_LIMIT,
            ).onSuccess { transactions ->
                transactions.forEach { transaction ->
                    processUsdtTransaction(transaction, address)
                }

                // Обновляем баланс
                val addressEntity = addressLocalRepository.getByAddress(address)
                if (addressEntity != null) {
                    val balance = tron.addressUtilities.getUsdtBalance(address)
                    tokenLocalRepository.updateBalance(addressEntity.id, TOKEN_USDT, balance)
                }
            }
        } catch (e: Exception) {
//            Sentry.captureException(e) TODO: реализовать
        }
    }

    private suspend fun processUsdtTransaction(
        transaction: Trc20TransactionData,
        address: String,
    ) {
        val senderAddressEntity = addressLocalRepository.getByAddress(transaction.from)
        val receiverAddressEntity = addressLocalRepository.getByAddress(transaction.to)

        val addressEntity = when (address) {
            senderAddressEntity?.address -> senderAddressEntity
            receiverAddressEntity?.address -> receiverAddressEntity
            else -> return
        }

        val amount = BigInteger(transaction.value)

        insertTransactionSafely(
            Transaction(
                id = 0L,
                txId = transaction.transactionId,
                senderAddressId = senderAddressEntity?.id,
                receiverAddressId = receiverAddressEntity?.id,
                senderAddress = transaction.from,
                receiverAddress = transaction.to,
                walletId = addressEntity.walletId,
                tokenName = TOKEN_USDT,
                amount = amount,
                timestamp = transaction.blockTimestamp,
                status = STATUS_SUCCESS,
                isProcessed = false,
                type = resolveTransactionType(senderAddressEntity?.id, receiverAddressEntity?.id),
                statusCode = TransactionStatusCode.SUCCESS,
                commission = BigInteger.ZERO,
            ),
        )

        // Очищаем pending если существует
        if (pendingTransactionLocalRepository.exists(transaction.transactionId)) {
            pendingTransactionLocalRepository.deleteByTxId(transaction.transactionId)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TRX Recovery
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun recoverTrxTransactions(address: String) {
        try {
            tron.http.getTrxTransactions(address, limit = TRANSACTION_LIMIT)
                .onSuccess { transactions ->
                    transactions.forEach { transaction ->
                        val contract = transaction.rawData?.contract?.getOrNull(0)
                        if (contract?.type == CONTRACT_TYPE_TRANSFER) {
                            processTrxTransaction(transaction, address)
                        }
                    }

                    // Обновляем баланс
                    val addressEntity = addressLocalRepository.getByAddress(address)
                    if (addressEntity != null) {
                        val balance = tron.addressUtilities.getTrxBalance(address)
                        tokenLocalRepository.updateBalance(addressEntity.id, TOKEN_TRX, balance)
                    }
                }
        } catch (e: Exception) {
//            Sentry.captureException(e) TODO: реализовать
        }
    }

    private suspend fun processTrxTransaction(
        transaction: TrxTransactionData,
        address: String,
    ) {
        val contract = transaction.rawData?.contract?.getOrNull(0) ?: return
        val parameterValue = contract.parameter?.value ?: return

        if (parameterValue.toAddress == null || parameterValue.amount == null) return

        val ownerAddress = tron.addressUtilities.hexToBase58CheckAddress(parameterValue.ownerAddress)
        val toAddress = tron.addressUtilities.hexToBase58CheckAddress(parameterValue.toAddress)

        val senderAddressEntity = addressLocalRepository.getByAddress(ownerAddress)
        val receiverAddressEntity = addressLocalRepository.getByAddress(toAddress)

        val addressEntity = when (address) {
            senderAddressEntity?.address -> senderAddressEntity
            receiverAddressEntity?.address -> receiverAddressEntity
            else -> return
        }

        insertTransactionSafely(
            Transaction(
                id = 0L,
                txId = transaction.txId,
                senderAddressId = senderAddressEntity?.id,
                receiverAddressId = receiverAddressEntity?.id,
                senderAddress = ownerAddress,
                receiverAddress = toAddress,
                walletId = addressEntity.walletId,
                tokenName = TOKEN_TRX,
                amount = BigInteger.valueOf(parameterValue.amount),
                timestamp = transaction.blockTimestamp,
                status = STATUS_SUCCESS,
                isProcessed = false,
                type = resolveTransactionType(senderAddressEntity?.id, receiverAddressEntity?.id),
                statusCode = TransactionStatusCode.SUCCESS,
                commission = BigInteger.ZERO,
            ),
        )

        // Очищаем pending если существует
        if (pendingTransactionLocalRepository.exists(transaction.txId)) {
            pendingTransactionLocalRepository.deleteByTxId(transaction.txId)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helper methods
    // ══════════════════════════════════════════════════════════════════════

    private suspend fun insertTransactionSafely(transaction: Transaction) {
        try {
            transactionLocalRepository.insert(transaction)
        } catch (_: SQLiteConstraintException) {
            // Транзакция уже существует — игнорируем
        }
    }

    private fun resolveTransactionType(senderId: Long?, receiverId: Long?): TransactionType {
        return when {
            senderId != null && receiverId != null -> TransactionType.BETWEEN_YOURSELF
            senderId != null -> TransactionType.SEND
            receiverId != null -> TransactionType.RECEIVE
            else -> TransactionType.UNKNOWN
        }
    }

    private companion object {
        const val BLOCKCHAIN_TRON = "Tron"
        const val USDT_CONTRACT_ADDRESS = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        const val TOKEN_USDT = "USDT"
        const val TOKEN_TRX = "TRX"
        const val CONTRACT_TYPE_TRANSFER = "TransferContract"
        const val STATUS_SUCCESS = "Success"
        const val TRANSACTION_LIMIT = 200
        const val API_DELAY_MS = 2000L
        const val API_DELAY_SHORT_MS = 1000L
    }
}
