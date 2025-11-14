package com.mkumar.viewmodel


import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Immutable
import com.mkumar.data.ProductFormData
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

enum class ProductType { Glass, Frame, Lens, GeneralProduct }

val productTypeDisplayNames = mapOf(
    ProductType.Lens to "Lens",
    ProductType.Frame to "Frame",
    ProductType.Glass to "Glass",
    ProductType.GeneralProduct to "General Product"
)

val productTypeLabelDisplayNames = mapOf(
    "Lens" to "Lens",
    "Frame" to "Frame",
    "Glass" to "Glass",
    "GeneralProduct" to "General Product"
)

// All the elements that we want to display on the Customer Details Screen in Order Card goes below
@Immutable
data class CustomerHeaderUi(
    val customer: UiCustomer?,
    val totalOrders: Int,
    val totalSpent: Int,
    val totalRemaining: Int,
)

/** Lightweight UI customer model */
data class UiCustomer(
    val id: String,
    val name: String,
    val phone: String,
    val createdAt: Long
)


// All the elements that we want to display on the Customer Details Screen in Order Card goes below
@Immutable
data class OrderRowUi(
    val id: String,
    val occurredAt: Long,
    val lastUpdatedAt: Long,
    val invoiceNumber: String,
    val amount: Int,
    val remainingBalance: Int,
    val adjustedTotal: Int? = null,               // if != 0 -> use as Total
)

/**
 * UI order totals aligned to PricingResult fields.
 */
data class UiOrder(
    val id: String,
    val invoiceNumber: String,
    val occurredAt: Long,
    val items: List<UiOrderItem>,
    val subtotalBeforeAdjust: Int,
    val adjustedAmount: Int,
    val totalAmount: Int,
    val advanceTotal: Int,
    val remainingBalance: Int,
    val lastUpdatedAt: Long
)

/** UI order item uses rupees + percentage discount */
data class UiOrderItem(
    val id: String = UUID.randomUUID().toString(),
    val productType: ProductType,
    val productDescription: String = "",
    val formData: ProductFormData? = null,
    val name: String,
    val quantity: Int,
    val unitPrice: Int,
    val discountPercentage: Int,
    val finalTotal: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun serializeFormData(): String? {
        return formData?.let { Json.encodeToString(it) }
    }

    companion object {
        fun deserializeFormData(json: String?): ProductFormData? {
            return json?.let { Json.decodeFromString<ProductFormData>(it) }
        }
    }
}

/** Draft used in bottom sheet while composing a new order. */
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
    data class OpenOrderSheet(val orderId: String? = null) : CustomerDetailsEffect
    data class ViewInvoice(val orderId: String, val invoiceNumber: String, val uri: Uri) : CustomerDetailsEffect
    data class ShareInvoice(val orderId: String, val uri: Uri) : CustomerDetailsEffect
    data object CloseOrderSheet : CustomerDetailsEffect
}


sealed interface CustomerDetailsIntent {
    data object Refresh : CustomerDetailsIntent
    data object NewSale : CustomerDetailsIntent
    data object CloseSheet : CustomerDetailsIntent

    data class OpenOrder(val orderId: String) : CustomerDetailsIntent
    data class UpdateOrder(val orderId: String) : CustomerDetailsIntent
    data class UpdateAdjustedAmount(val value: Int) : CustomerDetailsIntent
    data class UpdateAdvanceTotal(val value: Int) : CustomerDetailsIntent
    data class DeleteOrder(val orderId: String) : CustomerDetailsIntent
    data class ShareOrder(val orderId: String, val invoiceNumber: String, val logo: Bitmap) : CustomerDetailsIntent
    data class ViewInvoice(val orderId: String, val invoiceNumber: String, val logo: Bitmap) : CustomerDetailsIntent

    data class AddItem(val product: ProductType) : CustomerDetailsIntent
//    data class UpdateItem(val item: UiOrderItem) : CustomerDetailsIntent
//    data class RemoveItem(val itemId: String) : CustomerDetailsIntent
    data class UpdateOccurredAt(val occurredAt: Instant) : CustomerDetailsIntent

    data object SaveDraftAsOrder : CustomerDetailsIntent
    data object DiscardDraft : CustomerDetailsIntent
}

sealed interface OrderRowAction {
    data class Open(val orderId: String) : OrderRowAction
    data class ViewInvoice(val orderId: String, val invoiceNumber: String) : OrderRowAction
    data class Delete(val orderId: String) : OrderRowAction
    data class Share(val orderId: String, val invoiceNumber: String) : OrderRowAction
}

sealed interface NewOrderIntent {
    data object ConsumeJustAdded : NewOrderIntent
    data object Save : NewOrderIntent
    data class SelectType(val type: ProductType) : NewOrderIntent
    data class FormUpdate(val productId: String, val newData: ProductFormData) : NewOrderIntent
    data class FormDelete(val productId: String) : NewOrderIntent
}