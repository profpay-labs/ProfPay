package com.profpay.wallet.presentation.viewmodel.wallet.walletSot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.data.transfer.service.TransactionProcessor
import com.profpay.domain.security.PrivateKeyProvider
import com.profpay.domain.transfer.model.EstimateCommissionParams
import com.profpay.domain.transfer.model.EstimateCommissionResult
import com.profpay.domain.transfer.model.TransferUiResult
import com.profpay.domain.transfer.repository.TransferRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.domain.wallet.model.local.TokenBalanceLocal
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.wallet.presentation.viewmodel.dto.transfer.CommissionUiState
import com.profpay.wallet.presentation.viewmodel.dto.transfer.TransferErrorMapper
import com.profpay.wallet.presentation.viewmodel.dto.transfer.TransferUiEvent
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
class GeneralTransactionViewModel @Inject constructor(
    private val addressLocalRepository: AddressLocalRepository,
    private val profileLocalRepository: ProfileLocalRepository,
    private val transferRepository: TransferRepository,
    private val transactionProcessor: TransactionProcessor,
    private val privateKeyProvider: PrivateKeyProvider,
    private val tron: Tron,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _commissionState = MutableStateFlow<CommissionUiState>(CommissionUiState.Idle)
    val commissionState: StateFlow<CommissionUiState> = _commissionState.asStateFlow()

    private val _isGeneralAddressNotActivatedVisible = MutableStateFlow(false)
    val isGeneralAddressNotActivatedVisible: StateFlow<Boolean> = _isGeneralAddressNotActivatedVisible

    private val _generalAddressActivatedCommission = MutableStateFlow<BigInteger?>(null)
    val generalAddressActivatedCommission: StateFlow<BigInteger?> = _generalAddressActivatedCommission

    private val _uiEventTransfer = MutableStateFlow<TransferUiEvent>(TransferUiEvent.Idle)
    val uiEventTransfer: StateFlow<TransferUiEvent> = _uiEventTransfer.asStateFlow()

    fun prepareTransaction(
        walletId: Long,
        addressWithTokens: AddressWithTokensLocal,
        tokenEntity: TokenBalanceLocal?,
        balance: BigInteger?,
    ) = viewModelScope.launch(ioDispatcher) {
        _commissionState.value = CommissionUiState.Loading

        val generalAddress = addressLocalRepository.getGeneralAddressByWalletId(walletId)
        val privKeyBytes = privateKeyProvider.resolve(addressWithTokens.address)

        try {
            val amount = balance ?: tokenEntity?.availableBalance
            ?: run {
                _commissionState.value = CommissionUiState.Error("Баланс не определён")
                return@launch
            }

            val requiredEnergy = tron.transactions.estimateEnergy(
                fromAddress = addressWithTokens.address.address,
                toAddress = generalAddress,
                privateKey = privKeyBytes,
                amount = amount,
            )

            val requiredBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = addressWithTokens.address.address,
                toAddress = generalAddress,
                privateKey = privKeyBytes,
                amount = amount,
            )

            // Проверяем активацию general address
            if (!tron.addressUtilities.isAddressActivated(generalAddress)) {
                val activationFee = tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
                _isGeneralAddressNotActivatedVisible.value = true
                _generalAddressActivatedCommission.value = activationFee
            }

            // One-shot запрос комиссии
            val commissionResult = estimateCommission(
                address = addressWithTokens.address.address,
                bandwidth = requiredBandwidth.bandwidth,
                energy = requiredEnergy.energy,
            )

            commissionResult.fold(
                onSuccess = { result ->
                    _commissionState.value = CommissionUiState.Success(result)
                },
                onFailure = { error ->
                    Sentry.captureException(error)
                    _commissionState.value = CommissionUiState.Error(
                        error.message ?: "Ошибка расчёта комиссии"
                    )
                },
            )
        } finally {
            privKeyBytes.fill(0)
        }
    }

    /**
     * One-shot запрос комиссии через TransferRepository.
     */
    private suspend fun estimateCommission(
        address: String,
        bandwidth: Long,
        energy: Long,
    ): Result<EstimateCommissionResult> {
        val userId = profileLocalRepository.getUserId()
        return transferRepository.estimateCommission(
            EstimateCommissionParams(
                userId = userId,
                address = address,
                energyRequired = energy,
                bandwidthRequired = bandwidth,
            ),
        )
    }

    fun onConfirmTransaction(
        addressWithTokens: AddressWithTokensLocal,
        commission: BigInteger,
        walletId: Long,
        tokenEntity: TokenBalanceLocal?,
        amount: BigInteger,
        commissionResult: EstimateCommissionResult,
    ) = viewModelScope.launch(ioDispatcher) {
        val generalAddress = addressLocalRepository.getGeneralAddressByWalletId(walletId)

        when (val result = transactionProcessor.sendTransaction(
            sender = addressWithTokens.address.address,
            receiver = generalAddress,
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
