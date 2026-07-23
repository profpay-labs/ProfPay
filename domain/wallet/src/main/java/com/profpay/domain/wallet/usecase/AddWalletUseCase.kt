package com.profpay.domain.wallet.usecase

import com.profpay.domain.wallet.model.AddWalletParams
import com.profpay.domain.wallet.model.WalletResult
import com.profpay.domain.wallet.repository.WalletRepository
import javax.inject.Inject

/**
 * UseCase для добавления дополнительного кошелька.
 *
 * Особенности:
 * - Пользователь должен быть аутентифицирован
 * - Центральный адрес НЕ создаётся (он един на все кошельки)
 * - Создаётся только основной адрес и SOT адреса
 */
class AddWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {

    suspend operator fun invoke(params: AddWalletParams): Result<WalletResult> {
        require(params.generalAddress.address.isNotBlank()) { "General address is required" }
        require(params.generalAddress.pubKey.isNotBlank()) { "General address public key is required" }

        return walletRepository.addWallet(params)
    }
}
