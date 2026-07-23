package com.profpay.data.user.mapper

import com.profpay.data.user.dto.AcceptConsentResponseDto
import com.profpay.data.user.dto.CheckPermissionsResponseDto
import com.profpay.data.user.dto.RegisterDeviceResponseDto
import com.profpay.data.user.dto.RegisterUserResponseDto
import com.profpay.data.user.dto.TelegramDataResponseDto
import com.profpay.data.user.dto.UpdateTelegramResponseDto
import com.profpay.data.user.dto.UserExistsResponseDto
import com.profpay.domain.user.model.AcceptConsentResult
import com.profpay.domain.user.model.RegisterDeviceResult
import com.profpay.domain.user.model.RegisterUserResult
import com.profpay.domain.user.model.TelegramDataResult
import com.profpay.domain.user.model.UpdateTelegramResult
import com.profpay.domain.user.model.UserExistsResult
import com.profpay.domain.user.model.UserPermissionsResult

internal fun UserExistsResponseDto.toDomain(): UserExistsResult = UserExistsResult(
    exists = exists,
)

internal fun TelegramDataResponseDto.toDomain(): TelegramDataResult = TelegramDataResult(
    telegramId = telegramId,
    username = username,
)

fun RegisterUserResponseDto.toDomain(): RegisterUserResult =
    RegisterUserResult(
        userId = userId,
        timestamp = timestamp,
    )

fun CheckPermissionsResponseDto.toDomain(): UserPermissionsResult =
    UserPermissionsResult(
        isAppAllowed = appAllowed,
    )

fun RegisterDeviceResponseDto.toDomain(): RegisterDeviceResult =
    RegisterDeviceResult(
        successful = successful,
        timestamp = timestamp,
    )

fun AcceptConsentResponseDto.toDomain(): AcceptConsentResult =
    AcceptConsentResult(
        timestamp = timestamp,
    )

fun UpdateTelegramResponseDto.toDomain(): UpdateTelegramResult =
    UpdateTelegramResult(
        userId = userId,
        telegramId = telegramId,
        timestamp = timestamp,
    )
