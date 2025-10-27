package com.mkumar.data

data class OrderSummaryUi(
    val id: String,
    val invoiceShort: String,       // e.g., "INV-2F9C1A"
    val subtitle: String,           // e.g., "Glasses + Case" or "3 items"
    val timeFormatted: String,      // e.g., "06:12 PM"
    val advanceTotal: Int?,
    val remainingBalance: Int?,
    val totalAmount: Int?,
    val adjustedAmount: Int?,
    val isDraft: Boolean,
    val products: List<ProductEntry> = emptyList(),
    val selectedProductId: String? = null
)