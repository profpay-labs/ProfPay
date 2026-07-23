// core/tron/src/main/java/com/profpay/core/tron/api/TronStakingApi.kt
package com.profpay.core.tron.api

interface TronStakingApi {
    fun freezeTrxV2(value: Long, ownerAddress: String, privateKey: String): String?
    fun unfreezeBalanceV2(value: Long, ownerAddress: String, privateKey: String): String?
    fun withdrawExpireUnfreeze(ownerAddress: String, privateKey: String): String?
}
