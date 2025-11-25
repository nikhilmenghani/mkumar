// com.mkumar.domain.pricing.Models.kt
package com.mkumar.domain.pricing

data class PricingInput(
    val orderId: String,
    val items: List<ItemInput>,
    val adjustedAmount: Int,   // >= 0 rupees, flat reduction
    val paidTotal: Int      // >= 0 rupees
) {
    data class ItemInput(
        val itemId: String,
        val quantity: Int,
        val unitPrice: Int,          // rupees
        val discountPercentage: Int  // 0..100
    )
}

data class PricedItem(
    val itemId: String,
    val quantity: Int,
    val unitPrice: Int,
    val discountPercentage: Int,
    val lineSubtotal: Int,   // unitPrice * quantity
    val lineDiscount: Int,   // HALF_UP(lineSubtotal * pct/100)
    val lineTotal: Int       // max(0, lineSubtotal - lineDiscount)
)

data class PricingResult(
    val orderId: String,
    val items: List<PricedItem>,
    val subtotalBeforeAdjust: Int, // sum(lineTotal)
    val adjustedAmount: Int,       // clamped to [0..subtotal]
    val totalAmount: Int,          // subtotal - adjustedAmount
    val paidTotal: Int,         // clamped >= 0
    val remainingBalance: Int      // max(0, totalAmount - advanceTotal)
)
