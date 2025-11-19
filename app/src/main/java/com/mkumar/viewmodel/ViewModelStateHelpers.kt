package com.mkumar.viewmodel

import com.mkumar.model.CustomerDetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow


internal fun MutableStateFlow<CustomerDetailsUiState>.updateLoading(value: Boolean) {
    this.value = this.value.copy(isLoading = value)
}


internal fun MutableStateFlow<CustomerDetailsUiState>.updateRefreshing(value: Boolean) {
    this.value = this.value.copy(isRefreshing = value)
}