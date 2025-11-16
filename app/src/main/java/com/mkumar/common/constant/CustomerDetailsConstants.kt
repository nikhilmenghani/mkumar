package com.mkumar.common.constant

import com.mkumar.App.Companion.globalClass
import com.mkumar.common.extension.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CustomerDetailsConstants {

    fun getInvoiceFileName(orderId: String, invoiceNumber: String, withTimeStamp: Boolean = false): String {
        val invoiceConstant = globalClass.preferencesManager.invoicePrefs.invoicePrefix
        val invoiceShort = if (invoiceNumber.isBlank()) {
            invoiceConstant + orderId.takeLast(6).uppercase(Locale.getDefault())
        } else {
            if (invoiceNumber.startsWith(invoiceConstant, ignoreCase = true)) {
                invoiceNumber
            } else {
                invoiceConstant + invoiceNumber
            }
        }
        return if (withTimeStamp) {
            val selectedFormat = DateFormat.entries.getOrNull(
                globalClass.preferencesManager.invoicePrefs.invoiceDateFormat
            ) ?: DateFormat.SHORT_DATE_TIME

            val dateFormat = SimpleDateFormat(selectedFormat.pattern, Locale.getDefault())
            val dateStr = dateFormat.format(Date())
                .replace("/", "-")
                .replace(":", "")
                .replace(",", "")
                .replace(" ", "-")

            "$invoiceShort-$dateStr"
        } else {
            invoiceShort
        }
    }


}