// ==========================
// MKumar • Phase 4 Alignment Pack
// ViewModel + UI State + Mappers (single-source of truth)
// Consistent with com.mkumar.domain.pricing.Models (discountPercentage, rupees)
// ==========================

// ---------- FILE: com/mkumar/viewmodel/CustomerDetailsUiState.kt ----------
package com.mkumar.viewmodel

import com.mkumar.data.ProductType
import java.time.Instant

/** Lightweight UI customer model */
data class UiCustomer(
    val id: String,
    val name: String,
    val phone: String
)

/** UI order item uses rupees + percentage discount */
data class UiOrderItem(
    val id: String,                 // stable id
    val productType: ProductType,
    val name: String,
    val quantity: Int,
    val unitPrice: Int,             // rupees
    val discountPercentage: Int     // 0..100
)

/**
 * UI order totals aligned to PricingResult fields.
 */
data class UiOrder(
    val id: String,
    val occurredAt: Instant,
    val items: List<UiOrderItem>,
    val subtotalBeforeAdjust: Int,
    val adjustedAmount: Int,
    val totalAmount: Int,
    val advanceTotal: Int,
    val remainingBalance: Int
)

/** Draft used in bottom sheet while composing a new order. */
data class OrderDraft(
    val occurredAt: Instant = Instant.now(),
    val items: List<UiOrderItem> = emptyList(),
    val subtotalBeforeAdjust: Int = 0,
    val adjustedAmount: Int = 0,
    val totalAmount: Int = 0,
    val advanceTotal: Int = 0,
    val remainingBalance: Int = 0,
    val hasUnsavedChanges: Boolean = false
)

/** Screen state */
data class CustomerDetailsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val customer: UiCustomer? = null,
    val orders: List<UiOrder> = emptyList(),
    val isOrderSheetOpen: Boolean = false,
    val draft: OrderDraft = OrderDraft(),
    val errorMessage: String? = null
)

sealed interface CustomerDetailsEffect {
    data class ShowMessage(val message: String) : CustomerDetailsEffect
    data object OpenOrderSheet : CustomerDetailsEffect
    data object CloseOrderSheet : CustomerDetailsEffect
}

sealed interface CustomerDetailsIntent {
    data object Refresh : CustomerDetailsIntent
    data object NewSale : CustomerDetailsIntent
    data object CloseSheet : CustomerDetailsIntent

    data class AddItem(val item: UiOrderItem) : CustomerDetailsIntent
    data class UpdateItem(val item: UiOrderItem) : CustomerDetailsIntent
    data class RemoveItem(val itemId: String) : CustomerDetailsIntent
    data class UpdateOccurredAt(val occurredAt: Instant) : CustomerDetailsIntent

    data object SaveDraftAsOrder : CustomerDetailsIntent
    data object DiscardDraft : CustomerDetailsIntent
}
