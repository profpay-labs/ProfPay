// core/tron/src/main/java/com/profpay/core/tron/api/TronAccountsApi.kt
package com.profpay.core.tron.api

import org.tron.trident.proto.Response
import java.math.BigInteger

interface TronAccountsApi {
    fun getAccountResource(ownerAddress: String, privateKey: String): Response.AccountResourceMessage
    fun getAccount(ownerAddress: String, privateKey: String): Response.Account
    fun allowance(spender: String, ownerAddress: String, privateKey: String): BigInteger?
    fun isAllowanceUnlimited(spender: String, ownerAddress: String, privateKey: String): Boolean
    fun hasEnoughBandwidth(address: String, requiredBandwidth: Long): Boolean
}
