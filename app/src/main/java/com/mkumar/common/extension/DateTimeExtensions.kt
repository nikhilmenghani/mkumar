package com.mkumar.common.extension

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun Long.toInstant(): Instant = Instant.ofEpochMilli(this)
fun Instant.toEpochMillis(): Long = this.toEpochMilli()
fun Instant.toLong(): Long = this.toEpochMilli()

enum class DateFormat(val pattern: String) {
    SHORT_DATE_TIME("MMM d, h:mm a"),
    FULL_DATE_TIME("MMM d, yyyy h:mm a"),
    DATE_ONLY("MMM d, yyyy"),
    DEFAULT_DATE_ONLY("dd-MMM-yyyy"),
    TIME_ONLY("h:mm a"),
    ISO_DATE_TIME("yyyy-MM-dd'T'HH:mm:ss"),
    DEFAULT_DATE_TIME("dd-MM-yyyy HH:mm")
}

/* ----------------------------------------------------------
 *  FORMATTERS
 * ---------------------------------------------------------- */

fun Instant.format(
    format: DateFormat = DateFormat.DATE_ONLY,
    zoneId: ZoneId = ZoneId.systemDefault()
): String =
    DateTimeFormatter.ofPattern(format.pattern)
        .format(this.atZone(zoneId))

fun Instant.formatAsDateTime(
    format: DateFormat = DateFormat.SHORT_DATE_TIME,
    zoneId: ZoneId = ZoneId.systemDefault()
): String = format(format, zoneId)

fun Long.formatAsDate(
    format: DateFormat = DateFormat.DATE_ONLY,
    zoneId: ZoneId = ZoneId.systemDefault()
): String = this.toInstant().format(format, zoneId)

fun Long.formatAsDateTime(
    format: DateFormat = DateFormat.SHORT_DATE_TIME,
    zoneId: ZoneId = ZoneId.systemDefault()
): String = this.toInstant().format(format, zoneId)

fun Long.formatAsDate(): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))

/* ----------------------------------------------------------
 *  LocalDate → Instant & Long
 * ---------------------------------------------------------- */

/**
 * Convert LocalDate to UTC Instant at midnight.
 * This is the safest way to store dates globally.
 */
fun LocalDate.toUtcInstant(): Instant =
    this.atStartOfDay(ZoneOffset.UTC).toInstant()

/**
 * Convert LocalDate to Instant at local midnight.
 * Use this only if business = device's region.
 */
fun LocalDate.toLocalMidnightInstant(
    zoneId: ZoneId = ZoneId.systemDefault()
): Instant =
    this.atStartOfDay(zoneId).toInstant()

fun LocalDate.toLocalInstant(): Instant =
    this.atStartOfDay(ZoneId.systemDefault()).toInstant()


fun LocalDate.toEpochLong(): Long = this.toUtcInstant().toEpochMilli()

/**
 * Format a LocalDate safely using local timezone.
 */
fun LocalDate.formatAsDate(
    format: DateFormat = DateFormat.DEFAULT_DATE_ONLY,
    zoneId: ZoneId = ZoneId.systemDefault()
): String =
    DateTimeFormatter.ofPattern(format.pattern)
        .format(this.atStartOfDay(zoneId))

/* ----------------------------------------------------------
 *  Instant → LocalDate
 * ---------------------------------------------------------- */

/** Convert UTC Instant → LocalDate in device's timezone. */
fun Instant.toLocalDate(
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalDate =
    this.atZone(zoneId).toLocalDate()

/** Convert millis → LocalDate */
fun Long.toLocalDate(
    zoneId: ZoneId = ZoneId.systemDefault()
): LocalDate =
    this.toInstant().toLocalDate(zoneId)

/* ----------------------------------------------------------
 *  Helpers for global correctness
 * ---------------------------------------------------------- */

/**
 * Convert Instant.now() → LocalDate (local timezone)
 * Use when UI wants to show today's date.
 */
fun todayLocalDate(): LocalDate =
    Instant.now().toLocalDate()

/**
 * Create a UTC Instant for "now"
 * Use for DB storage if you want global consistency.
 */
fun nowUtcInstant(): Instant = Instant.now()

/**
 * Create "today" as UTC Instant at midnight
 * Useful when the app cares about date-only storage.
 */
fun todayUtcMidnight(): Instant =
    LocalDate.now(ZoneOffset.UTC).toUtcInstant()

/**
 * Legacy replacement for System.currentTimeMillis()
 */
fun nowUtcMillis(): Long = Instant.now().toEpochMilli()
