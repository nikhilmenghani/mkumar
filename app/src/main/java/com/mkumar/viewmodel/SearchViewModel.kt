package com.mkumar.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.model.OrderWithCustomerInfo
import com.mkumar.model.SearchBy
import com.mkumar.model.SearchMode
import com.mkumar.model.SearchType
import com.mkumar.model.UiCustomerMini
import com.mkumar.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: CustomerRepository
) : ViewModel() {

    data class UiState(
        val query: String = "",
        val invoiceQuery: String = "",
        val remainingOnly: Boolean = false,

        val mode: SearchMode = SearchMode.QUICK,

        // NEW:
        val searchBy: SearchBy = SearchBy.PHONE,
        val searchType: SearchType = SearchType.CUSTOMERS,
        val recent: List<UiCustomerMini> = emptyList(),

        val results: List<UiCustomerMini> = emptyList(),
        val orderResults: List<OrderWithCustomerInfo> = emptyList(),
        val isSearching: Boolean = false
    ) {
        val hasFilters: Boolean get() = invoiceQuery.isNotBlank() || remainingOnly
        val filterCount: Int get() =
            (if (invoiceQuery.isNotBlank()) 1 else 0) +
                    (if (remainingOnly) 1 else 0)
    }

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    fun updateQuery(value: String) {
        _ui.update { it.copy(query = value) }
    }

    fun updateInvoiceQuery(value: String) {
        _ui.update { it.copy(invoiceQuery = value) }
    }

    fun updateRemainingOnly(value: Boolean) {
        _ui.update { it.copy(remainingOnly = value) }
    }

    fun updateMode(mode: SearchMode) {
        _ui.update { it.copy(mode = mode) }
    }

    init {
        loadRecent()
        observe()
    }

    fun loadRecent() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repo.getRecentCustomerList(limit = 5)
            _ui.update { it.copy(recent = list) }
        }
    }


    fun updateSearchBy(by: SearchBy) {
        _ui.update {
            val forcedType = if (by == SearchBy.INVOICE) SearchType.ORDERS else it.searchType
            it.copy(searchBy = by, searchType = forcedType)
        }
    }

    fun updateSearchType(type: SearchType) {
        // If searchBy is invoice, force type = ORDERS only
        if (_ui.value.searchBy == SearchBy.INVOICE) {
            _ui.update { it.copy(searchType = SearchType.ORDERS) }
        } else {
            _ui.update { it.copy(searchType = type) }
        }
    }


    @OptIn(FlowPreview::class)
    private fun observe() {
        combine(
            _ui.map { it.query },
            _ui.map { it.invoiceQuery },
            _ui.map { it.remainingOnly },
            _ui.map { it.mode },
            _ui.map { it.searchType }
        ) { q, invoice, remaining, mode, type ->
            Quint(q, invoice, remaining, mode, type)
        }
            .debounce(300)
            .distinctUntilChanged()
            .onEach { (q, invoice, remaining, mode, type) ->

                // No text → show recent customers only
                if (q.isBlank() && invoice.isBlank() && !remaining) {
                    _ui.update {
                        it.copy(
                            results = emptyList(),
                            orderResults = emptyList(),
                            isSearching = false
                        )
                    }
                    return@onEach
                }

                _ui.update { it.copy(isSearching = true) }

                viewModelScope.launch(Dispatchers.IO) {
                    when (type) {
                        SearchType.CUSTOMERS -> {
                            val results = runCatching {
                                repo.searchCustomersAdvanced(
                                    nameOrPhone = q.takeIf { it.isNotBlank() },
                                    invoice = invoice.takeIf { it.isNotBlank() },
                                    remainingOnly = remaining,
                                    searchMode = mode
                                )
                            }.getOrDefault(emptyList())
                            _ui.update { it.copy(results = results, isSearching = false) }
                        }
                        SearchType.ORDERS -> {
                            val results = runCatching {
                                repo.searchOrdersAdvanced(
                                    invoice = q.takeIf { it.isNotBlank() },
                                )
                            }.getOrDefault(emptyList())
                            _ui.update { it.copy(orderResults = results, isSearching = false) }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadRecentCustomers(): List<UiCustomerMini> {
// Optional: implement in repo (e.g., lastOrderAt DESC)
        return emptyList()
    }

    fun triggerSearch() {
        // Setting the same query value will NOT trigger debounce
        // So force-search by toggling isSearching flag
        val current = _ui.value
        if (current.query.isBlank() && current.invoiceQuery.isBlank() && !current.remainingOnly) {
            return
        }

        _ui.update { it.copy(isSearching = true) }
    }

    fun stopSearch() {
        // Just flip the searching flag
        _ui.update { it.copy(isSearching = false) }
    }

    fun clearResults() {
        _ui.update { it.copy(results = emptyList(), isSearching = false) }
    }

    fun createOrUpdateCustomerCard(name: String, phone: String, email: String? = null): String {
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(), // or ULID
            name = name.trim(),
            phone = phone.trim(),
        )
        viewModelScope.launch {
            // If you want "update if same phone exists", do a DAO lookup and reuse ID.
            repo.upsert(customer)
            // optional: update search index here if you wired SearchDao
        }
        return customer.id
    }

    fun updateCustomer(id: String, name: String, phone: String) {
        viewModelScope.launch {
            repo.upsert(
                CustomerEntity(
                    id = id,                // keep same id to update
                    name = name.trim(),
                    phone = phone.trim()
                )
            )
        }
    }


}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Quint<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
