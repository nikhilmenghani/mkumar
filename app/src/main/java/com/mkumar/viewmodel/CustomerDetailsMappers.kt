package com.mkumar.viewmodel

import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.relations.CustomerWithOrders
import com.mkumar.domain.pricing.PricingInput
import com.mkumar.domain.pricing.PricingResult
import com.mkumar.domain.pricing.PricingService
import java.time.Instant
import java.util.Locale

/** Aggregated UI payload for CustomerDetails screen. */
data class UiBundle(val customer: UiCustomer, val orders: List<UiOrder>)

/**
 * Map repo relation (customer + orders) -> UI models.
 *
 * This version does NOT assume items are embedded in the relation.
 * Provide an [itemsOf] function that returns UI items for a given orderId.
 *
 * @param pricing      PricingService to compute totals (percentage-based discounts)
 * @param itemsOf      Supplier for order items (in RUPEES, 0..100 discountPercentage). Default: empty (no items).
 * @param adjustedOf   Extractor for order-level adjustedAmount (rupees). Default 0.
 * @param advanceOf    Extractor for order-level advanceTotal (rupees). Default 0.
 */
fun CustomerWithOrders.toUi(
    pricing: PricingService,
    itemsOf: (orderId: String) -> List<UiOrderItem> = { emptyList() },
    adjustedOf: (order: OrderEntity) -> Int = { 0 },
    advanceOf:  (order: OrderEntity) -> Int = { 0 }
): UiBundle {
    val uiCustomer = UiCustomer(customer.id, customer.name, customer.phone, customer.createdAt)

    val uiOrders: List<UiOrder> = orders.map { order ->
        // Fetch items from caller-supplied source (DAO/repo/cache)
        val uiItems = itemsOf(order.id)

        val priced: PricingResult = pricing.price(
            uiItems.toPricingInput(
                orderId = order.id,
                adjustedAmount = adjustedOf(order).coerceAtLeast(0),
                advanceTotal   = advanceOf(order).coerceAtLeast(0)
            )
        )

        UiOrder(
            id = order.id,
            occurredAt = Instant.ofEpochMilli(order.occurredAt),
            items = uiItems,
            subtotalBeforeAdjust = priced.subtotalBeforeAdjust,
            adjustedAmount       = order.adjustedAmount,
            totalAmount          = order.totalAmount,
            advanceTotal         = order.advanceTotal,
            remainingBalance     = order.remainingBalance,
            invoiceNumber = order.invoiceSeq?.let { "INV-%d".format(it) } ?: ("INV-" + order.id.takeLast(6).uppercase(Locale.getDefault()))
        )
    }.sortedByDescending { it.occurredAt }

    return UiBundle(uiCustomer, uiOrders)
}

/* ---------------------------- PRICING HELPERS ---------------------------- */

fun List<UiOrderItem>.toPricingInput(
    orderId: String,
    adjustedAmount: Int = 0,
    advanceTotal: Int = 0
): PricingInput = PricingInput(
    orderId = orderId,
    items = this.map { it.toItemInput() },
    adjustedAmount = adjustedAmount.coerceAtLeast(0),
    advanceTotal = advanceTotal.coerceAtLeast(0)
)

fun UiOrderItem.toItemInput(): PricingInput.ItemInput =
    PricingInput.ItemInput(
        itemId = id,
        quantity = quantity,
        unitPrice = unitPrice,                 // rupees
        discountPercentage = discountPercentage
    )

/* ------------------------------ UI Totals ------------------------------- */

data class UiTotals(
    val subtotalBeforeAdjust: Int,
    val adjustedAmount: Int,
    val totalAmount: Int,
    val advanceTotal: Int,
    val remainingBalance: Int
)

fun PricingResult.toUiTotals(): UiTotals =
    UiTotals(
        subtotalBeforeAdjust = subtotalBeforeAdjust,
        adjustedAmount = adjustedAmount,
        totalAmount = totalAmount,
        advanceTotal = advanceTotal,
        remainingBalance = remainingBalance
    )
