package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

import com.profpay.domain.transfer.model.EstimateCommissionResult
import com.profpay.domain.wallet.model.local.AddressWithTokensLocal
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import java.math.BigDecimal

data class ModelTransferFromBS(
    val amount: BigDecimal,
    val tokenName: TokenName,
    val addressReceiver: String,
    val addressSender: String,
    val commission: BigDecimal,
    val addressWithTokens: AddressWithTokensLocal?,
    val commissionResult: EstimateCommissionResult,
)
