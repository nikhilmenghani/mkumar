package com.mkumar.data.mappers

import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.models.OrderSummary

fun OrderEntity.toSummary() = OrderSummary(
    id = id,
    occurredAt = occurredAt,
    totalAmount = totalAmount,
    adjustedAmount = adjustedAmount,
    remainingBalance = remainingBalance,
    advanceTotal = advanceTotal
)
