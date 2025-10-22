package com.mkumar.data

data class OrderSummaryDomain(
    val id: String,
    val subtitle: String,        // "Glasses + Case" or "3 items"
    val occurredAt: java.time.Instant,
    val totalFormatted: String?, // null when draft
    val isDraft: Boolean
)