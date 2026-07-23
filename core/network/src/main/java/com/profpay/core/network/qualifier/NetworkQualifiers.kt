package com.profpay.core.network.qualifier

import javax.inject.Qualifier

/**
 * OkHttpClient с авторизацией (JWT token)
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient

/**
 * OkHttpClient без авторизации (для публичных API)
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicClient

/**
 * Base URL для API
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl
