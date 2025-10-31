package com.mkumar.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mkumar.data.db.dao.*
import com.mkumar.data.db.entities.*
import com.mkumar.data.utils.Converters

@Database(
    entities = [
        CustomerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        OutboxEntity::class,
        SearchFts::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun searchDao(): SearchDao
}
