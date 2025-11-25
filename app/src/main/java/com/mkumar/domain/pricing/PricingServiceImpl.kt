// com.mkumar.domain.pricing.PricingServiceImpl.kt
package com.mkumar.domain.pricing

import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.max

class PricingServiceImpl @Inject constructor() : PricingService {

    override fun price(input: PricingInput): PricingResult {

        val priced = input.items.map { it ->
            val qty = it.quantity.coerceAtLeast(0)
            val unit = it.unitPrice.coerceAtLeast(0)
            val pct  = it.discountPercentage.coerceIn(0, 100)

            val lineSubtotal = qty * unit
            val lineDiscount = (lineSubtotal * pct) / 100
            val lineTotal = max(0, lineSubtotal - lineDiscount)

            PricedItem(
                itemId = it.itemId,
                quantity = qty,
                unitPrice = unit,
                discountPercentage = pct,
                lineSubtotal = lineSubtotal,
                lineDiscount = lineDiscount,
                lineTotal = lineTotal
            )
        }

        val subtotal = priced.sumOf { it.lineTotal }

        return PricingResult(
            orderId = input.orderId,
            items = priced,
            subtotalBeforeAdjust = subtotal,
            adjustedAmount = input.adjustedAmount,   // no clamping
            totalAmount = subtotal,                  // ALWAYS actual total
            paidTotal = input.paidTotal,
            remainingBalance = subtotal - input.paidTotal
        )
    }

    private fun percentOf(base: Int, pct: Int): Int {
        if (pct <= 0) return 0
        if (pct >= 100) return base
        val bd = BigDecimal(base).multiply(BigDecimal(pct)).divide(BigDecimal(100))
        return bd.setScale(0, RoundingMode.HALF_UP).intValueExact()
    }
}
