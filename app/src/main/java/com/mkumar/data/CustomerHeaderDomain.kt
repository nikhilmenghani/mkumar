package com.mkumar.data

data class CustomerHeaderDomain(
    val id: String,
    val displayName: String,
    val phoneFormatted: String,
    val totalOrders: Int?,               // can be null in Phase 1
    val lifetimeValueFormatted: String?, // can be null in Phase 1
    val lastVisitFormatted: String?      // can be null in Phase 1
)