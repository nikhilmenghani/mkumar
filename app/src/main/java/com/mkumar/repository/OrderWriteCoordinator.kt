package com.mkumar.repository

import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity

interface OrderWriteCoordinator {
    /**
     * Replaces items of an order atomically and recalculates amounts.
     * If order doesn't exist, it will be created first (id must be stable).
     */
    suspend fun replaceItemsAndRecalculate(
        order: OrderEntity,
        items: List<OrderItemEntity>
    )
}
