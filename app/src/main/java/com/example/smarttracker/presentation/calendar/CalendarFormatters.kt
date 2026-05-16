package com.example.smarttracker.presentation.calendar

import com.example.smarttracker.domain.model.TrainingHistoryItem
import java.time.DayOfWeek
import java.time.LocalDate

// ── Форматтеры времени / расстояний ──────────────────────────────────────────

/** "2026-03-14T09:20:00" или "09:20:00" → "09:20". */
internal fun formatTime(iso: String?): String {
    if (iso == null) return "--:--"
    return try {
        val t = if (iso.contains('T')) iso.substringAfter('T') else iso
        t.substring(0, 5)
    } catch (_: Exception) { "--:--" }
}

/** "HH:MM - HH:MM" — диапазон для метки дневной карточки. */
internal fun formatTimeRange(start: String?, end: String?): String =
    "${formatTime(start)} - ${formatTime(end)}"

/** Разница двух ISO datetime → "01:34:02" или "--". */
internal fun formatDurationBetween(start: String?, end: String?): String {
    if (start == null || end == null) return "--"
    return try {
        val s = parseDateTime(start)
        val e = parseDateTime(end)
        val secs = java.time.Duration.between(s, e).seconds
        formatSeconds(secs)
    } catch (_: Exception) { "--" }
}

/** Секунды → "HH:MM:SS". */
internal fun formatSeconds(secs: Long): String =
    "%02d:%02d:%02d".format(secs / 3600, (secs % 3600) / 60, secs % 60)

/** Double? метров → "11,22 км" или "350 м" или "--". */
internal fun formatDistanceM(m: Double?): String {
    if (m == null) return "--"
    return if (m >= 1000) "${"%.2f".format(m / 1000).replace('.', ',')} км"
    else "${m.toInt()} м"
}

/** Double? ккал → "536 кКал" или "--". */
internal fun formatKcal(kcal: Double?): String =
    if (kcal == null) "--" else "${kcal.toInt()} кКал"

/**
 * Парсит ISO-строку в LocalDateTime.
 * API возвращает формат "2026-05-16T08:44:00.613000Z" (UTC + микросекунды).
 * Попытка 1: OffsetDateTime (основной путь для формата API).
 * Попытка 2: LocalDateTime (без таймзоны).
 * Попытка 3: только время "HH:mm:ss" → добавляем фиктивную дату.
 */
internal fun parseDateTime(iso: String): java.time.LocalDateTime {
    val normalized = iso.trim().replace(' ', 'T')
    return try {
        java.time.OffsetDateTime.parse(normalized).toLocalDateTime()
    } catch (_: Exception) {
        try {
            if (normalized.contains('T')) java.time.LocalDateTime.parse(normalized)
            else java.time.LocalDateTime.parse("1970-01-01T$normalized")
        } catch (_: Exception) {
            throw IllegalArgumentException("Не удалось распарсить дату: $iso")
        }
    }
}

// ── Длительность / агрегация ─────────────────────────────────────────────────

/** Длительность одной тренировки в секундах. 0 при отсутствии времён или ошибке парсинга. */
internal fun TrainingHistoryItem.durationSeconds(): Long {
    val s = timeStart ?: return 0L
    val e = timeEnd ?: return 0L
    return try {
        java.time.Duration.between(parseDateTime(s), parseDateTime(e)).seconds
    } catch (_: Exception) { 0L }
}

/** Сумма длительностей в списке элементов → секунды. */
internal fun totalDurationSeconds(items: List<TrainingHistoryItem>): Long =
    items.sumOf { it.durationSeconds() }

/** Агрегированные итоги периода (день / неделя). */
internal data class PeriodTotals(
    val seconds: Long,
    val distanceM: Double?,
    val kilocalories: Double?,
)

/** Суммирует длительность / дистанцию / калории по списку элементов. */
internal fun aggregateTotals(items: List<TrainingHistoryItem>): PeriodTotals = PeriodTotals(
    seconds = totalDurationSeconds(items),
    distanceM = items.mapNotNull { it.distanceM }.sum().takeIf { it > 0.0 },
    kilocalories = items.mapNotNull { it.kilocalories }.sum().takeIf { it > 0.0 },
)

/** typeActivId самой длинной (по времени) тренировки из списка, или null если пуст. */
internal fun longestTypeIdOf(items: List<TrainingHistoryItem>): Int? =
    items.maxByOrNull { it.durationSeconds() }?.typeActivId

/**
 * Доминирующий тип за период: тип с наибольшим суммарным временем + его доля от общего (в процентах).
 * @return null если список пуст или суммарное время = 0.
 */
internal fun dominantType(items: List<TrainingHistoryItem>): Pair<Int, Float>? {
    val totalSecs = totalDurationSeconds(items)
    if (totalSecs == 0L) return null
    val byType = items.groupBy { it.typeActivId }
        .mapValues { (_, list) -> totalDurationSeconds(list) }
    val dominant = byType.maxByOrNull { it.value } ?: return null
    return dominant.key to (dominant.value.toFloat() / totalSecs * 100f)
}

// ── Вспомогательные функции календаря ────────────────────────────────────────

/**
 * Генерирует список понедельников недель, покрывающих месяц.
 * Первый понедельник может быть в предыдущем месяце.
 */
internal fun generateWeeksForMonth(monthStart: LocalDate): List<LocalDate> {
    val monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth())
    val weeks = mutableListOf<LocalDate>()
    var weekStart = monthStart.with(DayOfWeek.MONDAY)
    val lastWeekStart = monthEnd.with(DayOfWeek.MONDAY)
    while (!weekStart.isAfter(lastWeekStart)) {
        weeks.add(weekStart)
        weekStart = weekStart.plusWeeks(1)
    }
    return weeks
}
