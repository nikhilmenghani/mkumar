package com.mkumar.model

data class OrderWithCustomerInfo(
    val id: String,
    val invoiceNumber: Long,
    val createdAt: Long,
    val totalAmount: Int,
    val adjustedAmount: Int,
    val remainingBalance: Int,
    val customerId: String,
    val customerName: String,
    val customerPhone: String
)
