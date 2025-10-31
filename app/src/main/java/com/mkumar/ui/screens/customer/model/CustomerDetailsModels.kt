package com.mkumar.ui.screens.customer.model

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
data class CustomerHeaderUi(
    val id: String,
    val name: String,
    val phoneFormatted: String,
    val joinedAt: Instant?,
    val totalOrders: Int,
    val totalSpent: Int, // rupees minor units
)

@Immutable
data class OrderRowUi(
    val id: String,
    val occurredAt: Instant,
    val itemsLabel: String,
    val amount: Int,
    val isQueued: Boolean,     // not yet synced
    val isSynced: Boolean,     // successfully synced
    val hasInvoice: Boolean,
)

@Immutable
data class OrderFilterUi(
    val query: String = "",
    val sortNewestFirst: Boolean = true,
)

@Immutable
data class CustomerDetailsUiState(
    val isLoading: Boolean = false,
    val header: CustomerHeaderUi = CustomerHeaderUi("", "", "", null, 0, 0),
    val orders: List<OrderRowUi> = emptyList(),
    val filter: OrderFilterUi = OrderFilterUi(),
    val newOrder: NewOrderUi = NewOrderUi()
)

// New Order creation flow (bottom sheet)
@Immutable
data class NewOrderUi(
    val selectedType: ProductType? = null,
    val lens: LensFormState = LensFormState(),
    val frame: FrameFormState = FrameFormState(),
    val contactLens: ContactLensFormState = ContactLensFormState(),
    val canSave: Boolean = false,
    val saving: Boolean = false,
)

enum class ProductType { LENS, FRAME, CONTACT_LENS }

sealed interface NewOrderIntent {
    data object Save : NewOrderIntent
    data class SelectType(val type: ProductType) : NewOrderIntent
    data class LensChanged(val state: LensFormState) : NewOrderIntent
    data class FrameChanged(val state: FrameFormState) : NewOrderIntent
    data class ContactLensChanged(val state: ContactLensFormState) : NewOrderIntent
}

sealed interface CreateOrderResult {
    data object NoChanges : CreateOrderResult
    data class Success(val orderId: String) : CreateOrderResult
    data class Error(val reason: String?) : CreateOrderResult
}

// Lightweight ViewModel contract so UI compiles
interface CustomerDetailsContract {
    val ui: kotlinx.coroutines.flow.StateFlow<CustomerDetailsUiState>

    fun startNewOrder()
    fun onNewOrderIntent(intent: NewOrderIntent)
    suspend fun createOrder(): CreateOrderResult

    fun onFilterChange(newFilter: OrderFilterUi)
    fun refreshFromCloud()
    fun onOrderAction(action: OrderRowAction)
}

sealed interface OrderRowAction {
    data class ViewInvoice(val orderId: String) : OrderRowAction
    data class Delete(val orderId: String) : OrderRowAction
    data class Share(val orderId: String) : OrderRowAction
}