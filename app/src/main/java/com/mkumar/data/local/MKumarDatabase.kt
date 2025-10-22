// app/src/main/java/com/mkumar/data/local/MKumarDatabase.kt
package com.mkumar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mkumar.data.local.dao.CustomerDao
import com.mkumar.data.local.dao.OrderDao
import com.mkumar.data.local.dao.SearchDao
import com.mkumar.data.local.entity.CustomerEntity
import com.mkumar.data.local.entity.OrderEntity
import com.mkumar.data.local.entity.OrderItemEntity
import com.mkumar.data.local.entity.OutboxEntity
import com.mkumar.data.local.entity.SearchFts

@Database(
    entities = [
        CustomerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        OutboxEntity::class,
        SearchFts::class
    ],
    version = 1
)

abstract class MKumarDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun searchDao(): SearchDao
}
