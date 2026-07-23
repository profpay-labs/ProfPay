package com.profpay.data.wallet.mapper

import com.profpay.data.wallet.dto.AddWalletRequestDto
import com.profpay.data.wallet.dto.GeneralAddressDto
import com.profpay.data.wallet.dto.SotAddressDto
import com.profpay.domain.wallet.model.AddWalletParams

// ══════════════════════════════════════════════════════════════════════
// Domain → DTO (Request)
// ══════════════════════════════════════════════════════════════════════

fun AddWalletParams.toDto(): AddWalletRequestDto {
    return AddWalletRequestDto(
        generalAddress = GeneralAddressDto(
            address = generalAddress.address,
            pubKey = generalAddress.pubKey,
            derivedIndices = generalAddress.derivedIndices.map { it },
        ),
        sotAddresses = sotAddresses.map { sot ->
            SotAddressDto(
                address = sot.address,
                pubKey = sot.pubKey,
                index = sot.index,
                derivationIndex = sot.derivationIndex,
            )
        },
    )
}


