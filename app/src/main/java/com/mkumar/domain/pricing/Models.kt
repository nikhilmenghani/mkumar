// com.mkumar.domain.pricing.Models.kt
package com.mkumar.domain.pricing

data class PricingInput(
    val orderId: String,
    val items: List<ItemInput>,
    val adjustedAmount: Int,   // final payable-total override; 0 means use calculated total
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
    val adjustedAmount: Int,       // final payable-total override; 0 means none
    val totalAmount: Int,          // calculated item total before the override
    val paidTotal: Int,         // clamped >= 0
    val remainingBalance: Int      // effective total (adjusted or calculated) - paid total
)
