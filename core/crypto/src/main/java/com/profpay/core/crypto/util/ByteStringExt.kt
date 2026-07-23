package com.profpay.core.crypto.util

import com.google.protobuf.ByteString
import java.util.Base64

/**
 * Конвертация ByteString в Base64 строку.
 */
fun ByteString.toBase64(): String =
    Base64.getEncoder().encodeToString(this.toByteArray())

/**
 * Конвертация Base64 строки в ByteString.
 */
fun String.toByteStringFromBase64(): ByteString =
    ByteString.copyFrom(Base64.getDecoder().decode(this))

/**
 * Конвертация ByteString в hex-строку.
 */
fun ByteString.toHex(): String =
    this.toByteArray().joinToString("") { "%02x".format(it) }

/**
 * Конвертация hex-строки в ByteString.
 */
fun String.toByteStringFromHex(): ByteString =
    ByteString.copyFrom(this.chunked(2).map { it.toInt(16).toByte() }.toByteArray())
