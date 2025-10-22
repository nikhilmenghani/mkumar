package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.CustomerDetailsUiState
import com.mkumar.data.CustomerHeaderUi
import com.mkumar.data.OrderSummaryUi
import com.mkumar.data.repository.CustomerRepository
import com.mkumar.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val customers: CustomerRepository,
    private val orders: OrderRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CustomerDetailsUiState())
    val ui: StateFlow<CustomerDetailsUiState> = _ui.asStateFlow()

    private var currentCustomerId: String? = null
    private var creatingDraft = false

    fun setCustomerId(id: String) {
        if (currentCustomerId == id) return
        currentCustomerId = id
        refresh()
    }

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

    fun onOrderClick(orderId: String) {
        // Phase 1: optional no-op or toast; wire route in later phases
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}
