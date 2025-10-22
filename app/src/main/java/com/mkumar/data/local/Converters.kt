package com.mkumar.data.local

import androidx.room.TypeConverter
import java.time.Instant

object Converters {
    @TypeConverter
    @JvmStatic
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    @JvmStatic
    fun epochMillisToInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }
}
