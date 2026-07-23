package com.profpay.domain.security.repository

interface PinManager {
    suspend fun hasPin(): Boolean
    suspend fun savePin(pin: String)
    suspend fun validatePin(pin: String): Boolean
    suspend fun clearPin()
}
