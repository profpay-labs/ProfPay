package com.profpay.data.user.repository

import com.profpay.core.network.error.NetworkError
import com.profpay.core.network.error.safeApiCall
import com.profpay.data.user.api.PublicUserApi
import com.profpay.data.user.api.UserApi
import com.profpay.data.user.dto.CheckPermissionsRequestDto
import com.profpay.data.user.dto.CheckPermissionsResponseDto
import com.profpay.data.user.dto.OnboardUserRequestDto
import com.profpay.data.user.dto.OnboardUserResponseDto
import com.profpay.data.user.dto.RegisterDeviceRequestDto
import com.profpay.data.user.dto.RegisterDeviceResponseDto
import com.profpay.data.user.dto.TelegramDataResponseDto
import com.profpay.data.user.dto.UpdateTelegramRequestDto
import com.profpay.data.user.dto.UpdateTelegramResponseDto
import com.profpay.data.user.dto.UserExistsResponseDto
import com.profpay.data.user.mapper.toDomain
import com.profpay.data.user.mapper.toDto
import com.profpay.domain.user.exception.UserError
import com.profpay.domain.user.model.OnboardUserResult
import com.profpay.domain.user.model.RegisterDeviceResult
import com.profpay.domain.user.model.TelegramDataResult
import com.profpay.domain.user.model.UpdateTelegramResult
import com.profpay.domain.user.model.UserExistsResult
import com.profpay.domain.user.model.UserPermissionsResult
import com.profpay.domain.user.model.WalletOnboardingData
import com.profpay.domain.user.repository.UserRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val publicUserApi: PublicUserApi,
    private val json: Json,
) : UserRepository {

    override suspend fun onboardUser(
        deviceToken: String,
        appId: String,
        consentAccepted: Boolean,
        wallet: WalletOnboardingData,
    ): Result<OnboardUserResult> {
        val request = OnboardUserRequestDto(
            deviceToken = deviceToken,
            appId = appId,
            consentAccepted = consentAccepted,
            wallet = wallet.toDto(),
        )

        val apiResult: Result<OnboardUserResponseDto> = safeApiCall(json) {
            publicUserApi.onboardUser(request)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toOnboardError(deviceToken) }
    }

    override suspend fun checkPermissions(
        appId: String,
        deviceToken: String,
    ): Result<UserPermissionsResult> {
        val request = CheckPermissionsRequestDto(
            appId = appId,
            deviceToken = deviceToken,
        )

        val apiResult: Result<CheckPermissionsResponseDto> = safeApiCall(json) {
            userApi.checkPermissions(request)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toPermissionsError() }
    }

    override suspend fun checkUserExists(userId: Long): Result<UserExistsResult> {
        val apiResult: Result<UserExistsResponseDto> = safeApiCall(json) {
            userApi.checkUserExists(userId)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toUserError() }
    }

    override suspend fun getTelegramData(appId: String): Result<TelegramDataResult> {
        val apiResult: Result<TelegramDataResponseDto> = safeApiCall(json) {
            userApi.getTelegramData(appId)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toTelegramDataError(appId) }
    }

    override suspend fun registerDevice(
        userId: Long,
        deviceToken: String,
        appId: String,
    ): Result<RegisterDeviceResult> {
        val request = RegisterDeviceRequestDto(
            userId = userId,
            deviceToken = deviceToken,
            appId = appId,
        )

        val apiResult: Result<RegisterDeviceResponseDto> = safeApiCall(json) {
            userApi.registerDevice(request)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toRegisterDeviceError(userId) }
    }

    override suspend fun updateTelegram(
        userId: Long,
        username: String,
        telegramId: Long,
    ): Result<UpdateTelegramResult> {
        val request = UpdateTelegramRequestDto(
            username = username,
            telegramId = telegramId,
        )

        val apiResult: Result<UpdateTelegramResponseDto> = safeApiCall(json) {
            userApi.updateTelegram(userId, request)
        }

        return apiResult
            .map { dto -> dto.toDomain() }
            .recoverCatching { error -> throw error.toUpdateTelegramError(userId) }
    }

    private fun Throwable.toRegisterError(deviceToken: String): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isBadRequest -> UserError.InvalidRegistrationData(this)
            isConflict -> UserError.DeviceAlreadyRegistered(deviceToken)
            isServerError -> UserError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toPermissionsError(): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isServerError -> UserError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toUserError(): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isServerError -> UserError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toTelegramDataError(appId: String): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> UserError.UserNotFoundByAppId(appId)
            isServerError -> UserError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toRegisterDeviceError(userId: Long): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> UserError.UserNotFound(userId)
            isServerError -> UserError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toConsentError(): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isServerError -> UserError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toUpdateTelegramError(userId: Long): Throwable = when (this) {
        is NetworkError.HttpError -> when {
            isNotFound -> UserError.UserNotFound(userId)
            isServerError -> UserError.ServerError(this)
            else -> this
        }
        else -> this
    }

    private fun Throwable.toOnboardError(deviceToken: String): Throwable {
        return when (this) {
            is NetworkError.HttpError -> when {
                isBadRequest -> UserError.ConsentNotAccepted
                isConflict -> UserError.DeviceAlreadyRegistered(deviceToken)
                isServerError -> UserError.ServerError(this)
                else -> UserError.OnboardingFailed(this)
            }
            else -> UserError.OnboardingFailed(this)
        }
    }
}
