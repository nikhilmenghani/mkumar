package com.mkumar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.common.constant.CustomerDetailsConstants
import com.mkumar.common.files.saveInvoicePdf
import com.mkumar.data.ProductFormData
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.domain.invoice.InvoiceData
import com.mkumar.domain.invoice.InvoiceItemRow
import com.mkumar.domain.invoice.InvoicePdfBuilderImpl
import com.mkumar.domain.pricing.PricingInput
import com.mkumar.domain.pricing.PricingService
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.OrderRepository
import com.mkumar.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val customerRepo: CustomerRepository,
    private val orderRepo: OrderRepository,
    private val orderItemRepo: ProductRepository,
    private val pricing: PricingService,
    @dagger.hilt.android.qualifiers.ApplicationContext private val app: android.content.Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val customerId: String =
        checkNotNull(savedStateHandle["customerId"]) { "customerId nav-arg is required" }

    private val _ui = MutableStateFlow(CustomerDetailsUiState())
    val ui: StateFlow<CustomerDetailsUiState> = _ui.asStateFlow()

    private val _effects = MutableSharedFlow<CustomerDetailsEffect>(extraBufferCapacity = 64)
    val effects: Flow<CustomerDetailsEffect> = _effects.asSharedFlow()

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
                .map { c -> UiCustomer(c.id, c.name, c.phone, c.createdAt) }

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

            is CustomerDetailsIntent.UpdateAdjustedAmount -> updateAdjustedAmount(intent.value)
            is CustomerDetailsIntent.UpdateAdvanceTotal -> updateAdvanceTotal(intent.value)

            is CustomerDetailsIntent.OpenOrder -> openExistingOrder(intent.orderId)
            is CustomerDetailsIntent.UpdateOrder -> updateExistingOrder(intent.orderId)
            is CustomerDetailsIntent.DeleteOrder -> deleteOrder(intent.orderId)
            is CustomerDetailsIntent.ShareOrder -> shareOrder(intent.orderId)
            is CustomerDetailsIntent.ViewInvoice -> viewInvoice(intent.orderId)
            is CustomerDetailsIntent.AddItem -> addItem(intent.product)
            is CustomerDetailsIntent.UpdateOccurredAt -> updateOccurredAt(intent.occurredAt)

            is CustomerDetailsIntent.SaveDraftAsOrder -> saveDraft()
            is CustomerDetailsIntent.DiscardDraft -> discardDraft()
        }
    }

    fun onNewOrderIntent(intent: NewOrderIntent) {
        when (intent) {
            is NewOrderIntent.FormUpdate -> onSaveItem(intent.productId, intent.newData)
            is NewOrderIntent.FormDelete -> onDeleteItem(intent.productId)
            is NewOrderIntent.Save -> TODO()
            is NewOrderIntent.SelectType -> TODO()
            is NewOrderIntent.ConsumeJustAdded -> {
                mutateDraft { d -> d.copy(justAddedItemId = null) }
            }
        }
    }

    private fun updateAdjustedAmount(value: Int) {
        val s = _ui.value
        val d = s.draft
        val newDraft = recomputeTotals(d.items, d.occurredAt, adjustedAmount = value, advanceTotal = d.advanceTotal)
        _ui.value = s.copy(draft = newDraft)
    }

    private fun updateAdvanceTotal(value: Int) {
        val s = _ui.value
        val d = s.draft
        val newDraft = recomputeTotals(d.items, d.occurredAt, adjustedAmount = d.adjustedAmount, advanceTotal = value)
        _ui.value = s.copy(draft = newDraft)
    }

    fun onSaveItem(productId: String, newForm: ProductFormData) {
        viewModelScope.launch {
            val s = _ui.value
            val draft = s.draft
            val orderId = draft.editingOrderId
            val current = draft.items.firstOrNull { it.id == productId } ?: return@launch

            // If order doesn't exist yet, keep it local only.
            if (orderId == null) {
                mutateDraft { d ->
                    val newItems = d.items.map { if (it.id == productId) it.copy(formData = newForm) else it }
                    recomputeTotals(
                        items = newItems,
                        occurredAt = d.occurredAt,
                        adjustedAmount = d.adjustedAmount,
                        advanceTotal = d.advanceTotal
                    )
                }
                emitMessage("Item updated locally. Save the order to persist.")
                return@launch
            }

            val updated = current.copy(
                formData = newForm,
                unitPrice = newForm.unitPrice,
                quantity = newForm.quantity,
                discountPercentage = newForm.discountPct,
                finalTotal = newForm.total
            )
            val entity = updated.toEntity(orderId)
            try {
                // 1) Persist
                orderItemRepo.upsert(entity)

                // 2) Reflect in UI draft
                mutateDraft { d ->
                    val newItems = d.items.map {
                        if (it.id == productId) it.copy(
                            formData = newForm,
                            unitPrice = newForm.unitPrice,
                            quantity = newForm.quantity,
                            discountPercentage = newForm.discountPct,
                            finalTotal = newForm.total
                        ) else it
                    }

                    // Write zero into state first (source of truth)
                    val cleared = d.copy(
                        items = newItems,
                        adjustedAmount = 0,
                        advanceTotal = 0
                    )

                    // Then recompute totals using zero
                    recomputeTotals(
                        items = cleared.items,
                        occurredAt = cleared.occurredAt,
                        adjustedAmount = cleared.adjustedAmount,
                        advanceTotal = cleared.advanceTotal
                    )
                }
                emitMessage("Item saved.")
            } catch (t: Throwable) {
                emitMessage("Failed to save item: ${t.message ?: "unknown error"}")
            }
        }
    }

    fun onDeleteItem(itemId: String) {
        viewModelScope.launch {
            val s = _ui.value
            val orderId = s.draft.editingOrderId

            // If order not yet created, just remove from draft.
            if (orderId == null) {
                mutateDraft { d ->
                    val updated = d.items.filterNot { it.id == itemId }
                    recomputeTotals(updated, d.occurredAt)
                }
                emitMessage("Item removed from draft.")
                return@launch
            }

            try {
                // 1) Persist delete
                orderItemRepo.deleteProductById(itemId)

                // 2) Reflect in UI draft
                mutateDraft { d ->
                    val updated = d.items.filterNot { it.id == itemId }
                    recomputeTotals(updated, d.occurredAt, adjustedAmount = 0, advanceTotal = d.advanceTotal)
                }
                emitMessage("Item deleted.")
            } catch (t: Throwable) {
                emitMessage("Failed to delete item: ${t.message ?: "unknown error"}")
            }
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
            val s = _ui.value
            val customer = s.customer
            if (customer == null) {
                emitMessage("Customer not found.")
                return@launch
            }

            // 1) If a prior draft existed with no items, delete that empty order row.
            s.draft.editingOrderId?.let { oldId ->
                try {
                    val hasItems = orderItemRepo.countItemsForOrder(oldId) > 0
                    if (!hasItems) orderRepo.delete(oldId)
                } catch (_: Throwable) {
                    // Non-fatal: continue creating the new sale
                }
            }

            // 2) Create a brand-new order row (DRAFT state if you have status)
            val now = Instant.now()
            val newOrderId = UUID.randomUUID().toString()
            val orderEntity = OrderEntity(
                id = newOrderId,
                customerId = customer.id,
                occurredAt = now.toEpochMilli(),
                // status = OrderStatus.DRAFT, // if you have this column
            )

            try {
                orderRepo.upsert(orderEntity)

                // 3) Open sheet with a fresh draft bound to this orderId
                _ui.value = s.copy(
                    isOrderSheetOpen = true,
                    draft = OrderDraft(
                        occurredAt = now,
                        editingOrderId = newOrderId,
                        // other defaults as needed
                    ),
                    errorMessage = null
                )
                _effects.tryEmit(CustomerDetailsEffect.OpenOrderSheet())
            } catch (t: Throwable) {
                emitMessage("Failed to start new sale: ${t.message ?: "unknown error"}")
            }
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
            val draft = recomputeTotals(uiItems, occurredAt, order.adjustedAmount, order.advanceTotal).copy(hasUnsavedChanges = false, editingOrderId = order.id)

            _ui.value = _ui.value.copy(
                isOrderSheetOpen = true,
                draft = draft
            )
            _effects.tryEmit(CustomerDetailsEffect.OpenOrderSheet(order.id))
        }
    }

    private fun updateExistingOrder(orderId: String) {
        viewModelScope.launch {
            val s = _ui.value
            val draft = s.draft
            if (draft.editingOrderId != orderId) {
                emitMessage("Cannot update: editing a different order.")
                return@launch
            }
            if (!draft.hasUnsavedChanges) {
                emitMessage("No changes to save.")
                return@launch
            }

            // Reuse saveDraft logic
            saveDraft()
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

    private suspend fun generateInvoicePdf(orderId: String): android.net.Uri? {
        val fileName = CustomerDetailsConstants.getInvoiceFileName(orderId, withTimeStamp = true) + ".pdf"

        val s = _ui.value
        val uiCustomer = s.customer
        val isEditingThisOrder = s.draft.editingOrderId == orderId
        val uiDraftItems = if (isEditingThisOrder) s.draft.items else emptyList()

        val order = orderRepo.getOrder(orderId) ?: return null

        val itemEntities: List<OrderItemEntity> =
            if (uiDraftItems.isNotEmpty()) {
                uiDraftItems.map { it.toEntity(orderId) }
            } else {
                orderItemRepo.getItemsForOrder(orderId)
            }

        val input = PricingInput(
            orderId = order.id,
            items = itemEntities.map {
                PricingInput.ItemInput(
                    itemId = it.id,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    discountPercentage = it.discountPercentage
                )
            },
            adjustedAmount = order.adjustedAmount.coerceAtLeast(0),
            advanceTotal = order.advanceTotal.coerceAtLeast(0)
        )

        val priced = pricing.price(input)
        val pricedById = priced.items.associateBy { it.itemId }

        val invoiceItems: List<InvoiceItemRow> = itemEntities.map { e ->
            val p = pricedById[e.id]
            val lineTotal = p?.lineTotal ?: e.finalTotal
            val description = UiOrderItem.deserializeFormData(e.formDataJson)?.productDescription ?: ""
            InvoiceItemRow(
                name = when {
                    e.productOwnerName.isNotBlank() -> e.productOwnerName
                    e.productTypeLabel.isNotBlank() -> e.productTypeLabel
                    else -> "Item"
                },
                qty = e.quantity,
                unitPrice = e.unitPrice.toDouble(),
                total = lineTotal.toDouble(),
                discount = e.discountPercentage,
                description = "[" + e.productOwnerName + "] -> " + description
            )
        }

        val invoiceData = InvoiceData(
            shopName = "M Kumar Luxurious Watch & Optical Store",
            shopAddress = "7, Shlok Height, Opp. Dev Paradise & Dharti Silver, Nr. Mansarovar Road, Chandkheda, Ahmedabad.",
            customerName = uiCustomer?.name ?: "Customer",
            ownerName = "Mahendra Menghani",
            customerPhone = uiCustomer?.phone ?: "-",
            ownerPhone = "9427956490",
            ownerEmail = "menghani.mahendra@gmail.com",
            orderId = order.id,
            occurredAtText = dateFmt.format(Date(order.occurredAt)),
            items = invoiceItems,
            subtotal = priced.subtotalBeforeAdjust.toDouble(),
            adjustedTotal = priced.adjustedAmount.toDouble(),
            advanceTotal = priced.advanceTotal.toDouble(),
            remainingBalance = priced.remainingBalance.toDouble()
        )

        val bytes = InvoicePdfBuilderImpl().build(invoiceData)
        return saveInvoicePdf(app, fileName, bytes)
    }

    fun shareOrder(orderId: String) {
        viewModelScope.launch {
            try {
                _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Creating invoice…"))
                val uri = generateInvoicePdf(orderId)
                if (uri != null) {
                    _effects.tryEmit(CustomerDetailsEffect.ShareInvoice(orderId, uri))
                    _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Invoice ready."))
                } else {
                    _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Order not found."))
                }
            } catch (t: Throwable) {
                _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Failed to prepare share: ${t.message}"))
            }
        }
    }

    fun viewInvoice(orderId: String) {
        viewModelScope.launch {
            try {
                _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Creating invoice…"))
                val uri = generateInvoicePdf(orderId)
                if (uri != null) {
                    _effects.tryEmit(CustomerDetailsEffect.ViewInvoice(orderId, uri))
                    _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Invoice ready."))
                } else {
                    _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Order not found."))
                }
            } catch (t: Throwable) {
                _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Failed to create/open invoice: ${t.message}"))
            }
        }
    }

    fun closeSheet() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isOrderSheetOpen = false)
            _effects.tryEmit(CustomerDetailsEffect.CloseOrderSheet)
        }
    }

    // --- Draft mutations ---

    private fun addItem(product: ProductType) = mutateDraft { draft ->
        val newId = UUID.randomUUID().toString()
        val item = UiOrderItem(
            id = newId,
            quantity = 1,
            unitPrice = 0,
            discountPercentage = 0,
            productType = product,
            name = product.toString()
        )
        val updated = draft.items + item
        recomputeTotals(updated, draft.occurredAt).copy(justAddedItemId = newId)
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

    private fun discardDraft() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(draft = OrderDraft())
            emitMessage("Discarded changes.")
        }
    }

    private fun saveDraft() {
        viewModelScope.launch {
            val s = _ui.value
            val draft = s.draft
            val customer = s.customer
            if (customer == null) {
                emitMessage("Customer not found."); return@launch
            }
            if (draft.items.isEmpty()) {
                emitMessage("Please add at least one item."); return@launch
            }

            // We expect this to exist because we created it at New Sale.
            val orderId = draft.editingOrderId ?: UUID.randomUUID().toString()
            val orderEntity = OrderEntity(
                id = orderId,
                customerId = customer.id,
                occurredAt = draft.occurredAt.toEpochMilli(),
                adjustedAmount = draft.adjustedAmount,
                advanceTotal = draft.advanceTotal,
                totalAmount = draft.totalAmount,
                remainingBalance = draft.remainingBalance
            )

            val itemEntities = draft.items.map { it.toEntity(orderId) }

            try {
                // If you have a transaction helper, use it:
                // orderRepo.withTransaction { ... }
                orderRepo.upsert(orderEntity)
                for (item in itemEntities) orderItemRepo.upsert(item)

                // Optional: mark status = CONFIRMED here if you track status

                _ui.value = s.copy(
                    isOrderSheetOpen = false,
                    draft = OrderDraft(),
                    errorMessage = null
                )
                _effects.tryEmit(CustomerDetailsEffect.CloseOrderSheet)
                emitMessage("Order saved.")
                observeViaCustomerWithOrders()
            } catch (t: Throwable) {
                _ui.value = s.copy(errorMessage = t.message ?: "Failed to save order")
                emitMessage("Failed to save order.")
            }
        }
    }

    private fun recomputeTotals(items: List<UiOrderItem>, occurredAt: Instant): OrderDraft {
        val d = _ui.value.draft
        return recomputeTotals(items, occurredAt, d.adjustedAmount, d.advanceTotal)
    }

    /** Draft pricing uses orderId = "DRAFT", adjusted = 0, advance = 0. */
    private fun recomputeTotals(
        items: List<UiOrderItem>,
        occurredAt: Instant,
        adjustedAmount: Int,
        advanceTotal: Int
    ): OrderDraft {
        val prev = _ui.value.draft
        val input = PricingInput(
            orderId = prev.editingOrderId ?: "DRAFT",
            items = items.map { it.toItemInput() },  // include qty, unitPrice, discounts, taxes…
            adjustedAmount = adjustedAmount.coerceAtLeast(0),
            advanceTotal = advanceTotal.coerceAtLeast(0),
        )
        val result = pricing.price(input)

        return prev.copy(
            occurredAt = occurredAt,
            items = items,
            subtotalBeforeAdjust = result.subtotalBeforeAdjust,
            adjustedAmount = result.adjustedAmount,
            totalAmount = result.totalAmount,
            advanceTotal = result.advanceTotal,
            remainingBalance = result.remainingBalance,
            hasUnsavedChanges = true
        )
    }

    // --- Persist order ---

    // In your ViewModel
    private fun UiOrderItem.toEntity(orderId: String): OrderItemEntity {
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
            finalTotal = finalTotal
        )
    }

    private fun OrderItemEntity.toUiItem(): UiOrderItem {
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
            productDescription = deserializedFormData?.productDescription ?: ""
        )
    }


    // --- Helpers ---

    private fun UiOrderItem.ensureId(): UiOrderItem =
        if (id.isBlank()) copy(id = UUID.randomUUID().toString()) else this

    private fun UiOrderItem.toItemInput(): PricingInput.ItemInput =
        PricingInput.ItemInput(
            itemId = id,
            quantity = quantity,
            unitPrice = unitPrice,
            discountPercentage = discountPercentage
        )

    private fun emitMessage(msg: String) {
        _effects.tryEmit(CustomerDetailsEffect.ShowMessage(msg))
    }
}

