package com.mkumar.data.mappers

import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.models.OrderSummary

fun OrderEntity.toSummary() = OrderSummary(
    id = id,
    occurredAt = occurredAt,
    totalAmount = totalAmount ?: 0,
    adjustedAmount = adjustedAmount ?: 0,
    remainingBalance = remainingBalance ?: 0,
    advanceTotal = advanceTotal
)
