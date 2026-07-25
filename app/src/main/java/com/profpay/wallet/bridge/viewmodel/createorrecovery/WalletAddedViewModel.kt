package com.profpay.wallet.bridge.viewmodel.createorrecovery

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.core.tron.model.AddressesWithKeysForM
import com.profpay.domain.user.exception.UserError
import com.profpay.domain.user.model.AppState
import com.profpay.domain.user.model.CentralAddress
import com.profpay.domain.user.model.GeneralAddress
import com.profpay.domain.user.model.SotAddress
import com.profpay.domain.user.model.WalletOnboardingData
import com.profpay.domain.user.model.local.UserProfile
import com.profpay.domain.user.repository.AppStateRepository
import com.profpay.domain.user.repository.local.ProfileLocalRepository
import com.profpay.domain.user.usecase.OnboardUserUseCase
import com.profpay.domain.wallet.exception.WalletError
import com.profpay.domain.wallet.model.AddWalletParams
import com.profpay.domain.wallet.model.GeneralAddressParams
import com.profpay.domain.wallet.model.SotAddressParams
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.usecase.AddWalletUseCase
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.data.repository.WalletAddedRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WalletAddedViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val onboardUserUseCase: OnboardUserUseCase,
    private val addWalletUseCase: AddWalletUseCase,
    private val walletAddedRepo: WalletAddedRepo,
    private val profileRepo: ProfileLocalRepository,
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<WalletUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class WalletUiEvent {
        data object NavigateToHome : WalletUiEvent()
        data class ShowError(val message: String) : WalletUiEvent()
    }

    /**
     * Создание кошелька.
     *
     * Логика определяется по AppState:
     * - NotRegistered → полный онбординг
     * - Registered → добавление дополнительного кошелька
     */
    fun onWalletCreatedClicked(
        addressesWithKeysForM: AddressesWithKeysForM,
        sharedPref: SharedPreferences,
    ) = viewModelScope.launch {
        try {
            val deviceToken = sharedPref.getString(PrefKeys.DEVICE_TOKEN, null)
                ?: throw IllegalStateException("Device Token not found")

            // Определяем действие по состоянию приложения
            when (val appState = appStateRepository.getAppState()) {
                is AppState.NotRegistered -> {
                    // Первый запуск: полный онбординг
                    performOnboarding(
                        deviceToken = deviceToken,
                        addressesWithKeysForM = addressesWithKeysForM,
                    )
                }

                is AppState.Registered -> {
                    // Уже зарегистрирован: добавляем дополнительный кошелёк
                    performAddWallet(addressesWithKeysForM)
                }
            }

            // Сохраняем адреса в локальную базу данных
            walletAddedRepo.insertNewCryptoAddresses(addressesWithKeysForM)

            _uiEvent.emit(WalletUiEvent.NavigateToHome)
        } catch (e: Exception) {
            Sentry.captureException(e)
            _uiEvent.emit(WalletUiEvent.ShowError(e.toUserMessage()))
        }
    }

    /**
     * Выполняет онбординг через единый endpoint (первый запуск).
     */
    private suspend fun performOnboarding(
        deviceToken: String,
        addressesWithKeysForM: AddressesWithKeysForM,
    ) {
        val appId = UUID.randomUUID().toString()

        val walletData = buildWalletOnboardingData(addressesWithKeysForM)

        val result = onboardUserUseCase(
            deviceToken = deviceToken,
            appId = appId,
            consentAccepted = true,
            wallet = walletData,
        )

        result.fold(
            onSuccess = { onboardResult ->
                profileRepo.create(
                    UserProfile(
                        userId = onboardResult.userId,
                        appId = appId,
                        deviceToken = deviceToken,
                        isActive = true
                    )
                )
                Log.i(TAG, "Onboarding successful: userId=${onboardResult.userId}, walletId=${onboardResult.walletId}")
            },
            onFailure = { error ->
                throw error
            }
        )
    }

    /**
     * Добавляет дополнительный кошелёк к существующему пользователю.
     */
    private suspend fun performAddWallet(
        addressesWithKeysForM: AddressesWithKeysForM,
    ) {
        val params = buildAddWalletParams(addressesWithKeysForM)

        val result = addWalletUseCase(params)

        result.fold(
            onSuccess = { addWalletResult ->
                Log.i(TAG, "Wallet added: walletId=${addWalletResult.id}, addresses=${addWalletResult.addresses.size}")
            },
            onFailure = { error ->
                throw error
            }
        )
    }

    /**
     * Конвертирует локальную модель адресов в параметры для добавления кошелька.
     */
    private fun buildAddWalletParams(
        addressesWithKeysForM: AddressesWithKeysForM,
    ): AddWalletParams {
        val generalAddressData = addressesWithKeysForM.addresses
            .firstOrNull { it.indexDerivationSot == 0 }
            ?: throw IllegalStateException("General address not found")

        val sotAddresses = addressesWithKeysForM.addresses
            .filter { it.indexDerivationSot != 0 }
            .map { address ->
                SotAddressParams(
                    address = address.address,
                    pubKey = address.publicKey,
                    index = address.indexSot.toInt(),
                    derivationIndex = address.indexDerivationSot,
                )
            }

        return AddWalletParams(
            generalAddress = GeneralAddressParams(
                address = generalAddressData.address,
                pubKey = generalAddressData.publicKey,
                derivedIndices = addressesWithKeysForM.derivedIndices,
            ),
            sotAddresses = sotAddresses,
        )
    }

    /**
     * Конвертирует локальную модель адресов в domain модель для онбординга.
     */
    private suspend fun buildWalletOnboardingData(
        addressesWithKeysForM: AddressesWithKeysForM,
    ): WalletOnboardingData {
        val generalAddressData = addressesWithKeysForM.addresses
            .firstOrNull { it.indexDerivationSot == 0 }
            ?: throw IllegalStateException("General address not found")

        val sotAddresses = addressesWithKeysForM.addresses
            .filter { it.indexDerivationSot != 0 }
            .map { address ->
                SotAddress(
                    address = address.address,
                    pubKey = address.publicKey,
                    index = address.indexSot.toInt(),
                    derivationIndex = address.indexDerivationSot,
                )
            }

        val centralAddress = centralAddressLocalRepository.get()?.let { entity ->
            CentralAddress(
                address = entity.address,
                pubKey = entity.publicKey,
            )
        }

        return WalletOnboardingData(
            generalAddress = GeneralAddress(
                address = generalAddressData.address,
                pubKey = generalAddressData.publicKey,
                derivedIndices = addressesWithKeysForM.derivedIndices,
            ),
            centralAddress = centralAddress,
            sotAddresses = sotAddresses,
        )
    }

    /**
     * Восстановление кошелька (существующий пользователь).
     */
    fun onWalletRecoveryClicked(
        sharedPref: SharedPreferences,
        addressesWithKeysForM: AddressesWithKeysForM,
        accountWasFound: Boolean,
        userId: Long?,
    ) = viewModelScope.launch {
        try {
            val deviceToken = sharedPref.getString(PrefKeys.DEVICE_TOKEN, null)
                ?: throw IllegalStateException("Device Token not found")

            if (accountWasFound && userId != null) {
                // Регистрация нового устройства для существующего пользователя
                walletAddedRepo.registerUserDevice(userId, deviceToken, sharedPref)
            } else {
                // Новый пользователь — полный онбординг
                performOnboarding(
                    deviceToken = deviceToken,
                    addressesWithKeysForM = addressesWithKeysForM,
                )
            }

            // Сохраняем адреса локально
            walletAddedRepo.insertNewCryptoAddresses(addressesWithKeysForM)

            _uiEvent.emit(WalletUiEvent.NavigateToHome)
        } catch (e: Exception) {
            Sentry.captureException(e)
            _uiEvent.emit(WalletUiEvent.ShowError(e.toUserMessage()))
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        // User errors
        is UserError.ConsentNotAccepted -> "Необходимо принять соглашение"
        is UserError.DeviceAlreadyRegistered -> "Устройство уже зарегистрировано"
        is UserError.OnboardingFailed -> "Ошибка регистрации"
        is UserError.ServerError -> "Ошибка сервера, попробуйте позже"
        // Wallet errors
        is WalletError.AddressAlreadyExists -> "Адрес уже существует"
        is WalletError.AddWalletFailed -> "Не удалось добавить кошелёк"
        is WalletError.Unauthorized -> "Ошибка авторизации"
        is WalletError.ServerError -> "Ошибка сервера, попробуйте позже"
        // Generic
        is IllegalStateException -> message ?: "Ошибка данных"
        else -> message ?: "Неизвестная ошибка"
    }

    private companion object {
        const val TAG = "WalletAddedViewModel"
    }
}
