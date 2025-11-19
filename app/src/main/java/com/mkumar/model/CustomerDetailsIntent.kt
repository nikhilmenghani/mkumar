package com.mkumar.model

import android.graphics.Bitmap

sealed interface CustomerDetailsIntent {
    data class DeleteOrder(val orderId: String) : CustomerDetailsIntent
    data class ShareOrder(val orderId: String, val invoiceNumber: String, val logo: Bitmap) : CustomerDetailsIntent
    data class ViewInvoice(val orderId: String, val invoiceNumber: String, val logo: Bitmap) : CustomerDetailsIntent
}