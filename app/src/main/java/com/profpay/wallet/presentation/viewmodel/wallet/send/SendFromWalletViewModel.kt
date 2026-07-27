package com.profpay.wallet.presentation.viewmodel.wallet.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.converter.toSunAmount
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.data.transfer.service.TransactionProcessor
import com.profpay.domain.market.model.BinanceSymbol
import com.profpay.domain.market.repository.ExchangeRatesLocalRepository
import com.profpay.domain.security.PrivateKeyProvider
import com.profpay.domain.transfer.model.EstimateCommissionParams
import com.profpay.domain.transfer.model.EstimateCommissionResult
import com.profpay.domain.transfer.model.TransferUiResult
import com.profpay.domain.transfer.repository.TransferRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.wallet.presentation.viewmodel.dto.TokenName
import com.profpay.wallet.presentation.viewmodel.dto.transfer.CommissionUiState
import com.profpay.wallet.presentation.viewmodel.dto.transfer.TransferErrorMapper
import com.profpay.wallet.presentation.viewmodel.dto.transfer.TransferUiEvent
import com.profpay.wallet.ui.feature.wallet.send.bottomsheet.ModelTransferFromBS
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

data class TransferUiState(
    val addressWithTokens: AddressWithTokensLocal? = null,
    val isAddressActivated: Boolean = true,
    val isValidRecipientAddress: Boolean = true,
    val isEnoughBalance: Boolean = true,
    val warning: String? = null,
    val commission: BigDecimal = BigDecimal.ZERO,
    val commissionResult: EstimateCommissionResult? = null,
    val tokenBalance: BigInteger = BigInteger.ZERO,
    val isButtonEnabled: Boolean = false,
)

@HiltViewModel
class SendFromWalletViewModel @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val profileLocalRepository: ProfileLocalRepository,
    private val transferRepository: TransferRepository,
    private val transactionProcessor: TransactionProcessor,
    private val privateKeyProvider: PrivateKeyProvider,
    private val exchangeRatesLocalRepository: ExchangeRatesLocalRepository,
    private val tron: Tron,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _commissionState = MutableStateFlow<CommissionUiState>(CommissionUiState.Idle)
    val commissionState: StateFlow<CommissionUiState> = _commissionState.asStateFlow()

    private val _transferEvent = MutableStateFlow<TransferUiEvent>(TransferUiEvent.Idle)
    val transferEvent: StateFlow<TransferUiEvent> = _transferEvent.asStateFlow()

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _trxToUsdtRate = MutableStateFlow(BigDecimal.ZERO)
    val trxToUsdtRate: StateFlow<BigDecimal> = _trxToUsdtRate.asStateFlow()

    private val _isReceiverActivated = MutableStateFlow(true)
    val isReceiverActivated: StateFlow<Boolean> = _isReceiverActivated.asStateFlow()

    private val _isContractAddress = MutableStateFlow(false)
    val isContractAddress: StateFlow<Boolean> = _isContractAddress.asStateFlow()

    private val _createNewAccountFee = MutableStateFlow(BigInteger.ZERO)
    val createNewAccountFee: StateFlow<BigInteger> = _createNewAccountFee.asStateFlow()

    fun consumeTransferEvent() {
        _transferEvent.value = TransferUiEvent.Idle
    }

    /**
     * Проверить активацию адреса получателя.
     * Результат доступен через [isReceiverActivated].
     * Если адрес не активирован — загружает комиссию за создание нового аккаунта.
     */
    fun checkReceiverActivation(address: String) = viewModelScope.launch(ioDispatcher) {
        val isActivated = tron.addressUtilities.isAddressActivated(address)
        _isReceiverActivated.value = isActivated

        if (!isActivated) {
            _createNewAccountFee.value = tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
        } else {
            _createNewAccountFee.value = BigInteger.ZERO
        }
    }

    /**
     * Проверить, является ли адрес контрактом.
     * Результат доступен через [isContractAddress].
     */
    fun checkIsContractAddress(address: String) = viewModelScope.launch(ioDispatcher) {
        _isContractAddress.value = tron.addressUtilities.isContractAddress(address)
    }

    /**
     * Проверить валидность TRON-адреса (синхронно, pure function).
     */
    fun isValidTronAddress(address: String): Boolean =
        tron.addressUtilities.isValidTronAddress(address)

    fun loadAddressWithTokens(
        addressId: Long,
        blockchain: String,
        tokenName: String,
    ) = viewModelScope.launch(ioDispatcher) {
        val addressWithTokens = addressLocalRepository.getGeneralAddressWithTokens(addressId, blockchain)
        val isActivated = tron.addressUtilities.isAddressActivated(
            addressWithTokens.address.address,
        )
        val token = addressWithTokens.tokens.find { it.tokenName == tokenName }
        val balance = token?.availableBalance?.toTokenAmount() ?: BigDecimal.ZERO

        _uiState.update {
            it.copy(
                addressWithTokens = addressWithTokens,
                isAddressActivated = isActivated,
                tokenBalance = balance.toSunAmount(),
            )
        }
    }

    fun updateInputs(
        addressTo: String,
        sum: String,
        tokenName: TokenName,
    ) = viewModelScope.launch(ioDispatcher) {
        val isValidAddress = tron.addressUtilities.isValidTronAddress(addressTo)
        val addressEntity = _uiState.value.addressWithTokens ?: return@launch
        val token = addressEntity.tokens.find { it.tokenName == tokenName.tokenName }
        val balance = token?.availableBalance?.toTokenAmount() ?: BigDecimal.ZERO
        val amount = sum.toBigDecimalOrNull() ?: BigDecimal.ZERO

        if (isValidAddress && sum.isNotEmpty()) {
            estimateCommissions(addressEntity, sum, addressTo, tokenName)
        }

        val isEnough = amount <= balance

        _uiState.update {
            it.copy(
                isValidRecipientAddress = isValidAddress,
                isEnoughBalance = isEnough,
                isButtonEnabled = isValidAddress && isEnough && amount > BigDecimal.ZERO,
                warning = if (!isEnough) "Недостаточно средств" else null,
            )
        }
    }

    fun loadTrxToUsdtRate() = viewModelScope.launch(ioDispatcher) {
        val rate = exchangeRatesLocalRepository.getRate(BinanceSymbol.TRX_USDT.symbol)
        _trxToUsdtRate.value = rate.toBigDecimal()
    }

    fun onConfirmTransaction(modelTransferFromBS: ModelTransferFromBS) =
        viewModelScope.launch(ioDispatcher) {
            _transferEvent.emit(TransferUiEvent.Loading)

            val tokenEntity = modelTransferFromBS.addressWithTokens?.tokens?.find {
                it.tokenName == modelTransferFromBS.tokenName.tokenName
            }

            when (val result = transactionProcessor.sendTransaction(
                sender = modelTransferFromBS.addressSender,
                receiver = modelTransferFromBS.addressReceiver,
                amount = modelTransferFromBS.amount.toSunAmount(),
                commission = modelTransferFromBS.commission.toSunAmount(),
                tokenEntity = tokenEntity,
                commissionResult = modelTransferFromBS.commissionResult,
            )) {
                is TransferUiResult.Success -> {
                    _transferEvent.emit(TransferUiEvent.Success)
                }
                is TransferUiResult.Failure -> {
                    val userMessage = TransferErrorMapper.toUserMessage(result.error)
                    _transferEvent.emit(
                        TransferUiEvent.Error(
                            title = "Ошибка перевода",
                            message = userMessage,
                        ),
                    )
                }
            }
        }

    // ══════════════════════════════════════════════════════════════════════
    // Private implementation
    // ══════════════════════════════════════════════════════════════════════

    private fun estimateCommissions(
        addressWithTokens: AddressWithTokensLocal,
        sumSending: String,
        addressSending: String,
        tokenNameModel: TokenName,
    ) = viewModelScope.launch(ioDispatcher) {
        if (sumSending.isEmpty() || !tron.addressUtilities.isValidTronAddress(addressSending)) {
            return@launch
        }

        _commissionState.value = CommissionUiState.Loading

        val privKeyBytes = privateKeyProvider.resolve(addressWithTokens.address)

        try {
            val amount = sumSending.toBigDecimal().toSunAmount()

            val requiredBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = addressWithTokens.address.address,
                toAddress = addressSending,
                privateKey = privKeyBytes,
                amount = amount,
            )

            val requiredEnergy = if (tokenNameModel.tokenName == "USDT") {
                tron.transactions.estimateEnergy(
                    fromAddress = addressWithTokens.address.address,
                    toAddress = addressSending,
                    privateKey = privKeyBytes,
                    amount = amount,
                ).energy
            } else {
                0L
            }

            val hasEnoughBandwidth = tron.accounts.hasEnoughBandwidth(
                addressWithTokens.address.address,
                requiredBandwidth.bandwidth,
            )

            val userId = profileLocalRepository.getUserId()
            val result = transferRepository.estimateCommission(
                EstimateCommissionParams(
                    userId = userId,
                    address = addressWithTokens.address.address,
                    energyRequired = requiredEnergy,
                    bandwidthRequired = if (hasEnoughBandwidth) 0 else requiredBandwidth.bandwidth,
                ),
            )

            result.fold(
                onSuccess = { commissionResult ->
                    _commissionState.value = CommissionUiState.Success(commissionResult)
                    _uiState.update {
                        it.copy(
                            commission = commissionResult.commission.toBigInteger().toTokenAmount(),
                            commissionResult = commissionResult,
                            warning = null,
                        )
                    }
                },
                onFailure = { error ->
                    Sentry.captureException(error)
                    _commissionState.value = CommissionUiState.Error(
                        error.message ?: "Ошибка расчёта комиссии",
                    )
                    _uiState.update { it.copy(warning = "Ошибка при расчёте комиссии") }
                },
            )
        } finally {
            privKeyBytes.fill(0)
        }
    }
}
