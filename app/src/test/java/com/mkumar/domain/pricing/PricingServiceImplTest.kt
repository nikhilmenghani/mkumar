package com.mkumar.domain.pricing

import org.junit.Assert.assertEquals
import org.junit.Test

class PricingServiceImplTest {
    private val service = PricingServiceImpl()

    @Test
    fun adjustedTotalOverridesCalculatedTotalForRemainingBalance() {
        val result = service.price(
            PricingInput(
                orderId = "order",
                items = listOf(
                    PricingInput.ItemInput(
                        itemId = "item",
                        quantity = 1,
                        unitPrice = 2_000,
                        discountPercentage = 0
                    )
                ),
                adjustedAmount = 1_900,
                paidTotal = 1_000
            )
        )

        assertEquals(2_000, result.totalAmount)
        assertEquals(1_900, result.adjustedAmount)
        assertEquals(900, result.remainingBalance)
    }

    @Test
    fun zeroAdjustmentUsesCalculatedTotalForRemainingBalance() {
        val result = service.price(
            PricingInput(
                orderId = "order",
                items = listOf(
                    PricingInput.ItemInput("item", 1, 2_000, 0)
                ),
                adjustedAmount = 0,
                paidTotal = 1_000
            )
        )

        assertEquals(1_000, result.remainingBalance)
    }
}
