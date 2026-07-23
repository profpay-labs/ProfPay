package com.profpay.data.user.mapper

import com.profpay.data.user.dto.CentralAddressDto
import com.profpay.data.user.dto.GeneralAddressDto
import com.profpay.data.user.dto.OnboardUserResponseDto
import com.profpay.data.user.dto.SotAddressDto
import com.profpay.data.user.dto.WalletOnboardingDataDto
import com.profpay.domain.user.model.CentralAddress
import com.profpay.domain.user.model.GeneralAddress
import com.profpay.domain.user.model.OnboardUserResult
import com.profpay.domain.user.model.SotAddress
import com.profpay.domain.user.model.WalletOnboardingData

// ══════════════════════════════════════════════════════════════════════
// Response → Domain
// ══════════════════════════════════════════════════════════════════════

fun OnboardUserResponseDto.toDomain(): OnboardUserResult {
    return OnboardUserResult(
        userId = userId,
        walletId = walletId,
        timestamp = timestamp,
    )
}

// ══════════════════════════════════════════════════════════════════════
// Domain → DTO (для request)
// ══════════════════════════════════════════════════════════════════════

fun WalletOnboardingData.toDto(): WalletOnboardingDataDto {
    return WalletOnboardingDataDto(
        generalAddress = generalAddress.toDto(),
        centralAddress = centralAddress?.toDto(),
        sotAddresses = sotAddresses.map { it.toDto() },
    )
}

fun GeneralAddress.toDto(): GeneralAddressDto {
    return GeneralAddressDto(
        address = address,
        pubKey = pubKey,
        derivedIndices = derivedIndices,
    )
}

fun CentralAddress.toDto(): CentralAddressDto {
    return CentralAddressDto(
        address = address,
        pubKey = pubKey,
    )
}

fun SotAddress.toDto(): SotAddressDto {
    return SotAddressDto(
        address = address,
        pubKey = pubKey,
        index = index,
        derivationIndex = derivationIndex,
    )
}
