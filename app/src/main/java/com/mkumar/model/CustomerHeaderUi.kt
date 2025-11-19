package com.mkumar.model

import androidx.compose.runtime.Immutable

@Immutable
data class CustomerHeaderUi(
    val customer: UiCustomer?,
    val totalOrders: Int,
    val totalSpent: Int,
    val totalRemaining: Int,
)