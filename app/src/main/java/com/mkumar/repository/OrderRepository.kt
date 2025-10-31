package com.mkumar.repository

import com.mkumar.data.db.entities.OrderEntity
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun upsert(order: OrderEntity)
    suspend fun delete(orderId: String)
    fun observeOrdersForCustomer(customerId: String): Flow<List<OrderEntity>>
    fun observeOrder(orderId: String): Flow<OrderEntity?>
    suspend fun getOrder(orderId: String): OrderEntity?
}
