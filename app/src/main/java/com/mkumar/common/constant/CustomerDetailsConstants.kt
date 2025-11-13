package com.mkumar.common.constant

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CustomerDetailsConstants {

    fun getInvoiceFileName(orderId: String, invoiceNumber: String, withTimeStamp: Boolean = false): String {
        val invoiceShort = if (invoiceNumber.isBlank()) {
            "INV-" + orderId.takeLast(6).uppercase(Locale.getDefault())
        } else {
            "INV-$invoiceNumber"
        }
        return if (withTimeStamp) {
            val dateFormat = SimpleDateFormat("dd-MM-yy-HHmmss", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            "$invoiceShort-$dateStr"
        } else {
            invoiceShort
        }
    }

}