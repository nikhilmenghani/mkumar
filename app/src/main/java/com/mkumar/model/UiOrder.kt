package com.mkumar.model

/**
 * Full order used for CustomerDetailsScreen list.
 */
data class UiOrder(
    val id: String,
    val invoiceNumber: String,
    val occurredAt: Long,
    val items: List<UiOrderItem>,
    val subtotalBeforeAdjust: Int,
    val adjustedAmount: Int,
    val totalAmount: Int,
    val paidTotal: Int,
    val remainingBalance: Int,
    val lastUpdatedAt: Long
)

