package com.profpay.wallet.presentation.viewmodel.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.protobuf.ByteString
import com.profpay.core.common.converter.toSunAmount
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.common.ext.toByteString
import com.profpay.core.crypto.util.ByteUtils
import com.profpay.core.crypto.util.toBase64
import com.profpay.core.tron.Tron
import com.profpay.domain.aml.exception.AmlError
import com.profpay.domain.aml.model.local.PendingAmlTransactionLocal
import com.profpay.domain.aml.repository.PendingAmlTransactionLocalRepository
import com.profpay.domain.aml.usecase.DownloadAmlPdfUseCase
import com.profpay.domain.aml.usecase.GetAmlReportUseCase
import com.profpay.domain.aml.usecase.ProcessAmlPaymentUseCase
import com.profpay.domain.config.repository.ConfigRepository
import com.profpay.domain.market.model.BinanceSymbol
import com.profpay.domain.market.repository.ExchangeRatesLocalRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.ActiveWalletManager
import com.profpay.domain.wallet.model.Transaction
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import com.profpay.domain.wallet.repository.local.WalletProfileLocalRepository
import com.profpay.wallet.presentation.viewmodel.wallet.aml.AmlPaymentUiEvent
import com.profpay.wallet.presentation.viewmodel.wallet.aml.AmlUiState
import com.profpay.wallet.presentation.viewmodel.wallet.aml.PdfDownloadUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TXDetailsViewModel @Inject constructor(
    private val getAmlReportUseCase: GetAmlReportUseCase,
    private val processAmlPaymentUseCase: ProcessAmlPaymentUseCase,
    private val downloadAmlPdfUseCase: DownloadAmlPdfUseCase,
    private val walletProfileLocalRepository: WalletProfileLocalRepository,
    private val profileLocalRepository: ProfileLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val exchangeRatesLocalRepository: ExchangeRatesLocalRepository,
    private val tron: Tron,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val pendingAmlTransactionLocalRepository: PendingAmlTransactionLocalRepository,
    private val activeWalletManager: ActiveWalletManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val _amlState = MutableStateFlow<AmlUiState>(AmlUiState.Idle)
    val amlState: StateFlow<AmlUiState> = _amlState.asStateFlow()

    private val _amlPaymentEvent = MutableStateFlow<AmlPaymentUiEvent>(AmlPaymentUiEvent.Idle)
    val amlPaymentEvent: StateFlow<AmlPaymentUiEvent> = _amlPaymentEvent.asStateFlow()

    private val _pdfDownloadEvent = MutableStateFlow<PdfDownloadUiEvent>(PdfDownloadUiEvent.Idle)
    val pdfDownloadEvent: StateFlow<PdfDownloadUiEvent> = _pdfDownloadEvent.asStateFlow()

    private val _amlFeeResult = MutableStateFlow<ByteString?>(null)
    val amlFeeResult: StateFlow<ByteString?> = _amlFeeResult.asStateFlow()

    private val _amlIsPending = MutableStateFlow(false)
    val amlIsPending: StateFlow<Boolean> = _amlIsPending.asStateFlow()

    private val _walletName = MutableStateFlow<String?>(null)
    val walletName: StateFlow<String?> = _walletName.asStateFlow()

    private val _trxUsdtRate = MutableStateFlow(0.0)
    val trxUsdtRate: StateFlow<Double> = _trxUsdtRate.asStateFlow()

    init {
        loadAmlFee()
    }

    fun loadAmlReport(
        address: String,
        txId: String,
        tokenName: String,
    ) = viewModelScope.launch(ioDispatcher) {
        _amlState.value = AmlUiState.Loading

        val userId = profileLocalRepository.getUserId()

        getAmlReportUseCase(
            userId = userId,
            address = address,
            txHash = txId,
            tokenName = tokenName,
        ).fold(
            onSuccess = { report ->
                _amlState.value = AmlUiState.Success(report)
            },
            onFailure = { error ->
                _amlState.value = AmlUiState.Error(mapAmlErrorToMessage(error))
                Sentry.captureException(error)
            },
        )
    }

    fun processAmlPayment(
        address: String,
        txId: String,
    ) = viewModelScope.launch(ioDispatcher) {
        _amlPaymentEvent.value = AmlPaymentUiEvent.Loading

        val centralAddress = centralAddressLocalRepository.get()
        if (centralAddress == null) {
            _amlPaymentEvent.value = AmlPaymentUiEvent.Error(
                title = "Ошибка",
                message = "Не удалось получить central address",
            )
            return@launch
        }

        val balance = tron.addressUtilities.getTrxBalance(centralAddress.address)
        val userId = profileLocalRepository.getUserId()

        val serverConfig = configRepository.getFeeConfiguration().fold(
            onSuccess = { it },
            onFailure = {
                Sentry.captureException(it)
                _amlPaymentEvent.value = AmlPaymentUiEvent.Error(
                    title = "Ошибка",
                    message = "Сервер недоступен",
                )
                return@launch
            },
        )

        val amlFeeValue = serverConfig.amlFee
        val trxFeeAddress = serverConfig.trxFeeAddress

        if (balance.toTokenAmount() < amlFeeValue.toBigInteger().toTokenAmount()) {
            _amlPaymentEvent.value = AmlPaymentUiEvent.Error(
                title = "Недостаточно средств",
                message = "Необходимо: ${amlFeeValue.toBigInteger().toTokenAmount()} TRX",
            )
            return@launch
        }

        val privateKey = ByteUtils.parseHex(centralAddress.privateKey)

        try {
            val signedTxnBytes = withContext(ioDispatcher) {
                tron.transactions.getSignedTrxTransaction(
                    fromAddress = centralAddress.address,
                    toAddress = trxFeeAddress,
                    privateKey = privateKey,
                    amount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount(),
                )
            }

            val estimateBandwidth = withContext(ioDispatcher) {
                tron.transactions.estimateBandwidth(
                    fromAddress = centralAddress.address,
                    toAddress = trxFeeAddress,
                    privateKey = privateKey,
                    amount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount(),
                )
            }

            val txnBytes = signedTxnBytes.signedTxn?.toBase64()
            if (txnBytes == null) {
                _amlPaymentEvent.value = AmlPaymentUiEvent.Error(
                    title = "Ошибка",
                    message = "Не удалось подписать транзакцию",
                )
                return@launch
            }

            processAmlPaymentUseCase(
                ProcessAmlPaymentUseCase.Params(
                    userId = userId,
                    txHash = txId,
                    address = address,
                    paymentAddress = trxFeeAddress,
                    bandwidthRequired = estimateBandwidth.bandwidth,
                    txnBytes = txnBytes,
                ),
            ).fold(
                onSuccess = {
                    pendingAmlTransactionLocalRepository.insert(PendingAmlTransactionLocal(txId = txId))
                    _amlPaymentEvent.value = AmlPaymentUiEvent.Success(
                        message = "Успешное действие, ожидайте уведомление.",
                    )
                },
                onFailure = { error ->
                    Sentry.captureException(error)
                    _amlPaymentEvent.value = AmlPaymentUiEvent.Error(
                        title = "Ошибка запроса",
                        message = mapAmlErrorToMessage(error),
                    )
                },
            )
        } finally {
            privateKey.fill(0) // Очищаем приватный ключ
        }
    }

    fun resetAmlPaymentEvent() {
        _amlPaymentEvent.value = AmlPaymentUiEvent.Idle
    }

    fun downloadPdf(txId: String) = viewModelScope.launch(ioDispatcher) {
        _pdfDownloadEvent.value = PdfDownloadUiEvent.Loading

        val userId = profileLocalRepository.getUserId()

        downloadAmlPdfUseCase(userId, txId).fold(
            onSuccess = { pdfBytes ->
                _pdfDownloadEvent.value = PdfDownloadUiEvent.Success(pdfBytes)
            },
            onFailure = { error ->
                Sentry.captureException(error)
                _pdfDownloadEvent.value = PdfDownloadUiEvent.Error(
                    message = "Не удалось скачать PDF",
                )
            },
        )
    }

    fun resetPdfDownloadEvent() {
        _pdfDownloadEvent.value = PdfDownloadUiEvent.Idle
    }

    fun loadExchangeRate() = viewModelScope.launch(ioDispatcher) {
        _trxUsdtRate.value = exchangeRatesLocalRepository.getRate(BinanceSymbol.TRX_USDT.symbol)
    }

    fun checkAmlIsPending(txId: String) = viewModelScope.launch(ioDispatcher) {
        _amlIsPending.value = pendingAmlTransactionLocalRepository.exists(txId)
    }

    fun getWalletNameById() = viewModelScope.launch(ioDispatcher) {
        val walletId = activeWalletManager.activeWalletId
        _walletName.value = walletProfileLocalRepository.getNameById(walletId)
    }

    fun getTransactionLiveDataById(transactionId: Long): LiveData<Transaction> =
        liveData(ioDispatcher) {
            emitSource(transactionLocalRepository.observeById(transactionId).asLiveData())
        }

    private fun loadAmlFee() = viewModelScope.launch(ioDispatcher) {
        configRepository.getFeeConfiguration().fold(
            onSuccess = { config ->
                _amlFeeResult.value = config.amlFee.toBigInteger().toByteString()
            },
            onFailure = { Sentry.captureException(it) },
        )
    }

    private fun mapAmlErrorToMessage(error: Throwable): String = when (error) {
        is AmlError.ReportNotFound -> "AML отчёт не найден"
        is AmlError.ProviderUnavailable -> "Сервис AML временно недоступен"
        is AmlError.RenewCooldownNotExpired -> "Обновление отчёта доступно раз в 24 часа"
        is AmlError.InvalidPaymentRequest -> "Некорректные данные платежа"
        is AmlError.ResourcePurchaseFailed -> "Не удалось приобрести ресурсы"
        is AmlError.InvalidTransaction -> "Некорректная транзакция: ${error.reason}"
        else -> error.message ?: "Неизвестная ошибка"
    }
}
