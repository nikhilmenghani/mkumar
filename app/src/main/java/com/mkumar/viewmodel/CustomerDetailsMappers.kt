package com.mkumar.viewmodel

import com.mkumar.App.Companion.globalClass
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.data.db.relations.CustomerWithOrders
import com.mkumar.domain.pricing.PricingInput
import com.mkumar.domain.pricing.PricingResult
import com.mkumar.domain.pricing.PricingService
import com.mkumar.model.OrderRowUi
import com.mkumar.model.ProductType
import com.mkumar.model.UiCustomer
import com.mkumar.model.UiOrder
import com.mkumar.model.UiOrderItem
import java.util.Locale
import java.util.UUID

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
    advanceOf: (order: OrderEntity) -> Int = { 0 }
): UiBundle {
    val uiCustomer = UiCustomer(customer.id, customer.name, customer.phone, customer.createdAt)

    val uiOrders: List<UiOrder> = orders.map { order ->
        // Fetch items from caller-supplied source (DAO/repo/cache)
        val uiItems = itemsOf(order.id)

        val priced: PricingResult = pricing.price(
            uiItems.toPricingInput(
                orderId = order.id,
                adjustedAmount = adjustedOf(order).coerceAtLeast(0),
                paidTotal = advanceOf(order).coerceAtLeast(0)
            )
        )
        order.toUiOrder(uiItems, priced.subtotalBeforeAdjust)
    }.sortedByDescending { it.receivedAt }

    return UiBundle(uiCustomer, uiOrders)
}

fun UiOrder.toOrderRowUi(): OrderRowUi =
    OrderRowUi(
        id = id,
        receivedAt = receivedAt,
        invoiceNumber = invoiceNumber,
        amount = totalAmount,
        remainingBalance = remainingBalance,
        adjustedTotal = adjustedAmount,
        lastUpdatedAt = lastUpdatedAt
    )

fun UiOrderItem.toItemInput(): PricingInput.ItemInput =
    PricingInput.ItemInput(
        itemId = id,
        quantity = quantity,
        unitPrice = unitPrice,
        discountPercentage = discountPercentage
    )

/* ---------------------------- PRICING HELPERS ---------------------------- */

fun List<UiOrderItem>.toPricingInput(
    orderId: String,
    adjustedAmount: Int = 0,
    paidTotal: Int = 0
): PricingInput = PricingInput(
    orderId = orderId,
    items = this.map { it.toItemInput() },
    adjustedAmount = adjustedAmount.coerceAtLeast(0),
    paidTotal = paidTotal.coerceAtLeast(0)
)

fun UiOrderItem.toEntity(orderId: String): OrderItemEntity {
    val serializedFormData = serializeFormData()
    return OrderItemEntity(
        id = id.ifBlank { UUID.randomUUID().toString() },
        orderId = orderId,
        quantity = quantity,
        unitPrice = unitPrice,
        discountPercentage = discountPercentage.coerceIn(0, 100),
        productTypeLabel = productType.toString(),
        productOwnerName = formData?.productOwner ?: "Error",
        formDataJson = serializedFormData,
        finalTotal = finalTotal,
        updatedAt = nowUtcMillis()
    )
}

fun OrderItemEntity.toUiItem(): UiOrderItem {
    val deserializedFormData = UiOrderItem.deserializeFormData(formDataJson)
    return UiOrderItem(
        id = id,
        quantity = quantity,
        unitPrice = unitPrice,
        discountPercentage = discountPercentage,
        productType = ProductType.valueOf(productTypeLabel),
        name = deserializedFormData?.productOwner ?: productOwnerName,
        formData = deserializedFormData,
        finalTotal = finalTotal,
        productDescription = deserializedFormData?.productDescription ?: "",
        updatedAt = updatedAt
    )
}

fun OrderEntity.toUiOrder(items: List<UiOrderItem> = emptyList(), subtotalBeforeAdjust: Int = 0): UiOrder =
    UiOrder(
        id = id,
        receivedAt = receivedAt,
        items = items,
        subtotalBeforeAdjust = subtotalBeforeAdjust,
        adjustedAmount = adjustedAmount,
        totalAmount = totalAmount,
        paidTotal = paidTotal,
        remainingBalance = remainingBalance,
        lastUpdatedAt = updatedAt,
        invoiceNumber = invoiceSeq?.let { globalClass.preferencesManager.invoicePrefs.invoicePrefix + "%d".format(it) } ?: (globalClass.preferencesManager.invoicePrefs.invoicePrefix + id.takeLast(6)
            .uppercase(Locale.getDefault()))
    )