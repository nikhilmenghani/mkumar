//// data/local/MKumarDatabase.kt
//package com.mkumar.data.local
//
//import androidx.room.Database
//import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
//import com.mkumar.data.db.dao.CustomerDao
//import com.mkumar.data.db.dao.OrderDao
//import com.mkumar.data.db.dao.OrderItemDao
//import com.mkumar.data.db.dao.SearchDao
//import com.mkumar.data.db.entities.OrderEntity
//import com.mkumar.data.db.entities.CustomerEntity
//import com.mkumar.data.db.entities.OrderItemEntity
//import com.mkumar.data.db.entities.SearchFts
//import com.mkumar.data.utils.Converters
//
//@Database(
//    entities = [
//        CustomerEntity::class,
//        OrderEntity::class,
//        OrderItemEntity::class,
//        SearchFts::class, // if using FTS
//    ],
//    version = 1,
//    exportSchema = true
//)
//@TypeConverters(Converters::class)
//abstract class MKumarDatabase : RoomDatabase() {
//    abstract fun customerDao(): CustomerDao
//    abstract fun orderDao(): OrderDao
//    abstract fun orderItemDao(): OrderItemDao
//    abstract fun searchDao(): SearchDao
//}
