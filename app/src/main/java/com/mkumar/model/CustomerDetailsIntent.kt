package com.mkumar.model

sealed interface CustomerDetailsIntent {
    data class CreateOrder(val customerId: String) : CustomerDetailsIntent
    data class DeleteOrder(val orderId: String) : CustomerDetailsIntent
    data class ShareOrder(val orderId: String, val invoiceNumber: String) : CustomerDetailsIntent
    data class ViewInvoice(val orderId: String, val invoiceNumber: String) : CustomerDetailsIntent
}