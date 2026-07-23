package com.profpay.data.wallet.mapper

import com.profpay.data.wallet.dto.*
import com.profpay.domain.wallet.model.*

internal fun WalletResponseDto.toDomain(): WalletResult = WalletResult(
    id = id,
    userId = userId,
    addresses = addresses.map { it.toDomain() },
)

internal fun WalletAddressDto.toDomain(): WalletAddress = WalletAddress(
    id = id,
    address = address,
    publicKey = publicKey,
    type = AddressType.fromString(type),
    derivedIndices = derivedIndices,
    sot = sot?.toDomain(),
    parentAddressId = parentAddressId,
    active = active,
)

internal fun SotInfoDto.toDomain(): SotInfo = SotInfo(
    index = index,
    derivationIndex = derivationIndex,
)

internal fun CreateWalletParams.toDto(): CreateWalletRequest = CreateWalletRequest(
    appId = appId,
    generalAddress = generalAddress.toDto(),
    centralAddress = centralAddress.toDto(),
    sotAddresses = sotAddresses.map { it.toDto() },
)

internal fun GeneralAddressParams.toDto(): GeneralAddressDto = GeneralAddressDto(
    address = address,
    pubKey = pubKey,
    derivedIndices = derivedIndices,
)

internal fun CentralAddressParams.toDto(): CentralAddressDto = CentralAddressDto(
    address = address,
    pubKey = pubKey,
)

internal fun SotAddressParams.toDto(): SotAddressDto = SotAddressDto(
    address = address,
    pubKey = pubKey,
    index = index,
    derivationIndex = derivationIndex,
)

internal fun UpdateDerivedIndexResponseDto.toDomain(): UpdateDerivedIndexResult =
    UpdateDerivedIndexResult(
        timestamp = timestamp,
    )

internal fun UpdateDerivedIndexParams.toDto(): UpdateDerivedIndexRequest =
    UpdateDerivedIndexRequest(
        appId = appId,
        oldSotAddress = oldSotAddress,
        newSotAddress = newSotAddress.toDto(),
        generalAddress = generalAddress.toDto(),
    )

internal fun GeneralAddressUpdateParams.toDto(): GeneralAddressUpdateDto =
    GeneralAddressUpdateDto(
        address = address,
        oldSotDerivationIndex = oldSotDerivationIndex,
        newSotDerivationIndex = newSotDerivationIndex,
    )

internal fun WalletDataResponseDto.toDomain(): WalletDataResult = WalletDataResult(
    userId = userId,
    derivedIndices = derivedIndices,
    timestamp = timestamp,
)

fun CentralAddressResponseDto.toDomain(): CentralAddressResult =
    CentralAddressResult(
        id = id,
        userId = userId,
        address = address,
        publicKey = publicKey,
        type = AddressType.fromString(type),
        active = active,
    )

fun CentralAddressParams.toSetCentralAddressDto(): SetCentralAddressRequestDto =
    SetCentralAddressRequestDto(
        address = address,
        publicKey = pubKey,
    )
