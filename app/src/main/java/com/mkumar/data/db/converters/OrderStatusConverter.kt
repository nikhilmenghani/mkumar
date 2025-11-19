package com.mkumar.data.db.converters

import androidx.room.TypeConverter
import com.mkumar.model.OrderStatus

class OrderStatusConverter {

    @TypeConverter
    fun toString(status: OrderStatus?): String =
        status?.value ?: OrderStatus.DRAFT.value

    @TypeConverter
    fun fromString(value: String?): OrderStatus =
        OrderStatus.from(value)
}
