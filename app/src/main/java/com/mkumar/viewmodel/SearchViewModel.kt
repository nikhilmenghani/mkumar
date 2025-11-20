package com.mkumar.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.impl.SearchMode
import com.mkumar.repository.impl.UiCustomerMini
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
        val results: List<UiCustomerMini> = emptyList(),
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
        observe()
    }

    @OptIn(FlowPreview::class)
    private fun observe() {
        combine(
            _ui.map { it.query },
            _ui.map { it.invoiceQuery },
            _ui.map { it.remainingOnly },
            _ui.map { it.mode }
        ) { q, invoice, remaining, mode ->
            Quad(q, invoice, remaining, mode)
        }
            .debounce(200)
            .distinctUntilChanged()
            .onEach { (q, invoice, remaining, mode) ->
                if (q.isBlank() && invoice.isBlank() && !remaining) {
                    _ui.update { it.copy(results = emptyList(), isSearching = false) }
                    return@onEach
                }

                _ui.update { it.copy(isSearching = true) }

                val results = runCatching {
                    repo.searchCustomersAdvanced(
                        nameOrPhone = q.takeIf { it.isNotBlank() },
                        invoice = invoice.takeIf { it.isNotBlank() },
                        remainingOnly = remaining
                    )
                }.getOrDefault(emptyList())

                _ui.update { it.copy(results = results, isSearching = false) }
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


}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
