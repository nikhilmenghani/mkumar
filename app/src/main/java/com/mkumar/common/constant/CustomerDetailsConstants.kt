package com.mkumar.common.constant

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CustomerDetailsConstants {

    fun getInvoiceFileName(orderId: String, withTimeStamp: Boolean = false): String {
        val invoiceShort = "INV-" + orderId.takeLast(6).uppercase(Locale.getDefault())
        return if (withTimeStamp) {
            val dateFormat = SimpleDateFormat("dd-MM-yy-HHmmss", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            "$invoiceShort-$dateStr"
        } else {
            invoiceShort
        }
    }

}