package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allItems = listOf("Apple", "Banana", "Orange", "Grape", "Mango") // Example data
    private val _filteredItems = MutableStateFlow(_allItems)
    val filteredItems: StateFlow<List<String>> = _filteredItems

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300L) // Debounce to avoid excessive filtering on every keystroke
                .collect { query ->
                    _filteredItems.value = if (query.isBlank()) {
                        _allItems
                    } else {
                        _allItems.filter { it.contains(query, ignoreCase = true) }
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}