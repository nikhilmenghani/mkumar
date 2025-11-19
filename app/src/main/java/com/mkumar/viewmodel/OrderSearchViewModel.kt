package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderSearchViewModel @Inject constructor(
    private val orderRepo: OrderRepository
) : ViewModel() {

    // ---------- UI State ----------

    data class UiState(
        val customerId: String = "",
        val invoiceQuery: String = "",
        val category: String? = null,
        val owner: String? = null,
        val remainingOnly: Boolean = false,
        val results: List<OrderEntity> = emptyList(),
        val isSearching: Boolean = false
    ) {
        val filterCount: Int get() =
            (if (invoiceQuery.isNotBlank()) 1 else 0) +
                    (if (category != null) 1 else 0) +
                    (if (owner != null) 1 else 0) +
                    (if (remainingOnly) 1 else 0)

        val hasFilters: Boolean get() = filterCount > 0
    }

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    // ---------- Public Updaters ----------

    fun setCustomerId(id: String) {
        _ui.update { it.copy(customerId = id) }
        triggerSearch()
    }

    fun updateInvoiceQuery(value: String) {
        _ui.update { it.copy(invoiceQuery = value) }
    }

    fun updateCategory(value: String?) {
        _ui.update { it.copy(category = value) }
    }

    fun updateOwner(value: String?) {
        _ui.update { it.copy(owner = value) }
    }

    fun updateRemainingOnly(value: Boolean) {
        _ui.update { it.copy(remainingOnly = value) }
    }

    fun clearFilters() {
        _ui.update {
            it.copy(
                invoiceQuery = "",
                category = null,
                owner = null,
                remainingOnly = false
            )
        }
    }

    init {
        observe()
    }

    // ---------- Observe Combined Filters & Search ----------

    @OptIn(FlowPreview::class)
    private fun observe() {
        combine(
            _ui.map { it.customerId },
            _ui.map { it.invoiceQuery },
            _ui.map { it.category },
            _ui.map { it.owner },
            _ui.map { it.remainingOnly }
        ) { customerId, invoice, category, owner, remaining ->
            Params(customerId, invoice, category, owner, remaining)
        }
            .debounce(200)         // Debounce typing only
            .distinctUntilChanged()
            .onEach { params ->
                if (params.customerId.isBlank()) return@onEach

                _ui.update { it.copy(isSearching = true) }

                val results = runCatching {
                    orderRepo.searchOrders(
                        customerId = params.customerId,
                        invoice = params.invoice.takeIf { it.isNotBlank() },
                        category = params.category,
                        owner = params.owner,
                        remainingOnly = params.remaining
                    )
                }.getOrDefault(emptyList())

                _ui.update { it.copy(results = results, isSearching = false) }
            }
            .launchIn(viewModelScope)
    }

    // manually triggered search (e.g. after setting customerId)
    private fun triggerSearch() {
        viewModelScope.launch {
            val s = _ui.value

            if (s.customerId.isBlank()) return@launch

            _ui.update { it.copy(isSearching = true) }

            val results = runCatching {
                orderRepo.searchOrders(
                    customerId = s.customerId,
                    invoice = s.invoiceQuery.takeIf { it.isNotBlank() },
                    category = s.category,
                    owner = s.owner,
                    remainingOnly = s.remainingOnly
                )
            }.getOrDefault(emptyList())

            _ui.update { it.copy(results = results, isSearching = false) }
        }
    }

    // Helper tuple
    private data class Params(
        val customerId: String,
        val invoice: String,
        val category: String?,
        val owner: String?,
        val remaining: Boolean
    )
}
