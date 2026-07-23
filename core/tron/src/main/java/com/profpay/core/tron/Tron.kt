package com.profpay.core.tron

import com.profpay.core.tron.api.TronAccountsApi
import com.profpay.core.tron.api.TronAddressApi
import com.profpay.core.tron.api.TronEstimateApi
import com.profpay.core.tron.api.TronHttpApi
import com.profpay.core.tron.api.TronSmartContractApi
import com.profpay.core.tron.api.TronStakingApi
import com.profpay.core.tron.api.TronTransactionsApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Главный фасад для работы с TRON блокчейном.
 */
@Singleton
class Tron @Inject constructor(
    val addressUtilities: TronAddressApi,
    val accounts: TronAccountsApi,
    val transactions: TronTransactionsApi,
    val staking: TronStakingApi,
    val smartContracts: TronSmartContractApi,
    val estimates: TronEstimateApi,
    val http: TronHttpApi,
) {
    // Алиас для нового стиля наименования
    val addresses: TronAddressApi get() = addressUtilities
}
