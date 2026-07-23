package com.profpay.wallet.bridge.viewmodel.smartcontract

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.common.di.IoDispatcher
import com.profpay.core.tron.Tron
import com.profpay.data.contract.repository.DealsStore
import com.profpay.domain.config.repository.ConfigRepository
import com.profpay.domain.contract.model.Deal
import com.profpay.domain.contract.model.local.SmartContractLocal
import com.profpay.domain.contract.repository.SmartContractLocalRepository
import com.profpay.domain.transfer.model.EstimateCommissionParams
import com.profpay.domain.transfer.repository.TransferRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.wallet.repository.local.AddressLocalRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.ProcessSmartContractUseCase
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.estimate.ProcessContractEstimatorUseCase
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.estimate.TransactionEstimatorResult
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.getOppositeUserId
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.isBuyerNotDeposited
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.isBuyerRequestInitialized
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.isContractAwaitingUserConfirmation
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.isDisputeNotAgreed
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.isDisputeNotDeclined
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.isExpertNotDecision
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.isSellerNotPayedExpertFee
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class GetSmartContractViewModel
@Inject
constructor(
    private val transferRepository: TransferRepository,
    private val dealsStore: DealsStore,
    modalStateHolder: SmartContractModalStateHolder,
    val profileLocalRepository: ProfileLocalRepository,
    private val processSmartContractUseCase: ProcessSmartContractUseCase,
    private val processContractEstimatorUseCase: ProcessContractEstimatorUseCase,
    private val addressLocalRepository: AddressLocalRepository,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val smartContractLocalRepository: SmartContractLocalRepository,
    val tron: Tron,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val _isActivated = MutableStateFlow(false)
    val isActivated: StateFlow<Boolean> = _isActivated.asStateFlow()

    private val _generalAddress = MutableStateFlow<String?>(null)
    val generalAddress: StateFlow<String?> = _generalAddress.asStateFlow()

    private val _appId = MutableStateFlow("")
    val appId: StateFlow<String> = _appId.asStateFlow()

    val state: StateFlow<List<Deal>> = dealsStore.deals

    private val _contractBalance = MutableStateFlow(BigInteger.ZERO)
    val contractBalance: StateFlow<BigInteger> = _contractBalance.asStateFlow()

    /** Модалка подтверждения действия (открывается по кнопке на карточке сделки). */
    private val _confirmationModal = MutableStateFlow(SmartContractModalData.Hidden)

    /**
     * Единое состояние модалки для экрана:
     * прогресс операции имеет приоритет над модалкой подтверждения.
     */
    val stateModal: StateFlow<SmartContractModalData> =
        combine(_confirmationModal, modalStateHolder.state) { confirmation, progress ->
            if (progress.isVisible) {
                SmartContractModalData(isActive = true, text = progress.message)
            } else {
                confirmation
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SmartContractModalData.Hidden,
        )

    private val _stateEstimateResourcePrice =
        MutableStateFlow(EstimateResourcePriceResult(commission = 0))
    val stateEstimateResourcePrice: StateFlow<EstimateResourcePriceResult> =
        _stateEstimateResourcePrice.asStateFlow()

    val smartContractLiveData: LiveData<SmartContractLocal?> =
        liveData(ioDispatcher) {
            emitSource(smartContractLocalRepository.observe().asLiveData())
        }

    private fun loadAppId() = viewModelScope.launch(ioDispatcher) {
        _appId.value = profileLocalRepository.getAppId()
    }

    fun loadContractBalance(contractAddress: String) = viewModelScope.launch(ioDispatcher) {
        _contractBalance.value = tron.addressUtilities.getUsdtBalance(contractAddress)
    }

    init {
        refreshDeals()
        loadGeneralAddress()
        loadAppId()
    }

    private fun loadGeneralAddress() = viewModelScope.launch(ioDispatcher) {
        _generalAddress.value = addressLocalRepository.getGeneralAddressByWalletId(1L)
    }

    fun refreshDeals() {
        viewModelScope.launch(ioDispatcher) {
            dealsStore.refresh()
        }
    }

    fun checkActivation(address: String) =
        viewModelScope.launch {
            _isActivated.value =
                withContext(ioDispatcher) {
                    tron.addressUtilities.isAddressActivated(address)
                }
        }

    fun setSmartContractModalActive(
        isActive: Boolean,
        buttonType: SmartContractButtonType?,
        deal: Deal?,
    ) {
        _confirmationModal.value =
            if (isActive) {
                SmartContractModalData(
                    isActive = true,
                    text = "",
                    buttonType = buttonType,
                    deal = deal,
                )
            } else {
                SmartContractModalData.Hidden
            }
    }

    fun getResourceQuote(
        energy: Long,
        bandwidth: Long,
    ) = viewModelScope.launch(ioDispatcher) {
        val address = addressLocalRepository.getGeneralAddressByWalletId(1L)
        val userId = profileLocalRepository.getUserId()

        transferRepository.estimateCommission(
            EstimateCommissionParams(
                userId = userId,
                address = address,
                energyRequired = energy,
                bandwidthRequired = bandwidth,
            ),
        ).onSuccess { result ->
            _stateEstimateResourcePrice.value =
                EstimateResourcePriceResult(commission = result.commission.toLong())
        }.onFailure {
            Sentry.captureException(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun completeContract(
        commission: BigDecimal,
        deal: Deal,
    ): CompleteReturnData {
        val result =
            processSmartContractUseCase.processCompleteSmartContract(
                commission = commission,
                deal = deal,
            )
        onContractActionFinished()
        return result
    }

    suspend fun rejectContract(
        commission: BigDecimal,
        deal: Deal,
    ): CompleteReturnData {
        val result =
            processSmartContractUseCase.processRejectSmartContract(
                commission = commission,
                deal = deal,
            )
        onContractActionFinished()
        return result
    }

    suspend fun estimateCompleteContract(deal: Deal): TransactionEstimatorResult? =
        processContractEstimatorUseCase.processCompleteSmartContract(deal = deal)

    suspend fun estimateRejectContract(deal: Deal): TransactionEstimatorResult? =
        processContractEstimatorUseCase.processRejectSmartContract(deal = deal)

    suspend fun expertSetDecision(
        deal: Deal,
        sellerValue: BigInteger,
        buyerValue: BigInteger,
    ) {
        processSmartContractUseCase.expertSetDecision(
            deal = deal,
            sellerValue = sellerValue,
            buyerValue = buyerValue,
        )
        onContractActionFinished()
    }

    /** Закрыть модалку подтверждения и обновить список сделок после операции. */
    private suspend fun onContractActionFinished() {
        _confirmationModal.value = SmartContractModalData.Hidden
        dealsStore.refresh()
    }

    suspend fun isButtonVisible(deal: Deal): ContractButtonVisibleType {
        val userId = profileLocalRepository.getUserId()
        return when {
            isBuyerRequestInitialized(deal, userId) ->
                ContractButtonVisibleType(agreeVisible = true, cancelVisible = true)
            isBuyerNotDeposited(deal, userId) ->
                ContractButtonVisibleType(agreeVisible = true, cancelVisible = true)
            isContractAwaitingUserConfirmation(deal, userId) ->
                ContractButtonVisibleType(agreeVisible = true, cancelVisible = true)
            isSellerNotPayedExpertFee(deal, userId) ->
                ContractButtonVisibleType(agreeVisible = true, cancelVisible = true)
            isExpertNotDecision(deal, userId) ->
                ContractButtonVisibleType(agreeVisible = true, cancelVisible = false)
            isDisputeNotAgreed(deal, userId) && isDisputeNotDeclined(deal, userId) ->
                ContractButtonVisibleType(agreeVisible = true, cancelVisible = true)
            !isDisputeNotAgreed(deal, userId) && isDisputeNotDeclined(deal, userId) ->
                ContractButtonVisibleType(agreeVisible = false, cancelVisible = true)
            isDisputeNotAgreed(deal, userId) && !isDisputeNotDeclined(deal, userId) ->
                ContractButtonVisibleType(agreeVisible = true, cancelVisible = false)
            else -> ContractButtonVisibleType(agreeVisible = false, cancelVisible = false)
        }
    }

    suspend fun getOppositeTelegramId(deal: Deal): Long {
        val userId = profileLocalRepository.getUserId()
        return if (deal.buyer.userId == userId) deal.seller.telegramId else deal.buyer.telegramId
    }

    suspend fun getOppositeUsername(deal: Deal): String {
        val userId = profileLocalRepository.getUserId()
        return if (deal.buyer.userId == userId) deal.seller.username else deal.buyer.username
    }

    suspend fun smartContractStatus(deal: Deal): StatusData {
        val userId = profileLocalRepository.getUserId()
        return when {
            isBuyerRequestInitialized(deal, userId) ->
                StatusData("Ожидание создания сделки в контракте", "Создать", "Отменить")
            isBuyerNotDeposited(deal, userId) ->
                StatusData("Ожидание депозита на смарт-контракт", "Пополнить", "Отменить")
            isSellerNotPayedExpertFee(deal, userId) ->
                StatusData("Ожидание подтверждения от продавца", "Оплатить", "Отменить")
            isContractAwaitingUserConfirmation(deal, userId) ->
                StatusData("Выполните условия договора и нажмите «Отправить»", "Отправить", "Открыть спор")
            isExpertNotDecision(deal, userId) ->
                StatusData("Решение над диспутом", "Подтвердить", "Отклонить")
            isDisputeNotAgreed(deal, userId) || isDisputeNotDeclined(deal, userId) ->
                StatusData("Примите решение над условиями диспута", "Подтвердить", "Отклонить")
            else -> determineOppositeStatus(deal)
        }
    }

    private suspend fun determineOppositeStatus(deal: Deal): StatusData {
        val userId = profileLocalRepository.getUserId()
        val oppositeUserId = getOppositeUserId(deal, userId)
        return when {
            isBuyerRequestInitialized(deal, oppositeUserId) ->
                StatusData("Ожидание создания сделки в контракте", "Создать", "Отменить")
            isBuyerNotDeposited(deal, oppositeUserId) ->
                StatusData("Ожидание депозита на смарт-контракт", "Пополнить", "Отменить")
            isSellerNotPayedExpertFee(deal, oppositeUserId) ->
                StatusData("Ожидание подтверждения от продавца", "Оплатить", "Отменить")
            isContractAwaitingUserConfirmation(deal, oppositeUserId) ->
                StatusData("Выполните условия договора и нажмите «Отправить»", "Отправить", "Открыть спор")
            else ->
                StatusData("Неизвестный статус контракта", "Нет действий", "Нет действий")
        }
    }

    fun deploySmartContract(
        commission: BigDecimal,
        energy: Long,
        bandwidth: Long,
    ) = viewModelScope.launch(ioDispatcher) {
        val address = addressLocalRepository.getGeneralAddressByWalletId(1L)
        val centralAddressEntity = centralAddressLocalRepository.get()
        val addressEntity = addressLocalRepository.getByAddress(address)

        if (centralAddressEntity == null || addressEntity == null) return@launch

        val centralAddressBalance = tron.addressUtilities.getTrxBalance(centralAddressEntity.address)
        if (centralAddressBalance.toTokenAmount() < commission) return@launch

        val trxFeeAddress = configRepository.getFeeConfiguration().fold(
            onSuccess = { it.trxFeeAddress },
            onFailure = {
                Sentry.captureException(it)
                return@launch
            },
        )

        // TODO: собрать DeployContractParams (подпись деплой-транзакции и комиссии
        //  через tron.smartContracts / tron.transactions с trxFeeAddress)
        //  и вызвать contractRepository.deployContract(params)
    }
}

data class StatusData(
    val status: String,
    val completeButtonName: String,
    val rejectButtonName: String,
)

data class ContractButtonVisibleType(
    val agreeVisible: Boolean,
    val cancelVisible: Boolean,
)
