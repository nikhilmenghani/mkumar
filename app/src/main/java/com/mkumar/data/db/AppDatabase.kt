package com.mkumar.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mkumar.data.db.converters.OrderStatusConverter
import com.mkumar.data.db.converters.StringListConverter
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.InvoiceCounterDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.dao.SearchDao
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.entities.InvoiceCounterEntity
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.data.db.entities.OutboxEntity
import com.mkumar.data.db.entities.SearchFts
import com.mkumar.data.utils.Converters

@Database(
    entities = [
        CustomerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        OutboxEntity::class,
        SearchFts::class,
        InvoiceCounterEntity::class,
    ],
    version = 1,
    exportSchema = true
)

@TypeConverters(Converters::class, OrderStatusConverter::class, StringListConverter::class,)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun searchDao(): SearchDao
    abstract fun invoiceCounterDao(): InvoiceCounterDao
}
