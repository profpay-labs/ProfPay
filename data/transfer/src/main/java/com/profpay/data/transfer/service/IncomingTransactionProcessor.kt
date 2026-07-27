package com.profpay.data.transfer.service

import android.database.sqlite.SQLiteConstraintException
import com.profpay.core.tron.Tron
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
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
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Процессор входящих транзакций из push-уведомлений.
 */
@Singleton
class IncomingTransactionProcessor @Inject constructor(
    private val pendingTransactionLocalRepository: PendingTransactionLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val addressLocalRepository: AddressLocalRepository,
    private val tokenLocalRepository: TokenLocalRepository,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val tron: Tron,
) {

    /**
     * Результат обработки транзакции.
     */
    sealed class ProcessResult {
        data class Sent(val amount: BigInteger, val tokenSymbol: String, val toAddress: String) : ProcessResult()

        data class Received(
            val amount: BigInteger,
            val tokenSymbol: String,
            val fromAddress: String,
            val toAddress: String,
            val txid: String,
            val requiresAmlCheck: Boolean,
        ) : ProcessResult()

        data object Skipped : ProcessResult()
    }

    /**
     * Обрабатывает входящую транзакцию.
     *
     * @param autoCheckAml если true и транзакция требует AML, вернётся Received с requiresAmlCheck=true
     */
    suspend fun processNewTransaction(
        txid: String,
        targetAddress: String,
        from: String,
        to: String,
        amountStr: String,
        tokenSymbol: String,
        tokenType: TransactionToken,
        blockTimestamp: Long,
        autoCheckAml: Boolean,
    ): ProcessResult {
        val senderAddressEntity = addressLocalRepository.getByAddress(from)
        val receiverAddressEntity = addressLocalRepository.getByAddress(to)

        val (addressEntity, isSender) = when (targetAddress) {
            senderAddressEntity?.address -> Pair(senderAddressEntity, true)
            receiverAddressEntity?.address -> Pair(receiverAddressEntity, false)
            else -> return ProcessResult.Skipped
        }

        val amount = BigInteger(amountStr)
        val isTransactionPending = transactionLocalRepository.isPending(txid)

        if (isTransactionPending && isSender) {
            transactionLocalRepository.updateStatusAndTimestamp(
                statusCode = TransactionStatusCode.SUCCESS,
                timestamp = blockTimestamp,
                txId = txid,
            )
        } else {
            try {
                transactionLocalRepository.insert(
                    Transaction(
                        id = 0L,
                        txId = txid,
                        senderAddressId = senderAddressEntity?.id,
                        receiverAddressId = receiverAddressEntity?.id,
                        senderAddress = from,
                        receiverAddress = to,
                        walletId = addressEntity.walletId,
                        tokenName = tokenSymbol,
                        amount = amount,
                        timestamp = blockTimestamp,
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
                return ProcessResult.Skipped
            }
        }

        // Обновляем баланс отправителя
        if (senderAddressEntity != null) {
            updateBalance(senderAddressEntity.id, senderAddressEntity.address, tokenType)
        }

        // Обновляем баланс получателя
        if (receiverAddressEntity != null) {
            updateBalance(receiverAddressEntity.id, receiverAddressEntity.address, tokenType)
        }

        return if (isSender) {
            pendingTransactionLocalRepository.deleteByTxId(txid)
            ProcessResult.Sent(amount, tokenSymbol, to)
        } else {
            // Определяем, нужна ли AML проверка
            val requiresAmlCheck = autoCheckAml &&
                senderAddressEntity == null &&
                shouldCheckAml(tokenType, amount)

            ProcessResult.Received(
                amount = amount,
                tokenSymbol = tokenSymbol,
                fromAddress = from,
                toAddress = to,
                txid = txid,
                requiresAmlCheck = requiresAmlCheck,
            )
        }
    }

    /**
     * Обрабатывает транзакцию на центральный адрес.
     */
    suspend fun processCentralAddressTransaction(
        txid: String,
        from: String,
        to: String,
        amountStr: String,
        tokenSymbol: String,
        blockTimestamp: Long,
    ): CentralAddressResult = coroutineScope {
        val (senderAddressEntity, receiverAddressEntity) = listOf(
            async { addressLocalRepository.getByAddress(from) },
            async { addressLocalRepository.getByAddress(to) },
        ).awaitAll()

        val amount = BigInteger(amountStr)

        try {
            transactionLocalRepository.insert(
                Transaction(
                    id = 0L,
                    txId = txid,
                    senderAddressId = senderAddressEntity?.id,
                    receiverAddressId = receiverAddressEntity?.id,
                    senderAddress = from,
                    receiverAddress = to,
                    walletId = 0,
                    tokenName = tokenSymbol,
                    amount = amount,
                    timestamp = blockTimestamp,
                    status = "Success",
                    isProcessed = false,
                    type = TransactionType.CENTRAL_ADDRESS,
                    statusCode = TransactionStatusCode.SUCCESS,
                    commission = BigInteger.ZERO,
                ),
            )
        } catch (_: SQLiteConstraintException) {
            return@coroutineScope CentralAddressResult.Skipped
        }

        val centralAddress = centralAddressLocalRepository.get()
            ?: return@coroutineScope CentralAddressResult.Skipped

        val balance = tron.addressUtilities.getTrxBalance(centralAddress.address)
        centralAddressLocalRepository.updateTrxBalance(balance)

        val isDeposit = centralAddress.address == to
        if (isDeposit) {
            pendingTransactionLocalRepository.deleteByTxId(txid)
        }

        // Активация неактивированных адресов
        activateAddressesIfNeeded(centralAddress.address, centralAddress.privateKey, balance)

        if (isDeposit) {
            CentralAddressResult.Deposited(amount, tokenSymbol, from)
        } else {
            CentralAddressResult.Processed
        }
    }

    sealed class CentralAddressResult {
        data class Deposited(
            val amount: BigInteger,
            val tokenSymbol: String,
            val fromAddress: String,
        ) : CentralAddressResult()

        data object Processed : CentralAddressResult()
        data object Skipped : CentralAddressResult()
    }

    private suspend fun updateBalance(addressId: Long, address: String, tokenType: TransactionToken) {
        val balance = if (tokenType == TransactionToken.TRX) {
            tron.addressUtilities.getTrxBalance(address)
        } else {
            tron.addressUtilities.getUsdtBalance(address)
        }
        tokenLocalRepository.updateBalance(addressId, tokenType.symbol, balance)
    }

    private fun shouldCheckAml(tokenType: TransactionToken, amount: BigInteger): Boolean {
        val threshold = if (tokenType == TransactionToken.TRX) {
            BigInteger.valueOf(5_000_000) // 5 TRX в sun
        } else {
            BigInteger.valueOf(1_000_000) // 1 USDT в минимальных единицах
        }
        return amount > threshold
    }

    private suspend fun activateAddressesIfNeeded(
        centralAddress: String,
        privateKey: String,
        balance: BigInteger,
    ) {
        if (balance < BigInteger.valueOf(1_500_000)) return

        val addresses = addressLocalRepository.getAllSotsWithTokensByBlockchain("Tron")
        for (addressData in addresses) {
            val newBalance = tron.addressUtilities.getTrxBalance(centralAddress)
            if (newBalance < BigInteger.valueOf(1_000_000)) break
            if (!tron.addressUtilities.isAddressActivated(addressData.address.address)) {
                tron.transactions.trxTransfer(
                    fromAddress = centralAddress,
                    toAddress = addressData.address.address,
                    privateKey = privateKey,
                    amount = 1_000,
                )
            }
        }
    }
}

/**
 * Тип токена транзакции.
 */
enum class TransactionToken(val symbol: String) {
    TRX("TRX"),
    USDT("USDT"),
}
