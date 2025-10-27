package com.mkumar.data

data class OrderSummaryDomain(
    val id: String,
    val subtitle: String,        // "Glasses + Case" or "3 items"
    val occurredAt: java.time.Instant,
    val advanceTotal: Int?,
    val remainingBalance: Int?,
    val totalAmount: Int?,
    val adjustedAmount: Int?,
    val isDraft: Boolean,
    val products: List<ProductEntry> = emptyList(),
)