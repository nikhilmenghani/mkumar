package com.mkumar.repository

import com.mkumar.data.db.entities.OrderItemEntity
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun upsert(item: OrderItemEntity)
    suspend fun insertAll(items: List<OrderItemEntity>)
    suspend fun deleteByOrderId(orderId: String)
    suspend fun deleteProductById(itemId: String)
    fun observeItemsForOrder(orderId: String): Flow<List<OrderItemEntity>>
    suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity>
}
