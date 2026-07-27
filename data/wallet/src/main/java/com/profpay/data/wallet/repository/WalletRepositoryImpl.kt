package com.profpay.data.wallet.repository

import com.profpay.core.network.exception.NetworkError
import com.profpay.core.network.exception.safeApiCall
import com.profpay.data.wallet.api.PublicWalletApi
import com.profpay.data.wallet.api.WalletApi
import com.profpay.data.wallet.dto.CentralAddressResponseDto
import com.profpay.data.wallet.dto.UpdateDerivedIndexResponseDto
import com.profpay.data.wallet.dto.WalletDataResponseDto
import com.profpay.data.wallet.dto.WalletResponseDto
import com.profpay.data.wallet.mapper.toDomain
import com.profpay.data.wallet.mapper.toDto
import com.profpay.data.wallet.mapper.toSetCentralAddressDto
import com.profpay.domain.wallet.exception.WalletError
import com.profpay.domain.wallet.model.AddWalletParams
import com.profpay.domain.wallet.model.CentralAddressParams
import com.profpay.domain.wallet.model.CentralAddressResult
import com.profpay.domain.wallet.model.UpdateDerivedIndexParams
import com.profpay.domain.wallet.model.UpdateDerivedIndexResult
import com.profpay.domain.wallet.model.WalletDataResult
import com.profpay.domain.wallet.model.WalletResult
import com.profpay.domain.wallet.repository.WalletRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletApi: WalletApi,
    private val publicWalletApi: PublicWalletApi,
    private val json: Json,
) : WalletRepository {

    override suspend fun addWallet(params: AddWalletParams): Result<WalletResult> {
        val request = params.toDto()

        val apiResult: Result<WalletResponseDto> = safeApiCall(json) {
            walletApi.addWallet(request)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toAddWalletError(params.generalAddress.address) }
    }

    override suspend fun updateDerivedIndex(
        params: UpdateDerivedIndexParams,
    ): Result<UpdateDerivedIndexResult> {
        val apiResult: Result<UpdateDerivedIndexResponseDto> = safeApiCall(json) {
            walletApi.updateDerivedIndex(params.toDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toUpdateDerivedIndexError(params) }
    }

    override suspend fun getWalletData(address: String): Result<WalletDataResult> {
        val apiResult: Result<WalletDataResponseDto> = safeApiCall(json) {
            walletApi.getWalletData(address)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toGetWalletDataError(address) }
    }

    override suspend fun setCentralAddress(
        params: CentralAddressParams,
    ): Result<CentralAddressResult> {
        val apiResult: Result<CentralAddressResponseDto> = safeApiCall(json) {
            walletApi.setCentralAddress(params.toSetCentralAddressDto())
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toCentralAddressError() }
    }

    private fun Throwable.toAddWalletError(address: String): Throwable {
        return when (this) {
            is NetworkError.HttpError -> when {
                isBadRequest -> WalletError.AddressAlreadyExists(address)
                isUnauthorized -> WalletError.Unauthorized(this)
                isServerError -> WalletError.ServerError(this)
                else -> WalletError.AddWalletFailed(this)
            }
            else -> WalletError.AddWalletFailed(this)
        }
    }

    private fun Throwable.toUpdateDerivedIndexError(params: UpdateDerivedIndexParams): Throwable =
        when (this) {
            is NetworkError.HttpError -> when {
                isNotFound -> WalletError.SotAddressNotFound(params.oldSotAddress)
                code == 400 -> WalletError.InvalidDerivationIndex(
                    params.generalAddress.newSotDerivationIndex,
                    errorBody ?: "Invalid derivation index"
                )
                isServerError -> WalletError.ServerError(this)
                else -> this
            }
            else -> this
        }

    private fun Throwable.toGetWalletDataError(address: String): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> WalletError.AddressNotFound(address)
            isServerError -> WalletError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toCentralAddressError(): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isUnauthorized -> WalletError.Unauthorized(this)
            isBadRequest -> WalletError.InvalidCentralAddressData(errorBody ?: "Invalid data")
            isServerError -> WalletError.ServerError(this)
            else -> this
        }
        else -> this
    }
}
