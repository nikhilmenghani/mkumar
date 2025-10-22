// app/src/main/java/com/mkumar/data/local/dao/OrderDao.kt
package com.mkumar.data.local.dao

import androidx.room.*
import com.mkumar.data.local.entity.OrderEntity
import com.mkumar.data.local.entity.OrderItemEntity

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY occurredAt DESC")
    suspend fun getOrdersForCustomer(customerId: String): List<OrderEntity>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity>

    @Transaction
    suspend fun insertOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) {
        insert(order)
        insertItems(items)
    }
}
