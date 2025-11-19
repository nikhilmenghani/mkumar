package com.mkumar.model

/**
 * This ViewModel now becomes "view-only".
 * All editing is done in OrderEditorViewModel.
 */
data class CustomerDetailsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val customer: UiCustomer? = null,
    val orders: List<UiOrder> = emptyList(),
//    val isOrderSheetOpen: Boolean = false,
    val draft: OrderDraft = OrderDraft(),
    val errorMessage: String? = null
)