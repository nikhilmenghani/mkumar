package com.mkumar.data.db.converters

import androidx.room.TypeConverter

/**
 * Used for:
 * - productCategories: List<String>
 * - owners: List<String>
 */
class StringListConverter {

    @TypeConverter
    fun toStored(value: List<String>?): String =
        value?.joinToString("|||") ?: ""

    @TypeConverter
    fun fromStored(value: String?): List<String> =
        value?.takeIf { it.isNotBlank() }
            ?.split("|||")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
}
