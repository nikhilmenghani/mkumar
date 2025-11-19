package com.mkumar.model

import java.time.Instant

// -----------------------------------------------------
// UI STATE
// -----------------------------------------------------

data class OrderEditorUi(
    val customer: UiCustomer? = null,
    val orders: List<UiOrder> = emptyList(),
    val isLoading: Boolean = true,
    val draft: Draft = Draft()
) {
    data class Draft(
        val orderId: String = "",
        val customerId: String = "",
        val occurredAt: Instant = Instant.now(),
        val invoiceNumber: Long = 0,
        val items: List<UiOrderItem> = emptyList(),
        val subtotalBeforeAdjust: Int = 0,
        val adjustedAmount: Int = 0,
        val totalAmount: Int = 0,
        val advanceTotal: Int = 0,
        val remainingBalance: Int = 0,
        val hasUnsavedChanges: Boolean = false,
        val editingOrderId: String? = null,
        val justAddedItemId: String? = null
    )
}
