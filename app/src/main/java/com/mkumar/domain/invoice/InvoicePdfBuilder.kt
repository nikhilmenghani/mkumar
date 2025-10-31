// com.mkumar.domain.invoice.InvoicePdfBuilder.kt
package com.mkumar.domain.invoice

interface InvoicePdfBuilder {
    fun build(data: InvoiceData): ByteArray
}
