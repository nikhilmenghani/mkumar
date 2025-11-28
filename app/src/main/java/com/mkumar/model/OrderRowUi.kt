package com.mkumar.model

import androidx.compose.runtime.Immutable

@Immutable
data class OrderRowUi(
    val id: String,
    val receivedAt: Long,
    val lastUpdatedAt: Long,
    val invoiceNumber: String,
    val amount: Int,
    val remainingBalance: Int,
    val adjustedTotal: Int? = null,               // if != 0 -> use as Total
)