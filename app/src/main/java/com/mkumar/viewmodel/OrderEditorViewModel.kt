package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.ProductFormData
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.domain.pricing.PricingInput
import com.mkumar.domain.pricing.PricingService
import com.mkumar.model.NewOrderIntent
import com.mkumar.model.OrderEditorEffect
import com.mkumar.model.OrderEditorIntent
import com.mkumar.model.OrderEditorUi
import com.mkumar.model.OrderStatus
import com.mkumar.model.ProductType
import com.mkumar.model.UiOrderItem
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.OrderRepository
import com.mkumar.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OrderEditorViewModel @Inject constructor(
    private val orderRepo: OrderRepository,
    private val productRepo: ProductRepository,
    private val customerRepo: CustomerRepository,
    private val pricing: PricingService
) : ViewModel() {

    private val _ui = MutableStateFlow(OrderEditorUi())
    val ui: StateFlow<OrderEditorUi> = _ui

    private val _effects = MutableSharedFlow<OrderEditorEffect>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val effects = _effects.asSharedFlow()

    // -------------------------------------------------
    // PUBLIC ENTRY POINT
    // -------------------------------------------------

    fun load(customerId: String, orderId: String?) {
        if (orderId.isNullOrBlank()) {
            createDraft(customerId)
        } else {
            loadExistingOrder(orderId)
        }
    }

    // -------------------------------------------------
    // INTENTS
    // -------------------------------------------------

    fun onIntent(intent: OrderEditorIntent) {
        when (intent) {
            is OrderEditorIntent.AddItem -> addItem(intent.type)
            is OrderEditorIntent.UpdateItem -> updateItem(intent.itemId, intent.newData)
            is OrderEditorIntent.DeleteItem -> deleteItem(intent.itemId)
            is OrderEditorIntent.UpdateAdjustedAmount -> updateAdjusted(intent.value)
            is OrderEditorIntent.UpdateAdvanceTotal -> updateAdvance(intent.value)
            is OrderEditorIntent.SaveOrder -> activateOrder()
            is OrderEditorIntent.UpdateOccurredAt -> updateOccurredAt(intent.occurredAt)
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

    // -------------------------------------------------
    // INITIAL LOAD
    // -------------------------------------------------

    private fun createDraft(customerId: String) {
        val draftId = UUID.randomUUID().toString()
        // Insert empty DRAFT row in DB
        viewModelScope.launch {
            val entity = OrderEntity(
                id = draftId,
                customerId = customerId,
                orderStatus = OrderStatus.DRAFT.value
            )
            val order = orderRepo.createOrderWithItems(entity)
            val customerWithOrders = customerRepo.getWithOrders(customerId)
            val uiCustomer = customerWithOrders?.toUi(pricing = pricing)?.customer
            _ui.update {
                it.copy(
                    isLoading = false,
                    draft = OrderEditorUi.Draft(
                        orderId = draftId,
                        customerId = customerId,
                        items = emptyList(),
                        adjustedAmount = 0,
                        advanceTotal = 0,
                        editingOrderId = draftId,
                        invoiceNumber = order.invoiceSeq ?: 0L
                    ),
                    customer = uiCustomer,
                    orders = customerWithOrders?.orders?.map { o -> o.toUiOrder() }.orEmpty(),
                )
            }
        }
    }

    private fun loadExistingOrder(orderId: String) {
        viewModelScope.launch {
            val order = orderRepo.getOrder(orderId) ?: return@launch
            val customerWithOrders = customerRepo.getWithOrders(order.customerId)
            val uiCustomer = customerWithOrders?.toUi(pricing = pricing)?.customer
            val items = productRepo.getItemsForOrder(orderId)
            val uiItems = items
                .map { it.toUiItem() }

            val draft = OrderEditorUi.Draft(
                orderId = order.id,
                editingOrderId = order.id,
                customerId = order.customerId,
                items = uiItems,
                adjustedAmount = order.adjustedAmount,
                advanceTotal = order.advanceTotal,
                invoiceNumber = order.invoiceSeq?: -1,
                totalAmount = order.totalAmount,
                remainingBalance = order.remainingBalance,
                occurredAt = Instant.ofEpochMilli(order.occurredAt )
            )

            _ui.update { it.copy(
                isLoading = false,
                customer = uiCustomer,
                orders = customerWithOrders?.orders?.map { o -> o.toUiOrder() }.orEmpty(),
                draft = draft
            ) }
        }
    }

    // -------------------------------------------------
    // ADD / EDIT / DELETE ITEMS
    // -------------------------------------------------

    private fun addItem(type: ProductType) {
        val current = _ui.value.draft
        val owner = current.items.firstOrNull { it.productType == type }?.name ?: "Default"

        val newItem = UiOrderItem(
            id = UUID.randomUUID().toString(),
            productType = type,
            name = owner, //review: should we default to last used owner for this type?
            productDescription = "",
            quantity = 1,
            unitPrice = 0,
            discountPercentage = 0
        )

        val updated = current.copy(items = current.items + newItem, justAddedItemId = newItem.id)
        recalc(updated)
    }

    private fun updateItem(itemId: String, new: UiOrderItem) {
        val current = _ui.value.draft
        val updatedItems = current.items.map { if (it.id == itemId) new else it }
        recalc(current.copy(items = updatedItems))
    }

    private fun deleteItem(itemId: String) {
        val current = _ui.value.draft
        val updated = current.copy(items = current.items.filterNot { it.id == itemId })
        recalc(updated)
    }

    fun onSaveItem(productId: String, newForm: ProductFormData) {
        viewModelScope.launch {
            val s = _ui.value
            val draft = s.draft
            val orderId = draft.editingOrderId
//            emitMessage("Saving $orderId")
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
                productRepo.upsert(entity)

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
//                emitMessage("Item saved.")
            } catch (t: Throwable) {
//                emitMessage("Failed to save item: ${t.message ?: "unknown error"}")
            }
        }
    }

    fun onDeleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                // 1) Persist delete
                productRepo.deleteProductById(itemId)

                // 2) Reflect in UI draft
                mutateDraft { d ->
                    val updated = d.items.filterNot { it.id == itemId }
                    recomputeTotals(updated, d.occurredAt, adjustedAmount = 0, advanceTotal = d.advanceTotal)
                }
//                emitMessage("Item deleted.")
            } catch (t: Throwable) {
//                emitMessage("Failed to delete item: ${t.message ?: "unknown error"}")
            }
        }
    }

    private fun recomputeTotals(items: List<UiOrderItem>, occurredAt: Instant): OrderEditorUi.Draft {
        val d = _ui.value.draft
        return recomputeTotals(items, occurredAt, d.adjustedAmount, d.advanceTotal)
    }

    private fun recomputeTotals(
        items: List<UiOrderItem>,
        occurredAt: Instant,
        adjustedAmount: Int ,
        advanceTotal: Int
    ): OrderEditorUi.Draft {
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
            remainingBalance = result.remainingBalance
        )
    }

    // -------------------------------------------------
    // PRICING
    // -------------------------------------------------

    private fun updateAdjusted(value: Int) {
        val current = _ui.value.draft
        recalc(current.copy(adjustedAmount = value))
    }

    private fun updateAdvance(value: Int) {
        val current = _ui.value.draft
        recalc(current.copy(advanceTotal = value))
    }

    private fun recalc(draft: OrderEditorUi.Draft) {
        val pricingInput = PricingInput(
            orderId = draft.orderId,
            adjustedAmount = draft.adjustedAmount,
            advanceTotal = draft.advanceTotal,
            items = draft.items.map {
                PricingInput.ItemInput(
                    itemId = it.id,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    discountPercentage = it.discountPercentage
                )
            }
        )

        val result = pricing.price(pricingInput)

        val updatedItems = draft.items.map { uiItem ->
            val priced = result.items.find { it.itemId == uiItem.id }
            uiItem.copy(
                finalTotal = priced?.lineTotal ?: 0
            )
        }

        val newDraft = draft.copy(
            items = updatedItems,
            adjustedAmount = result.adjustedAmount,
            advanceTotal = result.advanceTotal,
            totalAmount = result.totalAmount,
            remainingBalance = result.remainingBalance
        )

        _ui.update { it.copy(draft = newDraft) }
    }

//    // -------------------------------------------------
//    // SAVE / ACTIVATE ORDER
//    // -------------------------------------------------

    private fun activateOrder() {
        val draft = _ui.value.draft
        if (draft.items.isEmpty()) return

        viewModelScope.launch {
            val owners = draft.items.map { it.name }.distinct()
            val categories = draft.items.map { it.productType.toString() }.distinct()

            val entity = OrderEntity(
                id = draft.orderId,
                customerId = draft.customerId,
                invoiceSeq = draft.invoiceNumber,
                adjustedAmount = draft.adjustedAmount,
                totalAmount = draft.totalAmount,
                advanceTotal = draft.advanceTotal,
                remainingBalance = draft.remainingBalance,
                productCategories = categories,
                owners = owners,
                orderStatus = if (draft.remainingBalance > 0) OrderStatus.ACTIVE.value else OrderStatus.COMPLETED.value,
                updatedAt = System.currentTimeMillis()
            )
            val newEntities = draft.items.map {
                it.toEntity(draft.orderId)
            }
            orderRepo.upsert(entity)
            for (item in newEntities) productRepo.upsert(item)

            _effects.emit(OrderEditorEffect.CloseEditor)
        }
    }

    private fun mutateDraft(block: (OrderEditorUi.Draft) -> OrderEditorUi.Draft) {
        val s = _ui.value
        val prev = s.draft
        val updated = block(prev).copy(
            editingOrderId = prev.editingOrderId
        )
        _ui.value = s.copy(draft = updated)
    }

    private fun updateOccurredAt(instant: Instant) = mutateDraft { draft ->
        recomputeTotals(draft.items, instant)
    }

    private fun emitMessage(msg: String) {
        _effects.tryEmit(OrderEditorEffect.ShowMessage(msg))
    }
}


