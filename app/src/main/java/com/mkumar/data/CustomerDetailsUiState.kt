package com.mkumar.data

data class CustomerDetailsUiState(
    val isLoading: Boolean = false, //should be true and should be set to false on data load
    val error: String? = null,
    val header: CustomerHeaderUi? = null,
    // Group title -> orders in that group
    val ordersByDay: Map<String, List<OrderSummaryUi>> = emptyMap() // "Tue, 07 Oct 2025" -> [...]
)