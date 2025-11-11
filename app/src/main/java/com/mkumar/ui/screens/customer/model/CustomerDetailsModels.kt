package com.mkumar.ui.screens.customer.model

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
data class CustomerHeaderUi(
    val id: String,
    val name: String,
    val phoneFormatted: String,
    val joinedAt: Long?,
    val totalOrders: Int,
    val totalSpent: Int,
    val totalRemaining: Int,
)

@Immutable
data class OrderRowUi(
    val id: String,
    val occurredAt: Instant,
    val itemsLabel: String,
    val amount: Int,
    val remainingBalance: Int,
    val isQueued: Boolean,     // not yet synced
    val isSynced: Boolean,     // successfully synced
    val hasInvoice: Boolean,
    val adjustedTotal: Int? = null,               // if != 0 -> use as Total
)

