// com.mkumar.domain.invoice.InvoiceData.kt
package com.mkumar.domain.invoice

import android.graphics.Bitmap

data class InvoiceData(
    val shopName: String,
    val shopAddress: String,
    val ownerName: String,
    val customerName: String,
    val customerPhone: String,
    val ownerPhone: String,
    val ownerEmail: String,
    val orderId: String,
    val invoiceNumber: String,
    val occurredAtText: String,
    val items: List<InvoiceItemRow>,
    val subtotal: Double,
    val adjustedTotal: Double,
    val advanceTotal: Double,
    val remainingBalance: Double,
    val logoBitmap: Bitmap? = null
)

data class InvoiceItemRow(
    val name: String,
    val qty: Int,
    val productType: String,
    val unitPrice: Double,
    val total: Double,
    val discount: Int,
    val owner: String,
    val description: String
)