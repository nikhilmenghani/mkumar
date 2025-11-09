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
        val mode: SearchMode = SearchMode.QUICK,
        val results: List<UiCustomerMini> = emptyList(),
        val isSearching: Boolean = false
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    fun updateQuery(value: String) { _ui.update { it.copy(query = value) } }
    fun updateMode(mode: SearchMode) { _ui.update { it.copy(mode = mode) } }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()


    private val _results = MutableStateFlow<List<UiCustomerMini>>(emptyList())
    val results: StateFlow<List<UiCustomerMini>> = _results.asStateFlow()


    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()


    private val _recent = MutableStateFlow<List<UiCustomerMini>>(emptyList())
    val recent: StateFlow<List<UiCustomerMini>> = _recent.asStateFlow()

    init { observe() }


    @OptIn(FlowPreview::class)
    private fun observe() {
        combine(
            _ui.map { it.query },
            _ui.map { it.mode }
        ) { q, m -> q to m }
            .debounce(200)
            .distinctUntilChanged()
            .onEach { (q, m) ->
                if (q.isBlank()) { _ui.update { it.copy(results = emptyList(), isSearching = false) }; return@onEach }
                _ui.update { it.copy(isSearching = true) }
                val items = runCatching { repo.searchCustomers(q, m, 50) }.getOrDefault(emptyList())
                _ui.update { it.copy(results = items, isSearching = false) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadRecentCustomers(): List<UiCustomerMini> {
// Optional: implement in repo (e.g., lastOrderAt DESC)
        return emptyList()
    }
}