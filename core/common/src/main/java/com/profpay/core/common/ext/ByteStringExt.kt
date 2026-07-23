package com.profpay.core.common.ext

import com.google.protobuf.ByteString
import java.math.BigInteger

/**
 * Конвертирует ByteString в BigInteger (положительное число).
 */
fun ByteString.toBigInteger(): BigInteger =
    BigInteger(1, this.toByteArray())

/**
 * Конвертирует BigInteger в ByteString.
 */
fun BigInteger.toByteString(): ByteString =
    ByteString.copyFrom(this.toByteArray())
