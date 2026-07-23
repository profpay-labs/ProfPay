package com.profpay.data.transfer.service

import android.database.sqlite.SQLiteConstraintException
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.crypto.util.ByteUtils
import com.profpay.core.crypto.util.toBase64
import com.profpay.core.tron.Tron
import com.profpay.core.tron.model.BandwidthEstimate
import com.profpay.core.tron.model.EnergyEstimate
import com.profpay.core.tron.model.SignedTransactionData
import com.profpay.domain.config.repository.ConfigRepository
import com.profpay.domain.security.PrivateKeyProvider
import com.profpay.domain.transfer.exception.TransferError
import com.profpay.domain.transfer.model.CreateTransferParams
import com.profpay.domain.transfer.model.EstimateCommissionResult
import com.profpay.domain.transfer.model.TransactionData
import com.profpay.domain.transfer.model.TransferCommissionData
import com.profpay.domain.transfer.model.TransferUiResult
import com.profpay.domain.transfer.model.TransferToken
import com.profpay.domain.transfer.model.local.PendingTransactionLocal
import com.profpay.domain.transfer.repository.PendingTransactionLocalRepository
import com.profpay.domain.transfer.repository.TransferRepository
import com.profpay.domain.transfer.usecase.ValidateTransferUseCase
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.model.TokenType
import com.profpay.domain.wallet.model.Transaction
import com.profpay.domain.wallet.model.TransactionStatusCode
import com.profpay.domain.wallet.model.TransactionType
import com.profpay.domain.wallet.model.local.TokenBalanceLocal
import com.profpay.domain.wallet.model.local.WalletAddressLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.repository.local.TokenLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Процессор транзакций — отвечает за создание, подписание и отправку транзакций.
 *
 * Координирует:
 * - Валидацию параметров перевода
 * - Подписание транзакции и транзакции комиссии
 * - Отправку на сервер
 * - Сохранение в локальную БД
 */
@Singleton
class TransactionProcessor @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val profileLocalRepository: ProfileLocalRepository,
    private val tokenLocalRepository: TokenLocalRepository,
    private val pendingTransactionLocalRepository: PendingTransactionLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val transferRepository: TransferRepository,
    private val privateKeyProvider: PrivateKeyProvider,
    private val validateTransfer: ValidateTransferUseCase,
    private val tron: Tron,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val configRepository: ConfigRepository,
) {

    /**
     * Отправить транзакцию.
     * Возвращает [TransferUiResult] — Success или Failure с типизированной ошибкой.
     */
    suspend fun sendTransaction(
        sender: String,
        receiver: String,
        amount: BigInteger,
        commission: BigInteger,
        tokenEntity: TokenBalanceLocal?,
        commissionResult: EstimateCommissionResult,
    ): TransferUiResult {
        val tokenName = tokenEntity?.tokenName
            ?: return TransferUiResult.Failure(TransferError.TokenNotFound)

        val senderAddressEntity = addressLocalRepository.getByAddress(sender)
            ?: return TransferUiResult.Failure(TransferError.SenderAddressNotFound)

        // 1. Получаем приватный ключ
        val privateKey = privateKeyProvider.resolve(senderAddressEntity)

        return try {
            // 2. Определяем адрес для комиссии
            val commissionAddressInfo = getCommissionAddressInfo(
                tokenName = tokenName,
                privateKey = privateKey,
                addressEntity = senderAddressEntity,
            )

            // 3. Валидация
            val validationResult = validateTransfer.validate(
                ValidateTransferUseCase.Params(
                    senderAddress = sender,
                    commissionAddress = commissionAddressInfo.address,
                    tokenType = TokenType.fromName(tokenName) ?: TokenType.USDT,
                    tokenBalance = tokenEntity.availableBalance.toTokenAmount(),
                    amount = amount.toTokenAmount(),
                    commission = commission.toTokenAmount(),
                ),
            )

            if (validationResult.isFailure) {
                return TransferUiResult.Failure(
                    validationResult.exceptionOrNull() ?: TransferError.Unknown(),
                )
            }

            // 4. Получаем fee-адрес
            val feeAddress = getFeeAddress()
                ?: return TransferUiResult.Failure(TransferError.FeeAddressUnavailable)

            // 5. Рассчитываем итоговую сумму
            val actualAmount = calculateActualAmount(receiver, tokenName, amount, commission)

            val transferToken = if (tokenName == TokenType.TRX.tokenName) {
                TransferToken.TRX
            } else {
                TransferToken.USDT_TRC20
            }

            // 6. Подписываем транзакции
            val (signedTransaction, energyEstimate, bandwidthEstimate) = signTransaction(
                token = transferToken,
                sender = sender,
                receiver = receiver,
                privateKey = privateKey,
                amount = actualAmount,
            )

            val signedCommissionTransaction = signCommissionTransaction(
                commissionAddressInfo = commissionAddressInfo,
                feeAddress = feeAddress,
                commission = commission,
            )

            // 7. Отправляем на сервер и сохраняем локально
            executeTransfer(
                sender = sender,
                receiver = receiver,
                amount = actualAmount,
                energyEstimate = energyEstimate,
                bandwidthEstimate = bandwidthEstimate,
                signedTransaction = signedTransaction,
                commissionAddressInfo = commissionAddressInfo,
                commission = commission,
                signedCommissionTransaction = signedCommissionTransaction,
                token = transferToken,
                senderAddressEntity = senderAddressEntity,
                commissionResult = commissionResult,
            )
        } finally {
            // Обнуляем приватный ключ после использования
            privateKey.fill(0)
        }
    }

    /**
     * Получить информацию об адресе для оплаты комиссии.
     */
    suspend fun getCommissionAddressInfo(
        privateKey: ByteArray,
        tokenName: String,
        addressEntity: WalletAddressLocal,
    ): CommissionAddressInfo {
        val isGeneralAddress = addressLocalRepository.isGeneralAddress(addressEntity.address)
        val centralAddress = centralAddressLocalRepository.get()
            ?: throw TransferError.FeeAddressUnavailable

        return if (tokenName == TokenType.TRX.tokenName || isGeneralAddress) {
            CommissionAddressInfo(
                address = addressEntity.address,
                privateKey = privateKey,
            )
        } else {
            CommissionAddressInfo(
                address = centralAddress.address,
                privateKey = ByteUtils.parseHex(centralAddress.privateKey),
            )
        }
    }

    private suspend fun calculateActualAmount(
        receiver: String,
        tokenName: String,
        amount: BigInteger,
        commission: BigInteger,
    ): BigInteger {
        val isReceiverActivated = tron.addressUtilities.isAddressActivated(receiver)

        return when {
            !isReceiverActivated && tokenName == TokenType.TRX.tokenName ->
                amount - tron.addressUtilities.getCreateNewAccountFeeInSystemContract() - commission
            isReceiverActivated && tokenName == TokenType.TRX.tokenName ->
                amount - commission
            else -> amount
        }
    }

    private suspend fun getFeeAddress(): String? =
        configRepository.getFeeConfiguration().fold(
            onSuccess = { it.trxFeeAddress },
            onFailure = {
                // TODO: Добавить логирование
                null
            },
        )

    private suspend fun signTransaction(
        token: TransferToken,
        sender: String,
        receiver: String,
        privateKey: ByteArray,
        amount: BigInteger,
    ): Triple<SignedTransactionData, EnergyEstimate, BandwidthEstimate> =
        withContext(dispatcher) {
            var energy = EnergyEstimate(0, BigInteger.ZERO)
            var bandwidth: BandwidthEstimate

            val signedTransaction = when (token) {
                TransferToken.USDT_TRC20 -> {
                    energy = tron.transactions.estimateEnergy(sender, receiver, privateKey, amount)
                    bandwidth = tron.transactions.estimateBandwidth(sender, receiver, privateKey, amount)
                    tron.transactions.getSignedUsdtTransaction(sender, receiver, privateKey, amount)
                }
                TransferToken.TRX -> {
                    bandwidth = tron.transactions.estimateBandwidthTrx(sender, receiver, privateKey, amount)
                    tron.transactions.getSignedTrxTransaction(sender, receiver, privateKey, amount)
                }
            }

            Triple(signedTransaction, energy, bandwidth)
        }

    private suspend fun signCommissionTransaction(
        commissionAddressInfo: CommissionAddressInfo,
        feeAddress: String,
        commission: BigInteger,
    ): SignedCommissionTransaction = withContext(dispatcher) {
        val signedTransaction = tron.transactions.getSignedTrxTransaction(
            fromAddress = commissionAddressInfo.address,
            toAddress = feeAddress,
            privateKey = commissionAddressInfo.privateKey,
            amount = commission,
        )

        val bandwidthEstimate = tron.transactions.estimateBandwidthTrx(
            fromAddress = commissionAddressInfo.address,
            toAddress = feeAddress,
            privateKey = commissionAddressInfo.privateKey,
            amount = commission,
        )

        SignedCommissionTransaction(signedTransaction, bandwidthEstimate)
    }

    private suspend fun executeTransfer(
        sender: String,
        receiver: String,
        amount: BigInteger,
        energyEstimate: EnergyEstimate,
        bandwidthEstimate: BandwidthEstimate,
        signedTransaction: SignedTransactionData,
        commissionAddressInfo: CommissionAddressInfo,
        commission: BigInteger,
        signedCommissionTransaction: SignedCommissionTransaction,
        token: TransferToken,
        senderAddressEntity: WalletAddressLocal,
        commissionResult: EstimateCommissionResult,
    ): TransferUiResult = withContext(dispatcher) {
        try {
            val userId = profileLocalRepository.getUserId()

            val transferParams = CreateTransferParams(
                userId = userId,
                txId = signedTransaction.txid,
                token = token,
                transactionData = TransactionData(
                    address = sender,
                    receiverAddress = receiver,
                    amount = amount.toString(),
                    bandwidthRequired = if (tron.accounts.hasEnoughBandwidth(sender, bandwidthEstimate.bandwidth)) {
                        0
                    } else {
                        bandwidthEstimate.bandwidth
                    },
                    estimateEnergy = energyEstimate.energy,
                    txnBytes = signedTransaction.signedTxn!!.toBase64(),
                ),
                commissionData = TransferCommissionData(
                    address = commissionAddressInfo.address,
                    amount = commission.toString(),
                    bandwidthRequired = if (tron.accounts.hasEnoughBandwidth(
                            commissionAddressInfo.address,
                            signedCommissionTransaction.bandwidthEstimate.bandwidth,
                        )
                    ) 0 else signedCommissionTransaction.bandwidthEstimate.bandwidth,
                    categories = commissionResult.categories,
                    txnBytes = signedCommissionTransaction.signedTransaction.signedTxn!!.toBase64(),
                ),
            )

            val result = transferRepository.createTransfer(transferParams)

            result.fold(
                onSuccess = {
                    savePendingTransaction(signedTransaction.txid, amount, token, senderAddressEntity)
                    saveTransactionToDatabase(sender, receiver, amount, commission, token, signedTransaction.txid)
                    TransferUiResult.Success
                },
                onFailure = { exception ->
                    TransferUiResult.Failure(TransferError.ServerError(exception))
                },
            )
        } catch (e: Exception) {
            TransferUiResult.Failure(TransferError.Unknown(e))
        }
    }

    private suspend fun savePendingTransaction(
        txId: String,
        amount: BigInteger,
        token: TransferToken,
        senderAddressEntity: WalletAddressLocal,
    ) {
        val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
        val tokenId = tokenLocalRepository.getTokenId(
            senderAddressEntity.id,
            tokenType,
        )

        pendingTransactionLocalRepository.insert(
            PendingTransactionLocal(
                tokenId = tokenId,
                txId = txId,
                amount = amount,
            ),
        )
    }

    private suspend fun saveTransactionToDatabase(
        sender: String,
        receiver: String,
        amount: BigInteger,
        commission: BigInteger,
        token: TransferToken,
        txId: String,
    ) {
        try {
            val senderAddressEntity = addressLocalRepository.getByAddress(sender)
            val receiverAddressEntity = addressLocalRepository.getByAddress(receiver)

            val transactionAddressEntity = senderAddressEntity ?: receiverAddressEntity
            ?: return

            val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"

            transactionLocalRepository.insert(
                Transaction(
                    id = 0L,
                    txId = txId,
                    senderAddressId = senderAddressEntity?.id,
                    receiverAddressId = receiverAddressEntity?.id,
                    senderAddress = sender,
                    receiverAddress = receiver,
                    walletId = transactionAddressEntity.walletId,
                    tokenName = tokenType,
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    status = "Success",
                    isProcessed = false,
                    type = when {
                        senderAddressEntity?.id != null && receiverAddressEntity?.id != null -> TransactionType.BETWEEN_YOURSELF
                        senderAddressEntity?.id != null -> TransactionType.SEND
                        else -> TransactionType.RECEIVE
                    },
                    statusCode = TransactionStatusCode.PENDING,
                    commission = commission,
                ),
            )
        } catch (_: SQLiteConstraintException) {
            // Транзакция уже существует
        }
    }

    /**
     * Информация об адресе для оплаты комиссии.
     */
    data class CommissionAddressInfo(
        val address: String,
        val privateKey: ByteArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as CommissionAddressInfo
            return address == other.address && privateKey.contentEquals(other.privateKey)
        }

        override fun hashCode(): Int = 31 * address.hashCode() + privateKey.contentHashCode()
    }

    private data class SignedCommissionTransaction(
        val signedTransaction: SignedTransactionData,
        val bandwidthEstimate: BandwidthEstimate,
    )
}
