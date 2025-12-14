package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.common.extension.nowUtcMillis
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
import com.mkumar.model.productTypeDisplayNames
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.OrderRepository
import com.mkumar.repository.PaymentRepository
import com.mkumar.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OrderEditorViewModel @Inject constructor(
    private val orderRepo: OrderRepository,
    private val productRepo: ProductRepository,
    private val customerRepo: CustomerRepository,
    private val pricing: PricingService,
    private val paymentsRepo: PaymentRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(OrderEditorUi())
    val ui: StateFlow<OrderEditorUi> = _ui

    private val _effects = MutableSharedFlow<OrderEditorEffect>()
    val effects = _effects.asSharedFlow()

    // --------------------------------------------------------------
    // PUBLIC ENTRY (LOAD DRAFT / ORDER)
    // --------------------------------------------------------------

    fun load(orderId: String) {
        loadExistingOrder(orderId)
    }

    // --------------------------------------------------------------
    // INTENTS
    // --------------------------------------------------------------

    fun onIntent(intent: OrderEditorIntent) {
        when (intent) {

            is OrderEditorIntent.AddItem -> addItem(intent.type)

            is OrderEditorIntent.UpdateItem ->
                updateItem(intent.itemId, intent.newData)

            is OrderEditorIntent.DeleteItem ->
                deleteItem(intent.itemId)

            is OrderEditorIntent.UpdateAdjustedAmount ->
                updateAdjusted(intent.value)

            is OrderEditorIntent.SaveOrder ->
                activateOrder()

            is OrderEditorIntent.UpdateOccurredAt ->
                updateOccurredAt(intent.occurredAt)

            // ---- PAYMENT ADD ----
            is OrderEditorIntent.AddPayment -> {
                viewModelScope.launch {
                    paymentsRepo.addPayment(
                        orderId = intent.orderId,
                        amount = intent.amountPaid,
                        paymentAt = intent.paymentAt
                    )
                }
            }

            // ---- PAYMENT DELETE ----
            is OrderEditorIntent.DeletePayment -> {
                viewModelScope.launch {
                    paymentsRepo.deletePaymentById(intent.paymentId)
                }
            }

            else -> {}
        }
    }

    fun onNewOrderIntent(intent: NewOrderIntent) {
        when (intent) {
            is NewOrderIntent.FormUpdate -> onSaveItem(intent.productId, intent.newData)
            is NewOrderIntent.FormDelete -> onDeleteItem(intent.productId)
            is NewOrderIntent.ConsumeJustAdded -> {
                mutateDraft { it.copy(justAddedItemId = null) }
            }
            else -> {}
        }
    }

    // --------------------------------------------------------------
    // LOAD EXISTING ORDER
    // --------------------------------------------------------------

    private fun loadExistingOrder(orderId: String) {
        viewModelScope.launch {

            val order = orderRepo.getOrder(orderId) ?: return@launch

            val customerWithOrders = customerRepo.getWithOrders(order.customerId)
            val uiCustomer = customerWithOrders?.toUi(pricing)?.customer

            val items = productRepo.getItemsForOrder(orderId).map { it.toUiItem() }

            _ui.update {
                it.copy(
                    isLoading = false,
                    customer = uiCustomer,
                    orders = customerWithOrders?.orders?.map { o -> o.toUiOrder() }.orEmpty(),
                    draft = OrderEditorUi.Draft(
                        orderId = order.id,
                        editingOrderId = order.id,
                        customerId = order.customerId,
                        createdAt = order.createdAt,
                        updatedAt = order.updatedAt,
                        items = items,
                        adjustedAmount = order.adjustedAmount,
                        paidTotal = order.paidTotal,
                        totalAmount = order.totalAmount,
                        remainingBalance = order.remainingBalance,
                        invoiceNumber = order.invoiceSeq ?: -1,
                        receivedAt = order.receivedAt,
                        payments = emptyList() // will be updated by collector
                    )
                )
            }

            // Start real-time payments stream
            collectPayments(orderId)
        }
    }

    // --------------------------------------------------------------
    // REAL-TIME PAYMENT UPDATES
    // --------------------------------------------------------------

    private suspend fun collectPayments(orderId: String) {
        paymentsRepo.getPaymentsForOrder(orderId)
            .onEach { list ->

                // Update only UI state
                val paid = list.sumOf { it.amountPaid }
                val draft = _ui.value.draft

                val effectiveTotal = if (draft.adjustedAmount > 0)
                    draft.adjustedAmount
                else
                    draft.totalAmount

                val remaining = effectiveTotal - paid

                _ui.update { s ->
                    s.copy(
                        draft = s.draft.copy(
                            payments = list.map { it.toUiPaymentItem() },
                            paidTotal = paid,
                            remainingBalance = remaining
                        )
                    )
                }

                // DO NOT update the order here.
                // PaymentRepositoryImpl already updates the Order.
            }
            .launchIn(viewModelScope)
    }

    // --------------------------------------------------------------
    // ITEM CRUD
    // --------------------------------------------------------------

    private fun addItem(type: ProductType) {
        val d = _ui.value.draft
        val newItem = UiOrderItem(
            id = UUID.randomUUID().toString(),
            productType = type,
            name = "",
            productDescription = "",
            quantity = 1,
            unitPrice = 0,
            discountPercentage = 0,
            finalTotal = 0
        )

        val updated = d.copy(
            items = d.items + newItem,
            justAddedItemId = newItem.id
        )
        recalc(updated)
    }

    private fun updateItem(itemId: String, newItem: UiOrderItem) {
        val d = _ui.value.draft
        val newList = d.items.map { if (it.id == itemId) newItem else it }
        recalc(d.copy(items = newList))
    }

    private fun deleteItem(itemId: String) {
        val d = _ui.value.draft
        recalc(d.copy(items = d.items.filterNot { it.id == itemId }))
    }

    // --------------------------------------------------------------
    // SAVE SINGLE ITEM (FORM)
    // --------------------------------------------------------------

    fun onSaveItem(productId: String, newForm: ProductFormData) {
        viewModelScope.launch {
            val s = _ui.value
            val d = s.draft
            val orderId = d.orderId

            val current = d.items.firstOrNull { it.id == productId } ?: return@launch

            val updated = current.copy(
                formData = newForm,
                unitPrice = newForm.unitPrice,
                quantity = newForm.quantity,
                discountPercentage = newForm.discountPct,
                finalTotal = newForm.total
            )
            val entity = updated.toEntity(orderId)

            try {
                productRepo.upsert(entity)
                val newList = d.items.map { if (it.id == productId) updated else it }
                recalc(d.copy(items = newList))
            } catch (_: Throwable) {
            }
        }
    }

    fun onDeleteItem(productId: String) {
        viewModelScope.launch {
            try {
                productRepo.deleteProductById(productId)

                val d = _ui.value.draft
                val newList = d.items.filterNot { it.id == productId }

                recalc(d.copy(items = newList))
            } catch (_: Throwable) {
            }
        }
    }

    // --------------------------------------------------------------
    // PRICING
    // --------------------------------------------------------------

    private fun updateAdjusted(value: Int) {
        val d = _ui.value.draft
        recalc(d.copy(adjustedAmount = value))
    }

    private fun updateOccurredAt(long: Long) {
        val d = _ui.value.draft
        recalc(d.copy(receivedAt = long))
    }

    private fun recalc(draft: OrderEditorUi.Draft) {
        val result = pricing.price(
            PricingInput(
                orderId = draft.orderId,
                adjustedAmount = draft.adjustedAmount,
                paidTotal = draft.paidTotal,
                items = draft.items.map {
                    PricingInput.ItemInput(
                        itemId = it.id,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        discountPercentage = it.discountPercentage
                    )
                }
            )
        )

        val actualTotal = result.totalAmount

        // UI total = adjusted OR actual
        val uiTotal = if (draft.adjustedAmount > 0)
            draft.adjustedAmount
        else
            actualTotal

        val paid = draft.paidTotal
        val remaining = uiTotal - paid

        val updatedDraft = draft.copy(
            items = result.items.map { p ->
                val original = draft.items.first { it.id == p.itemId }
                original.copy(finalTotal = p.lineTotal)
            },
            totalAmount = actualTotal,     // save actual
            remainingBalance = remaining   // based on UI total
        )

        _ui.update { it.copy(draft = updatedDraft) }
    }

    // --------------------------------------------------------------
    // SAVE ORDER
    // --------------------------------------------------------------

    private fun activateOrder() {
        val draft = _ui.value.draft
        if (draft.items.isEmpty()) return

        viewModelScope.launch {
            // the owners and categories don't matter here since they will be overridden at upsert
            val owners = draft.items.map { it.name }.distinct()
            val categories = draft.items.mapNotNull {
                if (it.productType.name == "GeneralProduct") {
                    (it.formData as? ProductFormData.GeneralProductData)?.productType
                        ?: productTypeDisplayNames[it.productType]
                } else productTypeDisplayNames[it.productType]
            }.distinct()

            val entity = OrderEntity(
                id = draft.orderId,
                customerId = draft.customerId,
                invoiceSeq = draft.invoiceNumber,
                adjustedAmount = draft.adjustedAmount,
                createdAt = draft.createdAt,
                totalAmount = draft.totalAmount,
                paidTotal = draft.paidTotal,
                productCategories = categories,
                owners = owners,
                remainingBalance = draft.remainingBalance,
                updatedAt = nowUtcMillis(),
                receivedAt = draft.receivedAt,
                orderStatus = if (draft.remainingBalance > 0)
                    OrderStatus.ACTIVE.value
                else
                    OrderStatus.COMPLETED.value
            )

            orderRepo.upsert(entity)

            val newEntities = draft.items.map {
                it.toEntity(draft.orderId)
            }

            newEntities.forEach { productRepo.upsert(it) }

            _effects.emit(OrderEditorEffect.CloseEditor)
        }
    }

    // --------------------------------------------------------------
    // HELPERS
    // --------------------------------------------------------------

    private fun mutateDraft(block: (OrderEditorUi.Draft) -> OrderEditorUi.Draft) {
        val s = _ui.value
        _ui.value = s.copy(draft = block(s.draft))
    }
}
