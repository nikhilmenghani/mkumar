package com.mkumar.common.extension

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
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

fun Instant.format(
    format: DateFormat = DateFormat.DATE_ONLY,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    return DateTimeFormatter.ofPattern(format.pattern)
        .format(this.atZone(zoneId))
}

fun Instant.formatAsDateTime(
    format: DateFormat = DateFormat.SHORT_DATE_TIME,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    return this.format(format, zoneId)
}

fun Long.formatAsDate(
    format: DateFormat = DateFormat.DATE_ONLY,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    return this.toInstant().format(format, zoneId)
}

fun Long.formatAsDateTime(
    format: DateFormat = DateFormat.SHORT_DATE_TIME,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    return this.toInstant().format(format, zoneId)
}

fun Long.formatAsDate(): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))
