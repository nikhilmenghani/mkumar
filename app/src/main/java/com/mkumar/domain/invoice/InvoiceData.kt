// com.mkumar.domain.invoice.InvoiceData.kt
package com.mkumar.domain.invoice

data class InvoiceData(
    val shopName: String,
    val shopAddress: String,
    val customerName: String,
    val customerPhone: String,
    val orderId: String,
    val occurredAtText: String,
    val items: List<InvoiceItemRow>,
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val grandTotal: Double
)

data class InvoiceItemRow(
    val name: String,
    val qty: Int,
    val unitPrice: Double,
    val total: Double
)