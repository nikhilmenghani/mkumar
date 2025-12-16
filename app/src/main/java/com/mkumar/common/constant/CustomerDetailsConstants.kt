package com.mkumar.common.constant

import com.mkumar.common.extension.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CustomerDetailsConstants {

    fun getInvoiceFileName(
        orderId: String,
        invoiceNumber: String,
        invoicePrefix: String,
        invoiceDateFormatOrdinal: Int,
        withTimeStamp: Boolean = false
    ): String {

        val invoiceShort = if (invoiceNumber.isBlank()) {
            invoicePrefix + orderId.takeLast(6).uppercase(Locale.getDefault())
        } else {
            if (invoiceNumber.startsWith(invoicePrefix, ignoreCase = true)) {
                invoiceNumber
            } else {
                invoicePrefix + invoiceNumber
            }
        }

        if (!withTimeStamp) return invoiceShort

        val selectedFormat = DateFormat.entries.getOrNull(invoiceDateFormatOrdinal)
            ?: DateFormat.SHORT_DATE_TIME

        val dateFormat = SimpleDateFormat(selectedFormat.pattern, Locale.getDefault())
        val dateStr = dateFormat.format(Date())
            .replace("/", "-")
            .replace(":", "")
            .replace(",", "")
            .replace(" ", "-")

        return "$invoiceShort-$dateStr"
    }
}
