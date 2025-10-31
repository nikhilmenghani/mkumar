package com.mkumar.data.utils

import androidx.room.TypeConverter
import com.mkumar.data.ProductType
import java.time.Instant

object Converters {
    @TypeConverter
    @JvmStatic
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    @JvmStatic
    fun epochMillisToInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

//    @TypeConverter
//    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()
//
//    @TypeConverter
//    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

//    @TypeConverter
//    fun fromProductType(type: ProductType?): String? = type?.name
//
//    @TypeConverter
//    fun toProductType(value: String?): ProductType? = value?.let { ProductType.valueOf(it) }
}