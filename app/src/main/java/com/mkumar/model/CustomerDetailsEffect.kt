package com.mkumar.model

import android.net.Uri

sealed interface CustomerDetailsEffect {
    data class ShowMessage(val message: String) : CustomerDetailsEffect
    data class ViewInvoice(val orderId: String, val invoiceNumber: String, val uri: Uri) : CustomerDetailsEffect
    data class ShareInvoice(val orderId: String, val uri: Uri) : CustomerDetailsEffect
    data class OrderCreated(val orderId: String) : CustomerDetailsEffect
}
