package com.mkumar.model

sealed interface OrderRowAction {
    data class Open(val orderId: String) : OrderRowAction
    data class ViewInvoice(val orderId: String, val invoiceNumber: String) : OrderRowAction
    data class Delete(val orderId: String) : OrderRowAction
    data class Share(val orderId: String, val invoiceNumber: String) : OrderRowAction
}