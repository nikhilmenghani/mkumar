package com.mkumar.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.impl.UiCustomerMini
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: CustomerRepository
) : ViewModel() {


    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()


    private val _results = MutableStateFlow<List<UiCustomerMini>>(emptyList())
    val results: StateFlow<List<UiCustomerMini>> = _results.asStateFlow()


    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()


    private val _recent = MutableStateFlow<List<UiCustomerMini>>(emptyList())
    val recent: StateFlow<List<UiCustomerMini>> = _recent.asStateFlow()


    init {
        observeQuery()
// Optionally load recent customers when query is blank
        viewModelScope.launch { _recent.value = loadRecentCustomers() }
    }


    fun updateQuery(value: String) { _query.value = value }


    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        _query
            .debounce(200)
            .map { it.trim() }
            .distinctUntilChanged()
            .onEach { q ->
                if (q.isEmpty()) {
                    _results.value = emptyList()
                    _isSearching.value = false
                } else {
                    _isSearching.value = true
                    val items = runCatching { repo.searchCustomers(q, limit = 50) }.getOrDefault(emptyList())
                    _results.value = items
                    _isSearching.value = false
                }
            }
            .launchIn(viewModelScope)
    }


    private suspend fun loadRecentCustomers(): List<UiCustomerMini> {
// Optional: implement in repo (e.g., lastOrderAt DESC)
        return emptyList()
    }
}