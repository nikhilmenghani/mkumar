package com.mkumar.data.models

data class OrderSummary(
    val id: String,
    val occurredAt: Long,
    val totalAmount: Int,
    val adjustedAmount: Int,
    val remainingBalance: Int,
    val advanceTotal: Int
)
