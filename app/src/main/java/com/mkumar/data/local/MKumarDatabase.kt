// data/local/MKumarDatabase.kt
package com.mkumar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mkumar.data.local.dao.*
import com.mkumar.data.local.entities.*

@Database(
    entities = [
        CustomerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        SearchFts::class, // if using FTS
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MKumarDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun searchDao(): SearchDao
}
