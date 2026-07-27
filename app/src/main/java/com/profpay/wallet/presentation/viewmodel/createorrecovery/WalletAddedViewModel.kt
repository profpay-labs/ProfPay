package com.profpay.wallet.presentation.viewmodel.createorrecovery

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.profpay.domain.wallet.model.GeneratedWalletData
import com.profpay.domain.wallet.model.RecoveredAddressData
import com.profpay.domain.wallet.model.SotAddressParams
import com.profpay.domain.wallet.model.WalletAddressesData
import com.profpay.domain.wallet.repository.WalletCreationRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import com.profpay.domain.wallet.usecase.AddWalletUseCase
import com.profpay.wallet.PrefKeys
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
    private val walletCreationRepository: WalletCreationRepository,
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
     * Создание кошелька из GeneratedWalletData (новый кошелёк).
     *
     * Логика определяется по AppState:
     * - NotRegistered → полный онбординг
     * - Registered → добавление дополнительного кошелька
     */
    fun onWalletCreatedClicked(
        walletData: GeneratedWalletData,
        sharedPref: SharedPreferences,
    ) = viewModelScope.launch {
        try {
            val deviceToken = sharedPref.getString(PrefKeys.DEVICE_TOKEN, null)
                ?: throw IllegalStateException("Device Token not found")

            // Конвертируем GeneratedWalletData в WalletAddressesData
            val addressesData = walletData.toWalletAddressesData()

            // Определяем действие по состоянию приложения
            when (appStateRepository.getAppState()) {
                is AppState.NotRegistered -> {
                    performOnboarding(
                        deviceToken = deviceToken,
                        addressesData = addressesData,
                    )
                }
                is AppState.Registered -> {
                    performAddWallet(addressesData)
                }
            }

            // Сохраняем адреса в локальную базу данных
            walletCreationRepository.insertNewCryptoAddresses(addressesData)

            _uiEvent.emit(WalletUiEvent.NavigateToHome)
        } catch (e: Exception) {
            Sentry.captureException(e)
            _uiEvent.emit(WalletUiEvent.ShowError(e.toUserMessage()))
        }
    }

    /**
     * Восстановление кошелька (существующий пользователь).
     */
    fun onWalletRecoveryClicked(
        sharedPref: SharedPreferences,
        addressData: RecoveredAddressData,
        accountWasFound: Boolean,
        userId: Long?,
    ) = viewModelScope.launch {
        try {
            val deviceToken = sharedPref.getString(PrefKeys.DEVICE_TOKEN, null)
                ?: throw IllegalStateException("Device Token not found")

            // Конвертируем RecoveredAddressData в WalletAddressesData
            val addressesData = addressData.toWalletAddressesData()

            if (accountWasFound && userId != null) {
                // Регистрация нового устройства для существующего пользователя
                walletCreationRepository.registerUserDevice(userId, deviceToken)
            } else {
                // Новый пользователь — полный онбординг
                performOnboarding(
                    deviceToken = deviceToken,
                    addressesData = addressesData,
                )
            }

            // Сохраняем адреса локально
            walletCreationRepository.insertNewCryptoAddresses(addressesData)

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
        addressesData: WalletAddressesData,
    ) {
        val appId = UUID.randomUUID().toString()
        val walletData = buildWalletOnboardingData(addressesData)

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
    private suspend fun performAddWallet(addressesData: WalletAddressesData) {
        val params = buildAddWalletParams(addressesData)

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
     * Конвертирует WalletAddressesData в параметры для добавления кошелька.
     */
    private fun buildAddWalletParams(addressesData: WalletAddressesData): AddWalletParams {
        val generalAddressData = addressesData.addresses
            .firstOrNull { it.derivationIndex == 0 }
            ?: throw IllegalStateException("General address not found")

        val sotAddresses = addressesData.addresses
            .filter { it.derivationIndex != 0 }
            .map { address ->
                SotAddressParams(
                    address = address.address,
                    pubKey = address.publicKey,
                    index = address.sotIndex,
                    derivationIndex = address.derivationIndex,
                )
            }

        val derivedIndices = addressesData.addresses.map { it.derivationIndex }

        return AddWalletParams(
            generalAddress = GeneralAddressParams(
                address = generalAddressData.address,
                pubKey = generalAddressData.publicKey,
                derivedIndices = derivedIndices,
            ),
            sotAddresses = sotAddresses,
        )
    }

    /**
     * Конвертирует WalletAddressesData в domain модель для онбординга.
     */
    private suspend fun buildWalletOnboardingData(
        addressesData: WalletAddressesData,
    ): WalletOnboardingData {
        val generalAddressData = addressesData.addresses
            .firstOrNull { it.derivationIndex == 0 }
            ?: throw IllegalStateException("General address not found")

        val sotAddresses = addressesData.addresses
            .filter { it.derivationIndex != 0 }
            .map { address ->
                SotAddress(
                    address = address.address,
                    pubKey = address.publicKey,
                    index = address.sotIndex,
                    derivationIndex = address.derivationIndex,
                )
            }

        val centralAddress = centralAddressLocalRepository.get()?.let { entity ->
            CentralAddress(
                address = entity.address,
                pubKey = entity.publicKey,
            )
        }

        val derivedIndices = addressesData.addresses.map { it.derivationIndex }

        return WalletOnboardingData(
            generalAddress = GeneralAddress(
                address = generalAddressData.address,
                pubKey = generalAddressData.publicKey,
                derivedIndices = derivedIndices,
            ),
            centralAddress = centralAddress,
            sotAddresses = sotAddresses,
        )
    }

    // Extension functions для конвертации

    private fun GeneratedWalletData.toWalletAddressesData(): WalletAddressesData {
        return WalletAddressesData(
            entropy = this.entropy,
            addresses = this.addresses.map { addr ->
                WalletAddressesData.AddressData(
                    address = addr.address,
                    publicKey = addr.publicKey,
                    sotIndex = addr.sotIndex,
                    derivationIndex = addr.derivationIndex,
                )
            }
        )
    }

    private fun RecoveredAddressData.toWalletAddressesData(): WalletAddressesData {
        return WalletAddressesData(
            entropy = this.entropy,
            addresses = this.addresses.map { addr ->
                WalletAddressesData.AddressData(
                    address = addr.address,
                    publicKey = addr.publicKey,
                    sotIndex = addr.sotIndex,
                    derivationIndex = addr.derivationIndex,
                )
            }
        )
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
