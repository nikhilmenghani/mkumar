package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.mkumar.data.db.entities.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {


    @Upsert
    suspend fun upsert(order: OrderItemEntity)
    /**
     * Insert or replace a list of order items.
     * Usually called when saving an order with items in one transaction.
     */
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(items: List<OrderItemEntity>)

    /**
     * Insert or replace a single item.
     */
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
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
     * Delete an existing order item.
     */
    @Query("DELETE FROM order_items WHERE id = :itemId")
    suspend fun deleteProductById(itemId: String)

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

    @Query("SELECT COUNT(*) FROM order_items WHERE orderId = :orderId")
    fun countForOrder(orderId: String): Int
}