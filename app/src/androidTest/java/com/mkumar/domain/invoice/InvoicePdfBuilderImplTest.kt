package com.mkumar.domain.invoice

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import java.io.File

class InvoicePdfBuilderImplTest {
    @Test
    fun generateInvoicePdf() {
        val builder = InvoicePdfBuilderImpl()
        val data = InvoiceData(
            shopName = "MKumar Store",
            shopAddress = "123 Main St, Delhi",
            orderId = "1001",
            occurredAtText = "2024-06-10",
            customerName = "Amit Kumar",
            customerPhone = "+91 98765 43210",
            items = listOf(
                InvoiceItemRow(
                    "Item A", 2, 100.0, 200.0,
                    discount = 90,
                    owner = "Nikhil",
                    description = "test item A description"
                ),
                InvoiceItemRow(
                    "Item B", 1, 150.0, discount = 90,
                    owner = "Nikhil",
                    description = "test item A description",
                    total = 1000.0
                )
            ),
            subtotal = 350.0,
            ownerName = "Nikhil",
            ownerPhone = "7506712435",
            ownerEmail = "nikhil@menghani.com",
            invoiceNumber = "INV-123",
            adjustedTotal = 990.00,
            advanceTotal = 700.00,
            remainingBalance = 100.00
        )
        val pdfBytes = builder.build(data)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "test_invoice.pdf")
        file.writeBytes(pdfBytes)
        // Open test_invoice.pdf manually to preview
    }
}