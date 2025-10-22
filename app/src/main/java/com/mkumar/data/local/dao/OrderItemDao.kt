package com.mkumar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mkumar.data.local.entities.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {

    /**
     * Insert or replace a list of order items.
     * Usually called when saving an order with items in one transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OrderItemEntity>)

    /**
     * Insert or replace a single item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OrderItemEntity)

    /**
     * Update an existing order item (used rarely since we usually replace whole order set).
     */
    @Update
    suspend fun update(item: OrderItemEntity)

    /**
     * Delete all items belonging to a specific order.
     */
    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteByOrderId(orderId: String)

    /**
     * Fetch all items for a given order as Flow for reactive UI binding.
     */
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun observeItemsForOrder(orderId: String): Flow<List<OrderItemEntity>>

    /**
     * Fetch all items for a given order once (non-reactive).
     */
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity>
}
