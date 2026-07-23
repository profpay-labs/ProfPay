package com.profpay.data.transfer.service

import com.profpay.core.common.converter.toTokenAmount
import com.profpay.core.tron.Tron
import com.profpay.domain.transfer.repository.BalanceChecker
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация BalanceChecker через Tron SDK.
 */
@Singleton
class BalanceCheckerImpl @Inject constructor(
    private val tron: Tron,
) : BalanceChecker {

    override suspend fun isAddressActivated(address: String): Boolean =
        tron.addressUtilities.isAddressActivated(address)

    override suspend fun getTrxBalance(address: String): BigDecimal =
        tron.addressUtilities.getTrxBalance(address).toTokenAmount()
}
