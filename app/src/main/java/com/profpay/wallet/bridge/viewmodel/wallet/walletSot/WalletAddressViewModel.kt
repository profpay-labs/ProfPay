package com.profpay.wallet.bridge.viewmodel.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.converter.toSunAmount
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.data.transfer.service.TransactionProcessor
import com.profpay.domain.security.PrivateKeyProvider
import com.profpay.domain.transfer.model.EstimateCommissionParams
import com.profpay.domain.transfer.model.EstimateCommissionResult
import com.profpay.domain.transfer.model.TransferUiResult
import com.profpay.domain.transfer.repository.TransferRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.ActiveWalletManager
import com.profpay.domain.wallet.model.TransactionSummary
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.model.local.TokenBalanceLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.TransactionLocalRepository
import com.profpay.wallet.bridge.viewmodel.dto.transfer.CommissionUiState
import com.profpay.wallet.bridge.viewmodel.dto.transfer.TransferErrorMapper
import com.profpay.wallet.bridge.viewmodel.dto.transfer.TransferUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletAddressViewModel @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val transactionLocalRepository: TransactionLocalRepository,
    private val profileLocalRepository: ProfileLocalRepository,
    private val transferRepository: TransferRepository,
    private val transactionProcessor: TransactionProcessor,
    private val privateKeyProvider: PrivateKeyProvider,
    private val activeWalletManager: ActiveWalletManager,
    private val tron: Tron,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    // ══════════════════════════════════════════════════════════════════════
    // UI States (заменяют callback'и)
    // ══════════════════════════════════════════════════════════════════════

    private val _isActivated = MutableStateFlow<Boolean?>(null)
    val isActivated: StateFlow<Boolean?> = _isActivated.asStateFlow()

    private val _isGeneralAddress = MutableStateFlow<Boolean?>(null)
    val isGeneralAddress: StateFlow<Boolean?> = _isGeneralAddress.asStateFlow()

    private val _commissionState = MutableStateFlow<CommissionUiState>(CommissionUiState.Idle)
    val commissionState: StateFlow<CommissionUiState> = _commissionState.asStateFlow()

    private val _uiEventTransfer = MutableStateFlow<TransferUiEvent>(TransferUiEvent.Idle)
    val uiEventTransfer: StateFlow<TransferUiEvent> = _uiEventTransfer.asStateFlow()

    // ══════════════════════════════════════════════════════════════════════
    // Actions
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Проверить активацию адреса.
     * Результат доступен через [isActivated] StateFlow.
     */
    fun checkActivation(address: String) = viewModelScope.launch(ioDispatcher) {
        _isActivated.value = tron.addressUtilities.isAddressActivated(address)
    }

    /**
     * Проверить, является ли адрес general address.
     * Результат доступен через [isGeneralAddress] StateFlow.
     */
    fun checkIsGeneralAddress(address: String) = viewModelScope.launch(ioDispatcher) {
        _isGeneralAddress.value = addressLocalRepository.isGeneralAddress(address)
    }

    /**
     * Сбросить состояния проверок (например, при смене адреса).
     */
    fun resetAddressChecks() {
        _isActivated.value = null
        _isGeneralAddress.value = null
    }

    fun requestCommission(
        addressWithTokens: AddressWithTokensLocal,
        tokenName: String,
        valueAmount: String,
        addressSending: String,
    ) = viewModelScope.launch(ioDispatcher) {
        if (valueAmount.isEmpty() || !tron.addressUtilities.isValidTronAddress(addressSending)) {
            return@launch
        }

        _commissionState.value = CommissionUiState.Loading

        val privKeyBytes = privateKeyProvider.resolve(addressWithTokens.address)

        try {
            val amount = valueAmount.toBigDecimal().toSunAmount()

            val requiredEnergy = tron.transactions.estimateEnergy(
                fromAddress = addressWithTokens.address.address,
                toAddress = addressSending,
                privateKey = privKeyBytes,
                amount = amount,
            )

            val requiredBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = addressWithTokens.address.address,
                toAddress = addressSending,
                privateKey = privKeyBytes,
                amount = amount,
            )

            val hasEnoughBandwidth = tron.accounts.hasEnoughBandwidth(
                addressWithTokens.address.address,
                requiredBandwidth.bandwidth,
            )

            val userId = profileLocalRepository.getUserId()
            val result = transferRepository.estimateCommission(
                EstimateCommissionParams(
                    userId = userId,
                    address = addressWithTokens.address.address,
                    energyRequired = if (tokenName == "TRX") 0 else requiredEnergy.energy,
                    bandwidthRequired = if (hasEnoughBandwidth) 0 else requiredBandwidth.bandwidth,
                ),
            )

            result.fold(
                onSuccess = { commissionResult ->
                    _commissionState.value = CommissionUiState.Success(commissionResult)
                },
                onFailure = { error ->
                    Sentry.captureException(error)
                    _commissionState.value = CommissionUiState.Error(
                        error.message ?: "Ошибка расчёта комиссии",
                    )
                },
            )
        } catch (e: NumberFormatException) {
            Sentry.captureException(e)
            _commissionState.value = CommissionUiState.Error("Некорректная сумма")
        } finally {
            privKeyBytes.fill(0)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Data loading (LiveData для обратной совместимости с XML/старым кодом)
    // ══════════════════════════════════════════════════════════════════════

    fun getAddressWithTokensByAddress(address: String): LiveData<AddressWithTokensLocal> =
        liveData(ioDispatcher) {
            emitSource(addressLocalRepository.observeAddressWithTokensByAddress(address).asLiveData())
        }

    fun getTransactionsByAddressAndTokenLD(
        address: String,
        tokenName: String,
        isSender: Boolean,
        isCentralAddress: Boolean,
    ): LiveData<List<TransactionSummary>> {
        val walletId = activeWalletManager.activeWalletId
        return liveData(ioDispatcher) {
            emitSource(
                transactionLocalRepository.observeByAddressAndToken(
                    walletId = walletId,
                    address = address,
                    tokenName = tokenName,
                    isSender = isSender,
                    isCentralAddress = isCentralAddress,
                ).asLiveData(),
            )
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Pure functions (без side-эффектов)
    // ══════════════════════════════════════════════════════════════════════

    fun getListTransactionToTimestamp(listTransactions: List<TransactionSummary>): List<List<TransactionSummary>> {
        if (listTransactions.isEmpty()) return emptyList()
        return listTransactions
            .sortedByDescending { it.timestamp }
            .groupBy { it.transactionDate }
            .values
            .toList()
    }

    fun isValidTronAddress(address: String): Boolean =
        tron.addressUtilities.isValidTronAddress(address)

    // ══════════════════════════════════════════════════════════════════════
    // Transaction actions
    // ══════════════════════════════════════════════════════════════════════

    fun onClickedReject(
        toAddress: String,
        addressWithTokens: AddressWithTokensLocal,
        amount: BigInteger,
        commission: BigInteger,
        tokenEntity: TokenBalanceLocal?,
        commissionResult: EstimateCommissionResult,
    ) = viewModelScope.launch(ioDispatcher) {
        when (val result = transactionProcessor.sendTransaction(
            sender = addressWithTokens.address.address,
            receiver = toAddress,
            amount = amount,
            commission = commission,
            tokenEntity = tokenEntity,
            commissionResult = commissionResult,
        )) {
            is TransferUiResult.Success -> {
                _uiEventTransfer.emit(TransferUiEvent.Success)
            }
            is TransferUiResult.Failure -> {
                val userMessage = TransferErrorMapper.toUserMessage(result.error)
                _uiEventTransfer.emit(
                    TransferUiEvent.Error(
                        title = "Ошибка перевода",
                        message = userMessage,
                    ),
                )
            }
        }
    }
}
