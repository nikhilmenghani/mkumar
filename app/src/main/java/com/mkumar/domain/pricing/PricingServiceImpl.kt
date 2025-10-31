// com.mkumar.domain.pricing.PricingServiceImpl.kt
package com.mkumar.domain.pricing

import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class PricingServiceImpl @Inject constructor() : PricingService {
    override fun price(input: PricingInput): PricingResult {
        val priced = input.items.map { it ->
            val qty = it.quantity.coerceAtLeast(0)
            val unit = it.unitPrice.coerceAtLeast(0)
            val pct  = it.discountPercentage.coerceIn(0, 100)

            val lineSubtotal = unit * qty
            val lineDiscount = percentOf(lineSubtotal, pct)
            val lineTotal = max(0, lineSubtotal - lineDiscount)

            PricedItem(
                itemId = it.itemId, quantity = qty, unitPrice = unit,
                discountPercentage = pct, lineSubtotal = lineSubtotal,
                lineDiscount = lineDiscount, lineTotal = lineTotal
            )
        }

        val subtotal = priced.sumOf { it.lineTotal }
        val adj = min(input.adjustedAmount.coerceAtLeast(0), subtotal)
        val total = max(0, subtotal - adj)
        val adv = input.advanceTotal.coerceAtLeast(0)
        val remaining = max(0, total - adv)

        return PricingResult(
            orderId = input.orderId,
            items = priced,
            subtotalBeforeAdjust = subtotal,
            adjustedAmount = adj,
            totalAmount = total,
            advanceTotal = adv,
            remainingBalance = remaining
        )
    }

    private fun percentOf(base: Int, pct: Int): Int {
        if (pct <= 0) return 0
        if (pct >= 100) return base
        val bd = BigDecimal(base).multiply(BigDecimal(pct)).divide(BigDecimal(100))
        return bd.setScale(0, RoundingMode.HALF_UP).intValueExact()
    }
}
