// com.mkumar.domain.pricing.PricingMappers.kt
package com.mkumar.domain.pricing

import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity

fun buildPricingInput(order: OrderEntity, items: List<OrderItemEntity>): PricingInput {
    return PricingInput(
        orderId = order.id,
        adjustedAmount = (order.adjustedAmount ?: 0).coerceAtLeast(0),
        advanceTotal = order.advanceTotal.coerceAtLeast(0),
        items = items.map {
            PricingInput.ItemInput(
                itemId = it.id,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                discountPercentage = it.discountPercentage.coerceIn(0, 100)
            )
        }
    )
}

/** Update the OrderEntity totals from the priced result. */
fun OrderEntity.withTotals(from: PricingResult): OrderEntity {
    return this.copy(
        // Your entity names:
        adjustedAmount = from.adjustedAmount,
        totalAmount = from.totalAmount,
        remainingBalance = from.remainingBalance
    )
}
