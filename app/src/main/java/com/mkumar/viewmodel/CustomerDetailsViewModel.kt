package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.CustomerDetailsUiState
import com.mkumar.data.CustomerHeaderUi
import com.mkumar.data.OrderSummaryUi
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.repository.CustomerRepository
import com.mkumar.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val customers: CustomerRepository,
    private val orders: OrderRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CustomerDetailsUiState())
    val ui: StateFlow<CustomerDetailsUiState> = _ui.asStateFlow()

    private var currentCustomerId: String? = null
    private var creatingDraft = false

    val editingBuffer: MutableMap<String, MutableMap<String, ProductFormData?>> = mutableMapOf()

    private val _openForms = MutableStateFlow<Map<String, String>>(emptyMap())
    val openForms: StateFlow<Map<String, String>> = _openForms

    fun setCustomerId(id: String) {
        if (currentCustomerId == id) return
        currentCustomerId = id
        refresh()
    }

    fun getCurrentCustomerId(): String? = currentCustomerId

//    val customerOrdersUi: StateFlow<CustomerDetailsUiState> =
//        flow {
//            val id = currentCustomerId
//            if (id != null) {
//                val orderSummaries = orders.ordersForCustomer(id)
//                emit(orderSummaries)
//            } else {
//                emit(emptyList())
//            }
//        }
//            .map { orderSummaries ->
//                val locale = Locale.getDefault()
//                val zone = ZoneId.systemDefault()
//                val dayFmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", locale)
//                val timeFmt = DateTimeFormatter.ofPattern("h:mm a", locale)
//
//                val uiOrders = orderSummaries.map { o ->
//                    val invoiceShort = "INV-" + o.id.takeLast(6).uppercase(locale)
//                    OrderSummaryUi(
//                        id = o.id,
//                        invoiceShort = invoiceShort,
//                        subtitle = o.subtitle,
//                        timeFormatted = o.occurredAt.atZone(zone).format(timeFmt),
//                        totalFormatted = if (o.isDraft) null else o.totalFormatted,
//                        isDraft = o.isDraft
//                    )
//                }
//
//                val grouped = uiOrders.groupBy { o ->
//                    val domain = orderSummaries.first { it.id == o.id }
//                    domain.occurredAt.atZone(zone).toLocalDate().format(dayFmt)
//                }.toSortedMap(compareByDescending { dayStr -> dayStr })
//
//                CustomerDetailsUiState(
//                    header = _ui.value.header,
//                    ordersByDay = grouped
//                )
//            }
//            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomerDetailsUiState())

    fun refresh() {
        val id = currentCustomerId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val header = customers.customerHeader(id) // domain model
                val orderSummaries = orders.ordersForCustomer(id) // domain model list

                val locale = Locale.getDefault()
                val zone = ZoneId.systemDefault()
                val dayFmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", locale)
                val timeFmt = DateTimeFormatter.ofPattern("h:mm a", locale)

                val headerUi = CustomerHeaderUi(
                    id = header.id,
                    displayName = header.displayName,
                    phoneFormatted = header.phoneFormatted,
                    totalOrders = header.totalOrders,
                    lifetimeValueFormatted = header.lifetimeValueFormatted, // can be null in Phase 1
                    lastVisitFormatted = header.lastVisitFormatted
                )

                val uiOrders = orderSummaries.map { o ->
                    val invoiceShort = "INV-" + o.id.takeLast(6).uppercase(locale)
                    OrderSummaryUi(
                        id = o.id,
                        invoiceShort = invoiceShort,
                        subtitle = o.subtitle,                 // e.g., "Glasses + Case" or "3 items"
                        timeFormatted = o.occurredAt.atZone(zone).format(timeFmt),
                        totalFormatted = if (o.isDraft) null else o.totalFormatted,
                        isDraft = o.isDraft
                    )
                }

                val grouped = uiOrders.groupBy { o ->
                    // We need the day string; take from a representative occurredAt in domain.
                    // If domain doesn’t expose LocalDate, you can compute along with uiOrders.
                    val domain = orderSummaries.first { it.id == o.id }
                    domain.occurredAt.atZone(zone).toLocalDate().format(dayFmt)
                }.toSortedMap(compareByDescending { dayStr ->
                    // Stable sort by actual date descending; parse back to LocalDate
                    // (If parsing is too heavy, repository can return pre-grouped with keys.)
                    // Simple fallback: keep insertion order from sorted summaries.
                    dayStr
                })

                _ui.update { it.copy(isLoading = false, header = headerUi, ordersByDay = grouped) }
            }.onFailure { e ->
                _ui.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    fun onNewSale() {
        val id = currentCustomerId ?: return
        if (creatingDraft) return
        creatingDraft = true
        viewModelScope.launch {
            runCatching {
                orders.createDraftOrder(id)
            }.onSuccess {
                refresh()
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "Failed to create draft") }
            }
            creatingDraft = false
        }
    }

    fun addProductToOrder(orderId: String, productEntry: ProductEntry) {
        _ui.update { state ->
            val updatedOrdersByDay = state.ordersByDay.mapValues { (_, orders) ->
                orders.map { order ->
                    if (order.id == orderId) {
                        order.copy(
                            products = order.products + productEntry,
                            selectedProductId = productEntry.id
                        )
                    } else {
                        order
                    }
                }
            }
            state.copy(ordersByDay = updatedOrdersByDay)
        }
        openForm(orderId, productEntry.id)
    }

    fun removeProductFromOrder(orderId: String, productId: String) {
        _ui.update { state ->
            val updatedOrdersByDay = state.ordersByDay.mapValues { (_, orders) ->
                orders.map { order ->
                    if (order.id == orderId) {
                        order.copy(
                            products = order.products.filterNot { it.id == productId },
                            selectedProductId = if (order.selectedProductId == productId) null else order.selectedProductId
                        )
                    } else {
                        order
                    }
                }
            }
            state.copy(ordersByDay = updatedOrdersByDay)
        }
    }

    fun saveProductFormData(orderId: String, productId: String, formData: ProductFormData) {
        _ui.update { state ->
            val updatedOrdersByDay = state.ordersByDay.mapValues { (_, orders) ->
                orders.map { order ->
                    if (order.id == orderId) {
                        order.copy(
                            products = order.products.map { product ->
                                if (product.id == productId) {
                                    product.copy(
                                        formData = formData,
                                        isSaved = true
                                    )
                                } else {
                                    product
                                }
                            }
                        )
                    } else {
                        order
                    }
                }
            }
            state.copy(ordersByDay = updatedOrdersByDay)
        }
        // Clear editing buffer after save
        editingBuffer[orderId]?.remove(productId)
        _openForms.update { it - orderId }
    }

    fun saveProductsToOrder(orderId: String, productEntries: List<ProductEntry>) {
        viewModelScope.launch {
            runCatching {
                // Update the order in your repository
                for (productEntry in productEntries) {
                    orders.addProductToOrder(orderId, productEntry)
                }
            }.onSuccess {
                // Refresh UI state after successful update
                refresh()
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: "Failed to add products") }
            }
        }
    }

    fun getOrderById(orderId: String): OrderSummaryUi? {
        return _ui.value.ordersByDay
            .values
            .flatten()
            .find { it.id == orderId }
    }

    fun openForm(orderId: String, productId: String) {
        _openForms.update { it + (orderId to productId) }
        // keep selectedProductId stable (no random UUID overwrites)
        selectProduct(orderId, productId)
    }

    fun selectProduct(orderId: String, productId: String) {
        _ui.update { state ->
            val updatedOrdersByDay = state.ordersByDay.mapValues { (_, orders) ->
                orders.map { order ->
                    if (order.id == orderId) {
                        order.copy(selectedProductId = productId)
                    } else {
                        order
                    }
                }
            }
            state.copy(ordersByDay = updatedOrdersByDay)
        }
    }

    fun updateProductOwnerName(orderId: String, productId: String, newName: String) {
        _ui.update { state ->
            val updatedOrdersByDay = state.ordersByDay.mapValues { (_, orders) ->
                orders.map { order ->
                    if (order.id == orderId) {
                        order.copy(
                            products = order.products.map { product ->
                                if (product.id == productId) {
                                    product.copy(productOwnerName = newName)
                                } else {
                                    product
                                }
                            }
                        )
                    } else {
                        order
                    }
                }
            }
            state.copy(ordersByDay = updatedOrdersByDay)
        }
    }

    fun getOpenFormFlowForOrder(orderId: String): StateFlow<String> {
        return openForms.map { it[orderId] ?: "" }
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    }

    fun hasUnsavedChanges(orderId: String, product: ProductEntry, buffer: ProductFormData?): Boolean {
        return product.isSaved && product.formData != buffer
    }

    fun getEditingProductData(orderId: String, product: ProductEntry): ProductFormData? {
        return editingBuffer[orderId]?.get(product.id) ?: product.formData
    }

    fun updateEditingBuffer(orderId: String, productId: String, formData: ProductFormData) {
        val map = editingBuffer.getOrPut(orderId) { mutableMapOf() }
        map[productId] = formData
    }

    fun onOrderClick(orderId: String) {
        // Phase 1: optional no-op or toast; wire route in later phases
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}
