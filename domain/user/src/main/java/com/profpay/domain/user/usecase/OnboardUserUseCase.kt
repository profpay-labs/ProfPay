package com.profpay.domain.user.usecase

import com.profpay.domain.user.model.OnboardUserResult
import com.profpay.domain.user.model.WalletOnboardingData
import com.profpay.domain.user.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase для онбординга пользователя при первом запуске приложения.
 *
 * Включает:
 * - Создание пользователя
 * - Регистрацию устройства
 * - Принятие пользовательского соглашения
 * - Создание кошелька с адресами
 */
class OnboardUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke(
        deviceToken: String,
        appId: String,
        consentAccepted: Boolean,
        wallet: WalletOnboardingData,
    ): Result<OnboardUserResult> {
        require(consentAccepted) { "Consent must be accepted for onboarding" }
        require(wallet.generalAddress.address.isNotBlank()) { "General address is required" }
        require(wallet.generalAddress.pubKey.isNotBlank()) { "General address public key is required" }

        return userRepository.onboardUser(
            deviceToken = deviceToken,
            appId = appId,
            consentAccepted = consentAccepted,
            wallet = wallet,
        )
    }
}
