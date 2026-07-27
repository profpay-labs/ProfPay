package com.profpay.core.network.exception

class GrpcRequestException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class GrpcResponseException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
