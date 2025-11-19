package com.mkumar.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.common.constant.CustomerDetailsConstants
import com.mkumar.common.files.saveInvoicePdf
import com.mkumar.data.ProductFormData
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.domain.invoice.InvoiceData
import com.mkumar.domain.invoice.InvoiceItemRow
import com.mkumar.domain.invoice.InvoicePdfBuilderImpl
import com.mkumar.domain.pricing.PricingInput
import com.mkumar.domain.pricing.PricingService
import com.mkumar.model.CustomerDetailsEffect
import com.mkumar.model.CustomerDetailsIntent
import com.mkumar.model.CustomerDetailsUiState
import com.mkumar.model.UiCustomer
import com.mkumar.model.UiOrderItem
import com.mkumar.model.productTypeLabelDisplayNames
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.OrderRepository
import com.mkumar.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val customerRepo: CustomerRepository,
    private val orderRepo: OrderRepository,
    private val orderItemRepo: ProductRepository,
    private val pricing: PricingService,
    @ApplicationContext private val app: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dateFmt = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
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
                        order.toUiOrder()
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
            is CustomerDetailsIntent.DeleteOrder -> deleteOrder(intent.orderId)
            is CustomerDetailsIntent.ShareOrder -> shareOrder(intent.orderId, intent.invoiceNumber, intent.logo)
            is CustomerDetailsIntent.ViewInvoice -> viewInvoice(intent.orderId, intent.invoiceNumber, intent.logo)
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

    private suspend fun generateInvoicePdf(orderId: String, invoiceNumber: String, logo: Bitmap): Uri? {
        val fileName = CustomerDetailsConstants.getInvoiceFileName(orderId, invoiceNumber, withTimeStamp = true) + ".pdf"

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
            val productType = if (e.productTypeLabel == "GeneralProduct") {
                (UiOrderItem.deserializeFormData(e.formDataJson) as? ProductFormData.GeneralProductData)?.productType
                    ?: productTypeLabelDisplayNames[e.productTypeLabel] ?: "Unknown"
            } else {
                productTypeLabelDisplayNames[e.productTypeLabel] ?: "Unknown"
            }
            InvoiceItemRow(
                name = s.customer?.name ?: "",
                qty = e.quantity,
                unitPrice = e.unitPrice.toDouble(),
                total = lineTotal.toDouble(),
                discount = e.discountPercentage,
                description = description,
                owner = e.productOwnerName,
                productType = productType
            )
        }

        val invoiceData = InvoiceData(
            shopName = "M Kumar Luxurious Watch & Optical Store",
            shopAddress = "7, Shlok Height, Opp. Dev Paradise & Dharti Silver, Nr. Mansarovar Road, Chandkheda, Ahmedabad.",
            customerName = uiCustomer?.name ?: "Customer",
            ownerName = "Mahendra Menghani",
            customerPhone = uiCustomer?.phone ?: "-",
            ownerPhone = "942795 6490",
            ownerEmail = "menghani.mahendra@gmail.com",
            orderId = order.id,
            invoiceNumber = order.invoiceSeq.toString(),
            occurredAtText = dateFmt.format(Date(order.occurredAt)),
            items = invoiceItems,
            subtotal = priced.subtotalBeforeAdjust.toDouble(),
            adjustedTotal = priced.adjustedAmount.toDouble(),
            advanceTotal = priced.advanceTotal.toDouble(),
            remainingBalance = priced.remainingBalance.toDouble(),
            logoBitmap = logo
        )

        val bytes = InvoicePdfBuilderImpl().build(invoiceData)
        return saveInvoicePdf(app, fileName, bytes)
    }

    fun shareOrder(orderId: String, invoiceNumber: String, logo: Bitmap) {
        viewModelScope.launch {
            try {
                _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Creating invoice…"))
                val uri = generateInvoicePdf(orderId, invoiceNumber, logo)
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

    fun viewInvoice(orderId: String, invoiceNumber: String, logo: Bitmap) {
        viewModelScope.launch {
            try {
                _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Creating invoice…"))
                val uri = generateInvoicePdf(orderId, invoiceNumber, logo)
                if (uri != null) {
                    _effects.tryEmit(CustomerDetailsEffect.ViewInvoice(orderId, invoiceNumber, uri))
                    _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Invoice ready."))
                } else {
                    _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Order not found."))
                }
            } catch (t: Throwable) {
                _effects.tryEmit(CustomerDetailsEffect.ShowMessage("Failed to create/open invoice: ${t.message}"))
            }
        }
    }

    private fun emitMessage(msg: String) {
        _effects.tryEmit(CustomerDetailsEffect.ShowMessage(msg))
    }
}

