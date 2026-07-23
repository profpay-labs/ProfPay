package com.profpay.data.wallet.repository

import com.profpay.core.tron.Tron
import com.profpay.domain.wallet.exception.WalletError
import com.profpay.domain.wallet.model.CentralAddressParams
import com.profpay.domain.wallet.repository.ReissueCentralAddressRepository
import com.profpay.domain.wallet.repository.WalletRepository
import com.profpay.domain.wallet.repository.local.CentralAddressLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReissueCentralAddressRepositoryImpl @Inject constructor(
    private val centralAddressLocalRepository: CentralAddressLocalRepository,
    private val walletRepository: WalletRepository,
    private val tron: Tron,
) : ReissueCentralAddressRepository {

    override suspend fun changeCentralAddress(): Result<Unit> {
        val address = tron.addressUtilities.generateSingleAddress()

        return try {
            walletRepository.setCentralAddress(
                CentralAddressParams(
                    address.address,
                    address.publicKey
                )
            ).fold(
                onSuccess = {
                    centralAddressLocalRepository.change(
                        address = address.address,
                        publicKey = address.publicKey,
                        privateKey = address.privateKey,
                    )
                    Result.success(Unit)
                },
                onFailure = { throwable ->
                    val error = WalletError.CentralAddressServerError(
                        message = throwable.message ?: "Unknown server error",
                        cause = throwable.cause,
                    )
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            val error = WalletError.CentralAddressClientError(
                message = e.message ?: "Unknown client error",
                cause = e.cause,
            )
            Result.failure(error)
        }
    }
}
