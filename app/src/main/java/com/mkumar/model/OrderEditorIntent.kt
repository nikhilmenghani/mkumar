package com.mkumar.model

import com.mkumar.data.ProductFormData
import java.time.Instant

// -----------------------------------------------------
// INTENTS
// -----------------------------------------------------

sealed class OrderEditorIntent {
    data class AddItem(val type: ProductType) : OrderEditorIntent()
    data class UpdateItem(val itemId: String, val newData: UiOrderItem) : OrderEditorIntent()
    data class DeleteItem(val itemId: String) : OrderEditorIntent()
    data class UpdateAdjustedAmount(val value: Int) : OrderEditorIntent()
    data class UpdateAdvanceTotal(val value: Int) : OrderEditorIntent()
    object SaveOrder : OrderEditorIntent()
    data class UpdateOccurredAt(val occurredAt: Instant) : OrderEditorIntent()
    data class AddPayment(val orderId: String, val amountPaid: Int, val paymentAt: Long) : OrderEditorIntent()
    data class DeletePayment(val paymentId: String) : OrderEditorIntent()
}

sealed interface NewOrderIntent {
    data object ConsumeJustAdded : NewOrderIntent
    data object Save : NewOrderIntent
    data class SelectType(val type: ProductType) : NewOrderIntent
    data class FormUpdate(val productId: String, val newData: ProductFormData) : NewOrderIntent
    data class FormDelete(val productId: String) : NewOrderIntent
}

sealed interface OrderEditorEffect {
    data object CloseEditor : OrderEditorEffect
    data class ShowMessage(val message: String) : OrderEditorEffect
}