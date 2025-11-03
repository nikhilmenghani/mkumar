package com.mkumar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.ProductFormData
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.domain.pricing.PricingInput
import com.mkumar.domain.pricing.PricingService
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.OrderRepository
import com.mkumar.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import kotlin.String

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val customerRepo: CustomerRepository,
    private val orderRepo: OrderRepository,
    private val orderItemRepo: ProductRepository,
    private val pricing: PricingService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerId: String =
        checkNotNull(savedStateHandle["customerId"]) { "customerId nav-arg is required" }

    private val _ui = MutableStateFlow(CustomerDetailsUiState())
    val ui: StateFlow<CustomerDetailsUiState> = _ui.asStateFlow()

    private val _effects = Channel<CustomerDetailsEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<CustomerDetailsEffect> = _effects.consumeAsFlow()

    init {
        observeViaCustomerWithOrders()
    }

    private fun observeViaCustomerWithOrders() {
        viewModelScope.launch {
            _ui.updateLoading(true)

            customerRepo.observeWithOrders(customerId)
                .filterNotNull()
                .map { rel ->
                    // If you have an OrderItemDao, plug it here:
                    // itemsOf = { id -> orderItemDao.getItemsForOrderSync(id).map { it.toUi() } }
                    rel.toUi(pricing = pricing)
                }
                .onEach { mapped ->
                    _ui.value = _ui.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        customer = mapped.customer,
                        orders = mapped.orders,
                        errorMessage = null
                    )
                }
                .launchIn(this)
        }
    }

    @Suppress("unused")
    private fun observeViaSplitStreams() {
        viewModelScope.launch {
            _ui.updateLoading(true)

            val customerFlow = customerRepo.observeWithOrders(customerId)
                .map { it?.customer }
                .filterNotNull()
                .map { c -> UiCustomer(c.id, c.name, c.phone) }

            val ordersFlow = orderRepo.observeOrdersForCustomer(customerId)
                .map { orders ->
                    orders.map { order ->
                        UiOrder(
                            id = order.id,
                            occurredAt = Instant.ofEpochMilli(order.occurredAt),
                            items = emptyList(),
                            subtotalBeforeAdjust = 0,
                            adjustedAmount = 0,
                            totalAmount = 0,
                            advanceTotal = 0,
                            remainingBalance = 0
                        )
                    }.sortedByDescending { it.occurredAt }
                }

            combine(customerFlow, ordersFlow) { customer, orders ->
                _ui.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    customer = customer,
                    orders = orders,
                    errorMessage = null
                )
            }.onEach { _ui.value = it }
                .launchIn(this)
        }
    }

    fun onIntent(intent: CustomerDetailsIntent) {
        when (intent) {
            is CustomerDetailsIntent.Refresh -> refresh()
            is CustomerDetailsIntent.NewSale -> openNewSale()
            is CustomerDetailsIntent.CloseSheet -> closeSheet()

            is CustomerDetailsIntent.OpenOrder -> openExistingOrder(intent.orderId)
            is CustomerDetailsIntent.DeleteOrder -> deleteOrder(intent.orderId)
            is CustomerDetailsIntent.ShareOrder -> shareOrder(intent.orderId)
            is CustomerDetailsIntent.ViewInvoice -> viewInvoice(intent.orderId)
            is CustomerDetailsIntent.AddItem -> addItem(intent.product)
            is CustomerDetailsIntent.UpdateItem -> updateItem(intent.item)
            is CustomerDetailsIntent.RemoveItem -> removeItem(intent.itemId)
            is CustomerDetailsIntent.UpdateOccurredAt -> updateOccurredAt(intent.occurredAt)

            is CustomerDetailsIntent.SaveDraftAsOrder -> saveDraft()
            is CustomerDetailsIntent.DiscardDraft -> discardDraft()
        }
    }

    fun onNewOrderIntent(intent: NewOrderIntent) {
        when (intent) {
            is NewOrderIntent.FormUpdate -> updateFormData(intent.productId, intent.newData)
            is NewOrderIntent.FormDelete -> removeItem(intent.productId)
            is NewOrderIntent.Save -> TODO()
            is NewOrderIntent.SelectType -> TODO()
        }
    }

    fun updateFormData(productId: String, formData: ProductFormData) {
        // Update the form data for the specified product in the draft
        mutateDraft { draft ->
            val updatedItems = draft.items.map { item ->
                if (item.id == productId) {
                    item.copy(formData = formData)
                } else {
                    item
                }
            }
            draft.copy(items = updatedItems)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _ui.updateRefreshing(true)
            _ui.updateRefreshing(false)
        }
    }

    private fun openNewSale() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                isOrderSheetOpen = true,
                draft = OrderDraft(occurredAt = Instant.now(), editingOrderId = null)
            )
            _effects.trySend(CustomerDetailsEffect.OpenOrderSheet())
        }
    }

    private fun openExistingOrder(orderId: String) {
        viewModelScope.launch {
            val order: OrderEntity? = runCatching { orderRepo.getOrder(orderId) }.getOrNull()
            if (order == null) {
                emitMessage("Order not found.")
                return@launch
            }

            val entities = runCatching { orderItemRepo.getItemsForOrder(orderId) }.getOrDefault(emptyList())
            val uiItems = entities
                .map { it.toUiItem() }

            val occurredAt = Instant.ofEpochMilli(order.occurredAt)
            // Reuse your pricing pipeline to recompute totals for a read-back draft:
            val draft = recomputeTotals(uiItems, occurredAt).copy(hasUnsavedChanges = false, editingOrderId = order.id)

            _ui.value = _ui.value.copy(
                isOrderSheetOpen = true,
                draft = draft
            )
            _effects.trySend(CustomerDetailsEffect.OpenOrderSheet(order.id))
        }
    }

    private fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            try {
                orderRepo.delete(orderId)
                emitMessage("Order deleted.")
            } catch (t: Throwable) {
                emitMessage("Failed to delete order: ${t.message}")
            }
        }
    }

    private fun shareOrder(orderId: String) {
        viewModelScope.launch {
            // Here you can enqueue a share effect or open a chooser.
            _effects.trySend(CustomerDetailsEffect.ShowMessage("Share feature coming soon."))
            // later you can create CustomerDetailsEffect.ShareOrder(orderId)
        }
    }

    private fun viewInvoice(orderId: String) {
        viewModelScope.launch {
            // If you generate invoices as PDFs, trigger opening the file here
            _effects.trySend(CustomerDetailsEffect.ShowMessage("Opening invoice for $orderId..."))
            // Later you can emit CustomerDetailsEffect.ViewInvoice(orderId, fileUri)
        }
    }



    private fun closeSheet() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isOrderSheetOpen = false)
            _effects.trySend(CustomerDetailsEffect.CloseOrderSheet)
        }
    }

    // --- Draft mutations ---

    private fun addItem(product: ProductType) = mutateDraft { draft ->
        val item = UiOrderItem(
            id = "",
            quantity = 1,
            unitPrice = 0,
            discountPercentage = 0,
            productType = product,
            name = product.toString()
        )
        val updated = draft.items + item.ensureId()
        recomputeTotals(updated, draft.occurredAt)
    }

    private fun updateItem(item: UiOrderItem) = mutateDraft { draft ->
        val updated = draft.items.map { if (it.id == item.id) item else it }
        recomputeTotals(updated, draft.occurredAt)
    }

    private fun removeItem(itemId: String) = mutateDraft { draft ->
        val updated = draft.items.filterNot { it.id == itemId }
        recomputeTotals(updated, draft.occurredAt)
    }

    private fun updateOccurredAt(instant: Instant) = mutateDraft { draft ->
        recomputeTotals(draft.items, instant)
    }

    private fun mutateDraft(block: (OrderDraft) -> OrderDraft) {
        val s = _ui.value
        val prev = s.draft
        val updated = block(prev).copy(
            hasUnsavedChanges = true,
            editingOrderId = prev.editingOrderId
        )
        _ui.value = s.copy(draft = updated)
    }


    /** Draft pricing uses orderId = "DRAFT", adjusted = 0, advance = 0. */
    private fun recomputeTotals(items: List<UiOrderItem>, occurredAt: Instant): OrderDraft {
        val input = PricingInput(
            orderId = "DRAFT",
            items = items.map { it.toItemInput() },
            adjustedAmount = 0,
            advanceTotal = 0
        )
        val result = pricing.price(input)
        val prev = _ui.value.draft

        return OrderDraft(
            occurredAt = occurredAt,
            items = items,
            subtotalBeforeAdjust = result.subtotalBeforeAdjust,
            adjustedAmount = result.adjustedAmount,
            totalAmount = result.totalAmount,
            advanceTotal = result.advanceTotal,
            remainingBalance = result.remainingBalance,
            hasUnsavedChanges = true,
            editingOrderId = prev.editingOrderId
        )
    }

    // --- Persist order ---

    // In your ViewModel
    private fun UiOrderItem.toEntity(orderId: String): OrderItemEntity =
        OrderItemEntity(
            id = id.ifBlank { UUID.randomUUID().toString() },
            orderId = orderId,
            quantity = quantity,
            unitPrice = unitPrice,
            discountPercentage = discountPercentage.coerceIn(0, 100),
            productTypeLabel = productType.toString(),
            productOwnerName = name,
        )

    private fun saveDraft() {
        viewModelScope.launch {
            val s = _ui.value
            val draft = s.draft
            val customer = s.customer
            if (customer == null) {
                emitMessage("Customer not found.")
                return@launch
            }
            if (draft.items.isEmpty()) {
                emitMessage("Please add at least one item.")
                return@launch
            }

            try {
                val orderId = draft.editingOrderId ?: UUID.randomUUID().toString()
                val orderEntity = OrderEntity(
                    id = orderId,
                    customerId = customer.id,
                    occurredAt = draft.occurredAt.toEpochMilli(),
                )
                val itemEntities = draft.items.map { it.toEntity(orderId) }

                orderRepo.upsert(orderEntity)
                for (item in itemEntities) {
                    orderItemRepo.upsert(item)
                }

                _ui.value = s.copy(
                    isOrderSheetOpen = false,
                    draft = OrderDraft(),
                    errorMessage = null
                )
                _effects.trySend(CustomerDetailsEffect.CloseOrderSheet)
                emitMessage("Order saved.")
            } catch (t: Throwable) {
                _ui.value = s.copy(errorMessage = t.message ?: "Failed to save order")
                emitMessage("Failed to save order.")
            }
        }
    }

    private fun OrderItemEntity.toUiItem() = UiOrderItem(
        id = id,
        quantity = quantity,
        unitPrice = unitPrice,
        discountPercentage = discountPercentage,
        productType = ProductType.valueOf(productTypeLabel),
        name = productOwnerName
    )


    private fun discardDraft() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(draft = OrderDraft())
            emitMessage("Discarded changes.")
        }
    }

    // --- Helpers ---

    private fun UiOrderItem.ensureId(): UiOrderItem =
        if (id.isBlank()) copy(id = java.util.UUID.randomUUID().toString()) else this

    private fun UiOrderItem.toItemInput(): PricingInput.ItemInput =
        PricingInput.ItemInput(
            itemId = id,
            quantity = quantity,
            unitPrice = unitPrice,
            discountPercentage = discountPercentage
        )

    private fun emitMessage(msg: String) {
        _effects.trySend(CustomerDetailsEffect.ShowMessage(msg))
    }
}

