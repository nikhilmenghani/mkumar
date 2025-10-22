package com.mkumar.data

data class CustomerHeaderUi(
    val id: String,
    val displayName: String,
    val phoneFormatted: String,
    val totalOrders: Int?,
    val lifetimeValueFormatted: String?, // null if not computed in Phase 1
    val lastVisitFormatted: String?      // e.g., "07 Oct 2025" or null
)