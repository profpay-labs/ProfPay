package com.profpay.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnboardUserRequestDto(
    @SerialName("deviceToken")
    val deviceToken: String,
    @SerialName("appId")
    val appId: String,
    @SerialName("consentAccepted")
    val consentAccepted: Boolean,
    @SerialName("wallet")
    val wallet: WalletOnboardingDataDto,
)

@Serializable
data class WalletOnboardingDataDto(
    @SerialName("generalAddress")
    val generalAddress: GeneralAddressDto,
    @SerialName("centralAddress")
    val centralAddress: CentralAddressDto? = null,
    @SerialName("sotAddresses")
    val sotAddresses: List<SotAddressDto> = emptyList(),
)

@Serializable
data class GeneralAddressDto(
    @SerialName("address")
    val address: String,
    @SerialName("pubKey")
    val pubKey: String,
    @SerialName("derivedIndices")
    val derivedIndices: List<Int>,
)

@Serializable
data class CentralAddressDto(
    @SerialName("address")
    val address: String,
    @SerialName("pubKey")
    val pubKey: String,
)

@Serializable
data class SotAddressDto(
    @SerialName("address")
    val address: String,
    @SerialName("pubKey")
    val pubKey: String,
    @SerialName("index")
    val index: Int,
    @SerialName("derivationIndex")
    val derivationIndex: Int,
)
