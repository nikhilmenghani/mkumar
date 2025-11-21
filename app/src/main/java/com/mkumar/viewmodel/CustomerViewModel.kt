package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.model.UiCustomerMini
import com.mkumar.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class CustomerViewModel @OptIn(ExperimentalTime::class)
@Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _currentCustomerId = MutableStateFlow<String?>(null)
    val currentCustomerId: StateFlow<String?> = _currentCustomerId

    private val _orderSortBy = MutableStateFlow("Invoice")
    private val _orderSortAsc = MutableStateFlow(false)

    val orderSortBy: StateFlow<String> = _orderSortBy.asStateFlow()
    val orderSortAsc: StateFlow<Boolean> = _orderSortAsc.asStateFlow()

    private val _formState = MutableStateFlow(UiCustomerMini())

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentOrders = combine(
        _orderSortBy, _orderSortAsc
    ) { sortBy, asc ->
        Pair(sortBy, asc)
    }.flatMapLatest { (sortBy, asc) ->
        repository.getRecentOrders(
            limit = 10,
            sortBy = sortBy,
            ascending = asc
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun setOrderSortBy(sortBy: String) {
        _orderSortBy.value = sortBy
    }

    fun setOrderSortAsc(asc: Boolean) {
        _orderSortAsc.value = asc
    }

    fun removeCustomer(customerID: String) {
        viewModelScope.launch {
            repository.deleteById(customerID)
        }
    }

    fun selectCustomer(customerID: String?) {
        _currentCustomerId.value = customerID
        _formState.value = _customers.value.find { it.id == customerID }
            ?: UiCustomerMini()
    }
}