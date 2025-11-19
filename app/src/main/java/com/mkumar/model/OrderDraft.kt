package com.mkumar.model

import java.time.Instant

/**
 * Live editing state used ONLY in OrderEditorViewModel.
 */
data class OrderDraft(
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
