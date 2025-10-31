// com.mkumar.domain.invoice.InvoiceData.kt
package com.mkumar.domain.invoice

import java.time.LocalDateTime

data class InvoiceData(
    val shopName: String,
    val shopAddress: String?,
    val shopPhone: String?,
    val customerName: String,
    val customerPhone: String?,
    val orderId: String,
    val occurredAt: LocalDateTime,
    val items: List<InvoiceItem>,
    val subtotalBeforeAdjust: Int, // sum of discounted line totals
    val adjustedAmount: Int,
    val advanceTotal: Int,
    val totalAmount: Int,
    val remainingBalance: Int,
    val currencySymbol: String = "₹"
) {
    data class InvoiceItem(
        val name: String,
        val quantity: Int,
        val unitPrice: Int,
        val discountPercentage: Int,
        val lineTotal: Int
    )
}
