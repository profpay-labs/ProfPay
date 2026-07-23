package com.profpay.core.common.format

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Форматирует дату относительно текущего времени.
 */
object RelativeDateFormatter {

    private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

    /**
     * Форматирует timestamp (в секундах) в человекочитаемую строку.
     *
     * Примеры: "день назад", "3 дня назад", "5 дней назад", "01.05.2025"
     *
     * @param timestampSeconds Unix timestamp в секундах
     * @return отформатированная строка
     */
    fun formatRelative(timestampSeconds: Long): String {
        val date = Instant.ofEpochSecond(timestampSeconds)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val daysDiff = ChronoUnit.DAYS.between(date, LocalDate.now()).toInt()

        return when {
            daysDiff == 0 -> "сегодня"
            daysDiff == 1 -> "день назад"
            daysDiff in 2..4 -> "$daysDiff дня назад"
            daysDiff in 5..7 -> "$daysDiff дней назад"
            else -> date.format(dateFormat)
        }
    }

    /**
     * @deprecated Используй formatRelative(timestampSeconds: Long)
     */
    @Deprecated(
        message = "Используй formatRelative(timestampSeconds: Long)",
        replaceWith = ReplaceWith("formatRelative(timestamp.toLong())")
    )
    fun displayDateForAML(timestamp: String): String = formatRelative(timestamp.toLong())
}
