package com.profpay.core.common.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Форматирует BigDecimal для отображения валюты.
 *
 * - Использует точку как разделитель тысяч, запятую как десятичный разделитель
 * - Для значений >= 1: 2 знака после запятой
 * - Для значений < 1: до 8 знаков после запятой
 * - Округление DOWN (без округления вверх)
 */
fun BigDecimal.formatCurrency(): String {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    val format = DecimalFormat().apply {
        decimalFormatSymbols = symbols
        roundingMode = RoundingMode.DOWN

        if (this@formatCurrency.abs() >= BigDecimal.ONE) {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        } else {
            minimumFractionDigits = 2
            maximumFractionDigits = 8
        }
    }

    return format.format(this)
}

/**
 * @deprecated Используй BigDecimal.formatCurrency()
 */
@Deprecated(
    message = "Используй extension function BigDecimal.formatCurrency()",
    replaceWith = ReplaceWith("value.formatCurrency()", "com.profpay.core.common.format.formatCurrency")
)
fun decimalFormat(value: BigDecimal): String = value.formatCurrency()
