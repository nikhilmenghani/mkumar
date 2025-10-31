package com.mkumar.repository.impl

import androidx.room.withTransaction
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.repository.OrderWriteCoordinator
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class OrderWriteCoordinatorImpl @Inject constructor(
    private val db: AppDatabase
) : OrderWriteCoordinator {

    override suspend fun replaceItemsAndRecalculate(
        order: OrderEntity,
        items: List<OrderItemEntity>
    ) {
        db.withTransaction {
            // Ensure order row exists
            db.orderDao().upsert(order)

            // Replace all items for order
            db.orderItemDao().deleteByOrderId(order.id)
            if (items.isNotEmpty()) db.orderItemDao().insertAll(items)

            // Recalculate totals from items (minor units)
            val subtotal = items.sumOf { max(0, it.unitPrice) * max(1, it.quantity) }
            val adjusted = order.adjustedAmount ?: 0
            val advance = order.advanceTotal
            val grand = (subtotal - adjusted).coerceAtLeast(0)
            val remaining = (grand - advance).coerceAtLeast(0)

            val updated = order.copy(
                totalAmount = subtotal,
                // adjustedAmount already set on incoming order
                remainingBalance = remaining
            )
            db.orderDao().upsert(updated)
        }
    }
}
