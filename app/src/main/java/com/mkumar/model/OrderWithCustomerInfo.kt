package com.mkumar.model

data class OrderWithCustomerInfo(
    val id: String,
    val invoiceNumber: String,
    val occurredAt: Long,
    val totalAmount: Int,
    val remainingBalance: Int,
    val customerId: String,
    val customerName: String,
    val customerPhone: String
)
