package com.profpay.data.wallet.mapper

import com.profpay.core.database.dao.wallet.WalletProfileCipher
import com.profpay.core.database.dao.wallet.WalletProfileModel
import com.profpay.core.database.entities.wallet.WalletProfileEntity
import com.profpay.domain.wallet.model.local.WalletCipherData
import com.profpay.domain.wallet.model.local.WalletProfileLocal
import com.profpay.domain.wallet.model.local.WalletProfileSummary

object WalletProfileMapper {

    fun WalletProfileEntity.toLocal(): WalletProfileLocal = WalletProfileLocal(
        id = id,
        name = name,
        iv = iv,
        cipherText = cipherText,
    )

    fun WalletProfileLocal.toEntity(): WalletProfileEntity = WalletProfileEntity(
        id = id,
        name = name,
        iv = iv,
        cipherText = cipherText,
    )

    fun WalletProfileModel.toSummary(): WalletProfileSummary = WalletProfileSummary(
        id = id ?: 0L,
        name = name,
    )

    fun WalletProfileCipher.toDomain(): WalletCipherData = WalletCipherData(
        iv = iv,
        cipherText = cipherText,
    )
}
