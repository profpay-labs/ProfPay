package com.profpay.core.common.converter

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * Утилиты для конвертации между SUN (минимальная единица TRX/USDT) и Token Amount.
 * 1 TRX/USDT = 1_000_000 SUN
 */
object SunConverter {
    private val SUN_DIVISOR = BigDecimal("1000000")

    /**
     * Конвертирует SUN в Token Amount.
     * Например: 1_000_000 SUN → 1.0 TRX
     */
    fun toTokenAmount(sunAmount: BigInteger): BigDecimal {
        val bd = sunAmount
            .toBigDecimal()
            .divide(SUN_DIVISOR, 6, RoundingMode.DOWN)
            .stripTrailingZeros()

        return if (bd.scale() < 0) bd.setScale(0, RoundingMode.UNNECESSARY) else bd
    }

    /**
     * Конвертирует Token Amount в SUN.
     * Например: 1.0 TRX → 1_000_000 SUN
     */
    fun toSunAmount(tokenAmount: BigDecimal): BigInteger =
        tokenAmount
            .multiply(SUN_DIVISOR)
            .setScale(0, RoundingMode.DOWN)
            .toBigInteger()
}

// Extension functions для удобства использования
fun BigInteger.toTokenAmount(): BigDecimal = SunConverter.toTokenAmount(this)
fun BigDecimal.toSunAmount(): BigInteger = SunConverter.toSunAmount(this)
