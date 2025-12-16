package com.mkumar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.common.constant.CustomerDetailsConstants
import com.mkumar.data.PreferencesManager
import com.mkumar.domain.invoice.InvoiceManager
import com.mkumar.domain.pricing.PricingService
import com.mkumar.model.CustomerDetailsEffect
import com.mkumar.model.CustomerDetailsIntent
import com.mkumar.model.CustomerDetailsUiState
import com.mkumar.model.UiCustomer
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.OrderRepository
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
import javax.inject.Inject

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val customerRepo: CustomerRepository,
    private val orderRepo: OrderRepository,
    private val pricing: PricingService,
    private val invoiceManager: InvoiceManager,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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
                    rel.toUi(pricing = pricing, invoicePrefix = preferencesManager.invoicePrefs.invoicePrefix)
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
                        order.toUiOrder(invoicePrefix = preferencesManager.invoicePrefs.invoicePrefix)
                    }.sortedByDescending { it.receivedAt }
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
            is CustomerDetailsIntent.CreateOrder -> createOrder(intent.customerId)
            is CustomerDetailsIntent.DeleteOrder -> deleteOrder(intent.orderId)
            is CustomerDetailsIntent.ShareOrder -> shareOrder(intent.orderId, intent.invoiceNumber)
            is CustomerDetailsIntent.ViewInvoice -> viewInvoice(intent.orderId, intent.invoiceNumber)
        }
    }

    fun humanReadableInvoiceLocation(orderId: String, invoiceNumber: String): String {
        val fileName = CustomerDetailsConstants.getInvoiceFileName(
            orderId = orderId,
            invoiceNumber = invoiceNumber,
            invoicePrefix = preferencesManager.invoicePrefs.invoicePrefix,
            invoiceDateFormatOrdinal = preferencesManager.invoicePrefs.invoiceDateFormat) + ".pdf"
        return "Files > Downloads > Documents > MKumar > Invoices > $fileName"
    }

    private fun createOrder(customerId: String){
        viewModelScope.launch {
            try {
                val newOrderId = orderRepo.createDraftOrder(customerId)
                emitMessage("Order created.")
                _effects.emit(CustomerDetailsEffect.OrderCreated(newOrderId))
            } catch (t: Throwable) {
                emitMessage("Failed to create order: ${t.message}")
            }
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

    fun shareOrder(orderId: String, invoiceNumber: String) {
        viewModelScope.launch {
            _effects.emit(CustomerDetailsEffect.ShowMessage("Creating invoice…"))
            when (val r = invoiceManager.createInvoice(orderId, invoiceNumber)) {
                is InvoiceManager.InvoiceResult.Success ->
                    _effects.emit(CustomerDetailsEffect.ShareInvoice(orderId, r.uri))
                is InvoiceManager.InvoiceResult.NotFound ->
                    _effects.emit(CustomerDetailsEffect.ShowMessage(r.message))
                is InvoiceManager.InvoiceResult.Error ->
                    _effects.emit(CustomerDetailsEffect.ShowMessage("Failed: ${r.throwable.message}"))
            }
        }
    }

    fun viewInvoice(orderId: String, invoiceNumber: String) {
        viewModelScope.launch {
            _effects.emit(CustomerDetailsEffect.ShowMessage("Creating invoice…"))
            when (val r = invoiceManager.createInvoice(orderId, invoiceNumber)) {
                is InvoiceManager.InvoiceResult.Success ->
                    _effects.emit(CustomerDetailsEffect.ViewInvoice(orderId, invoiceNumber, r.uri))
                is InvoiceManager.InvoiceResult.NotFound ->
                    _effects.emit(CustomerDetailsEffect.ShowMessage(r.message))
                is InvoiceManager.InvoiceResult.Error ->
                    _effects.emit(CustomerDetailsEffect.ShowMessage("Failed: ${r.throwable.message}"))
            }
        }
    }

    private fun emitMessage(msg: String) {
        _effects.tryEmit(CustomerDetailsEffect.ShowMessage(msg))
    }
}

